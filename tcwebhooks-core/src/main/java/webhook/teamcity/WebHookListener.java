package webhook.teamcity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.ParametersSupport;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.tests.TestName;

import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import webhook.WebHook;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.Loggers;


/**
 * WebHookListner
 * Listens for Server events and then triggers the execution of webhooks if configured.
 */
public class WebHookListener extends BuildServerAdapter {
    
    private static final String WEBHOOKS_SETTINGS_ATTRIBUTE_NAME = "webhooks";
	private final SBuildServer myBuildServer;
    private final ProjectSettingsManager mySettings;
    private final WebHookMainSettings myMainSettings;
    private final WebHookPayloadManager myManager;
    private final WebHookFactory webHookFactory;
    private final WebHookAuthenticatorProvider webHookAuthenticatorProvider;
    
    
    public WebHookListener(SBuildServer sBuildServer, ProjectSettingsManager settings, 
    						WebHookMainSettings configSettings, WebHookPayloadManager manager,
    						WebHookFactory factory, WebHookAuthenticatorProvider webHookAuthenticationProvider) {

        myBuildServer = sBuildServer;
        mySettings = settings;
        myMainSettings = configSettings;
        myManager = manager;
        webHookFactory = factory;
        webHookAuthenticatorProvider = webHookAuthenticationProvider;
        
        Loggers.SERVER.info("WebHookListener :: Starting");
    }
    
    public void register(){
        myBuildServer.addListener(this);
        Loggers.SERVER.info("WebHookListener :: Registering");
    }

	public void getFromConfig(WebHook webHook, WebHookConfig webHookConfig){
		webHook.setUrl(webHookConfig.getUrl());
		webHook.setEnabled(webHookConfig.getEnabled());
		//webHook.addParams(webHookConfig.getParams());
		webHook.setBuildStates(webHookConfig.getBuildStates());
		if (webHookConfig.getAuthenticationConfig() != null){
			WebHookAuthenticator auth = webHookAuthenticatorProvider.getAuthenticator(webHookConfig.getAuthenticationConfig().type);
			auth.setWebHookAuthConfig(webHookConfig.getAuthenticationConfig());
			webHook.setAuthentication(auth);
		}
		webHook.setProxy(myMainSettings.getProxyConfigForUrl(webHookConfig.getUrl()));
		Loggers.ACTIVITIES.debug("WebHookListener :: Webhook proxy set to " 
				+ webHook.getProxyHost() + " for " + webHookConfig.getUrl());
	}
    
	private SortedMap<String,String> mergeParameters(SortedMap<String,String> parametersFromConfig, ParametersSupport build){
		SortedMap<String, String> newMap = new TreeMap<String,String>();
		
		Map<String,String> teamCityProperties = build.getParametersProvider().getAll(); 
		for (String key : teamCityProperties.keySet()){
			if (key.startsWith("webhook.")){
				newMap.put(key.substring("webhook.".length()), teamCityProperties.get(key));
			}
		}
		newMap.putAll(parametersFromConfig);
		return newMap;
	}
	
	private void processBuildEvent(SRunningBuild sRunningBuild, BuildStateEnum state) {

			Loggers.SERVER.debug("About to process WebHooks for " + sRunningBuild.getProjectId() + " at buildState " + state.getShortName());
			for (WebHookConfigWrapper whcw : getListOfEnabledWebHooks(sRunningBuild.getProjectId())){
				WebHookPayload payloadFormat = myManager.getFormat(whcw.whc.getPayloadFormat());
				whcw.wh.setContentType(payloadFormat.getContentType());
				
				if (state.equals(BuildStateEnum.BUILD_STARTED)){
					whcw.wh.setPayload(payloadFormat.buildStarted(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), mergeParameters(whcw.whc.getParams(),sRunningBuild), whcw.whc.getEnabledTemplates()));
					whcw.wh.setEnabled(whcw.whc.isEnabledForBuildType(sRunningBuild.getBuildType()) && whcw.wh.getBuildStates().enabled(BuildStateEnum.BUILD_STARTED));
				} else if (state.equals(BuildStateEnum.BUILD_INTERRUPTED)){
					whcw.wh.setPayload(payloadFormat.buildInterrupted(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), mergeParameters(whcw.whc.getParams(),sRunningBuild), whcw.whc.getEnabledTemplates()));
					whcw.wh.setEnabled(whcw.whc.isEnabledForBuildType(sRunningBuild.getBuildType()) && whcw.wh.getBuildStates().enabled(BuildStateEnum.BUILD_INTERRUPTED));
				} else if (state.equals(BuildStateEnum.BEFORE_BUILD_FINISHED)){
					whcw.wh.setPayload(payloadFormat.beforeBuildFinish(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), mergeParameters(whcw.whc.getParams(),sRunningBuild), whcw.whc.getEnabledTemplates()));
					whcw.wh.setEnabled(whcw.whc.isEnabledForBuildType(sRunningBuild.getBuildType()) && whcw.wh.getBuildStates().enabled(BuildStateEnum.BEFORE_BUILD_FINISHED));
				} else if (state.equals(BuildStateEnum.BUILD_FINISHED)){
					whcw.wh.setEnabled(whcw.whc.isEnabledForBuildType(sRunningBuild.getBuildType()) && whcw.wh.getBuildStates().enabled(
							BuildStateEnum.BUILD_FINISHED, 
							sRunningBuild.getStatusDescriptor().isSuccessful(),
							this.hasBuildChangedHistoricalState(sRunningBuild)));
					whcw.wh.setPayload(payloadFormat.buildFinished(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), mergeParameters(whcw.whc.getParams(),sRunningBuild), whcw.whc.getEnabledTemplates()));;
				}
				
				doPost(whcw.wh, whcw.whc.getPayloadFormat());
				Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whcw.whc.getPayloadFormat()).getFormatDescription());
	    	}
	}

	/** 
	 * Build a list of Enabled webhooks to pass to the POSTing logic.
	 * @param projectId
	 * @return
	 */
	private List<WebHookConfigWrapper> getListOfEnabledWebHooks(String projectId) {
		List<WebHookConfigWrapper> configs = new ArrayList<WebHookListener.WebHookConfigWrapper>();
		List<SProject> projects = new ArrayList<SProject>();
		SProject myProject = myBuildServer.getProjectManager().findProjectById(projectId);
		projects.addAll(myProject.getProjectPath());
		for (SProject project : projects){
			WebHookProjectSettings projSettings = (WebHookProjectSettings) mySettings.getSettings(project.getProjectId(), WEBHOOKS_SETTINGS_ATTRIBUTE_NAME);
	    	if (projSettings.isEnabled()){
		    	for (WebHookConfig whc : projSettings.getWebHooksConfigs()){
		    		if (whc.isEnabledForSubProjects() == false && !myProject.getProjectId().equals(project.getProjectId())){
		    			// Sub-projects are disabled and we are a subproject.
		    			if (Loggers.ACTIVITIES.isDebugEnabled()){
			    			Loggers.ACTIVITIES.debug(this.getClass().getSimpleName() + ":getListOfEnabledWebHooks() "
			    					+ ":: subprojects not enabled. myProject is: " + myProject.getProjectId() + ". webhook project is: " + project.getProjectId());
		    			}
		    			continue;
		    		}
		    		
		    		if (whc.getEnabled()){
						WebHook wh = webHookFactory.getWebHook();
						this.getFromConfig(wh, whc);
						if (myManager.isRegisteredFormat(whc.getPayloadFormat())){
							configs.add(new WebHookConfigWrapper(wh, whc));
						} else {
							Loggers.ACTIVITIES.warn("WebHookListener :: No registered Payload Handler for " + whc.getPayloadFormat());
						}
						wh = null;
		    		} else {
		    			Loggers.ACTIVITIES.debug(this.getClass().getSimpleName() 
		    					+ ":processBuildEvent() :: WebHook disabled. Will not process " + whc.getUrl() + " (" + whc.getPayloadFormat() + ")");
		    		}
				}
	    	} else {
	    		Loggers.ACTIVITIES.debug("WebHookListener :: WebHooks are disasbled for  " + projectId);
	    	}
		}
    	return configs;
	}

	@Override
    public void buildStarted(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_STARTED);
    }	
	
    @Override
    public void buildFinished(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_FINISHED);
    }    

    @Override
    public void buildInterrupted(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_INTERRUPTED);
    }      

    @Override
    public void beforeBuildFinish(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, BuildStateEnum.BEFORE_BUILD_FINISHED);
	}
    
    @Deprecated
    /** This method has been removed from the TeamCity API as of version 7.1
     * 
     * @param sBuildType
     * @param responsibilityInfoOld
     * @param responsibilityInfoNew
     * @param isUserAction
     */
    public void responsibleChanged(@NotNull SBuildType sBuildType, 
    							   @NotNull ResponsibilityInfo responsibilityInfoOld, 
    							   @NotNull ResponsibilityInfo responsibilityInfoNew, 
    							   boolean isUserAction) {
    	
    	if (myBuildServer.getServerMajorVersion() >= 7){
    		return;
    	}
		Loggers.SERVER.debug("About to process WebHooks for " + sBuildType.getProjectId() + " at buildState responsibilityChanged");
		for (WebHookConfigWrapper whcw : getListOfEnabledWebHooks(sBuildType.getProjectId())){

						WebHookPayload payloadFormat = myManager.getFormat(whcw.whc.getPayloadFormat());
						whcw.wh.setContentType(payloadFormat.getContentType());
						whcw.wh.setPayload(payloadFormat.responsibleChanged(sBuildType, 
									responsibilityInfoOld, 
									responsibilityInfoNew, 
									isUserAction, 
									mergeParameters(whcw.whc.getParams(),sBuildType), whcw.whc.getEnabledTemplates()));
						whcw.wh.setEnabled(whcw.whc.isEnabledForBuildType(sBuildType) && whcw.wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(whcw.wh, whcw.whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whcw.whc.getPayloadFormat()).getFormatDescription());
		}
     }

	@Override
	public void responsibleChanged(SProject project,
			Collection<TestName> testNames, ResponsibilityEntry entry,
			boolean isUserAction) {
		Loggers.SERVER.debug("About to process WebHooks for " + project.getProjectId() + " at buildState responsibilityChanged");
		for (WebHookConfigWrapper whcw : getListOfEnabledWebHooks(project.getProjectId())){
						WebHookPayload payloadFormat = myManager.getFormat(whcw.whc.getPayloadFormat());
						whcw.wh.setContentType(payloadFormat.getContentType());
						whcw.wh.setPayload(payloadFormat.responsibleChanged(project, 
								testNames, 
								entry, 
									isUserAction, 
									whcw.whc.getParams(), whcw.whc.getEnabledTemplates()));
						whcw.wh.setEnabled(whcw.wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(whcw.wh, whcw.whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whcw.whc.getPayloadFormat()).getFormatDescription());

     	}
	}

	@Override
	public void responsibleChanged(SProject project, TestNameResponsibilityEntry oldTestNameResponsibilityEntry, TestNameResponsibilityEntry newTestNameResponsibilityEntry, boolean isUserAction) {
		Loggers.SERVER.debug("About to process WebHooks for " + project.getProjectId() + " at buildState responsibilityChanged");
		for (WebHookConfigWrapper whcw : getListOfEnabledWebHooks(project.getProjectId())){
						WebHookPayload payloadFormat = myManager.getFormat(whcw.whc.getPayloadFormat());
						whcw.wh.setContentType(payloadFormat.getContentType());
						whcw.wh.setPayload(payloadFormat.responsibleChanged(project, 
									oldTestNameResponsibilityEntry, 
									newTestNameResponsibilityEntry, 
									isUserAction, 
									whcw.whc.getParams(), whcw.whc.getEnabledTemplates()));
						whcw.wh.setEnabled(whcw.wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(whcw.wh, whcw.whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whcw.whc.getPayloadFormat()).getFormatDescription());

     	}
	}
	
	/**
	 * New version of responsibleChanged, which has some bugfixes, but 
	 * is only available in versions 7.0 and above.    
	 * @param bt
	 * @param oldValue
	 * @param newValue
	 * @since 7.0
	 */
	@Override
	public void responsibleChanged(@NotNull SBuildType sBuildType,
            @NotNull ResponsibilityEntry responsibilityEntryOld,
            @NotNull ResponsibilityEntry responsibilityEntryNew){
		
		Loggers.SERVER.debug("About to process WebHooks for " + sBuildType.getProjectId() + " at buildState responsibilityChanged");
		for (WebHookConfigWrapper whcw : getListOfEnabledWebHooks(sBuildType.getProjectId())){
						WebHookPayload payloadFormat = myManager.getFormat(whcw.whc.getPayloadFormat());
						whcw.wh.setContentType(payloadFormat.getContentType());
						whcw.wh.setPayload(payloadFormat.responsibleChanged(sBuildType, 
									responsibilityEntryOld, 
									responsibilityEntryNew, 
									mergeParameters(whcw.whc.getParams(),sBuildType), whcw.whc.getEnabledTemplates()));
						whcw.wh.setEnabled(whcw.whc.isEnabledForBuildType(sBuildType) && whcw.wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(whcw.wh, whcw.whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whcw.whc.getPayloadFormat()).getFormatDescription());
     	}
	}
	
	public void responsibleRemoved(SProject project, TestNameResponsibilityEntry entry){
		
	}
	
    
	/** doPost used by responsibleChanged
	 * 
	 * @param wh
	 * @param payloadFormat
	 */
	private void doPost(WebHook wh, String payloadFormat) {
		try {
			if (wh.isEnabled()){
				wh.post();
				Loggers.SERVER.info(this.getClass().getSimpleName() + " :: WebHook triggered : " 
						+ wh.getUrl() + " using format " + payloadFormat 
						+ " returned " + wh.getStatus() 
						+ " " + wh.getErrorReason());	
				Loggers.SERVER.debug(this.getClass().getSimpleName() + ":doPost :: content dump: " + wh.getPayload());
				if (wh.isErrored()){
					Loggers.SERVER.error(wh.getErrorReason());
				}
				if ((wh.getStatus() == null || wh.getStatus() > HttpStatus.SC_OK))
					Loggers.ACTIVITIES.warn("WebHookListener :: " + wh.getParam("projectId") + " WebHook (url: " + wh.getUrl() + " proxy: " + wh.getProxyHost() + ":" + wh.getProxyPort()+") returned HTTP status " + wh.getStatus().toString());
			} else {
				Loggers.SERVER.debug("WebHook NOT triggered: " 
						+ wh.getParam("buildStatus") + " " + wh.getUrl());	
			}
		} catch (FileNotFoundException e) {
			Loggers.SERVER.warn(this.getClass().getName() + ":doPost :: " 
					+ "A FileNotFoundException occurred while attempting to execute WebHook (" + wh.getUrl() + "). See the following stacktrace");
			Loggers.SERVER.warn(e);
		} catch (IOException e) {
			Loggers.SERVER.warn(this.getClass().getName() + ":doPost :: " 
					+ "An IOException occurred while attempting to execute WebHook (" + wh.getUrl() + "). See the following stacktrace");
			Loggers.SERVER.warn(e);
		}
	}

	@Nullable
	private SFinishedBuild getPreviousNonPersonalBuild(SRunningBuild paramSRunningBuild)
	  {
	    List<SFinishedBuild> localList = this.myBuildServer.getHistory().getEntriesBefore(paramSRunningBuild, false);

	    for (SFinishedBuild localSFinishedBuild : localList)
	      if (!(localSFinishedBuild.isPersonal())) return localSFinishedBuild;
	    return null;
	}
	
	private boolean hasBuildChangedHistoricalState(SRunningBuild sRunningBuild){
		SFinishedBuild previous = getPreviousNonPersonalBuild(sRunningBuild);
		if (previous != null){
			if (sRunningBuild.getBuildStatus().isSuccessful()){
				return previous.getBuildStatus().isFailed();
			} else if (sRunningBuild.getBuildStatus().isFailed()) {
				return previous.getBuildStatus().isSuccessful();
			}
		}
		return true; 
	}
	
	/**
	 * An inner class to wrap up the WebHook and its WebHookConfig into one unit.
	 *
	 */
	
	private class WebHookConfigWrapper{
		public WebHook wh;
		public WebHookConfig whc;
		
		public WebHookConfigWrapper(WebHook webhook, WebHookConfig webhookConfig) {
			this.wh = webhook;
			this.whc = webhookConfig;
		}
	}

}
