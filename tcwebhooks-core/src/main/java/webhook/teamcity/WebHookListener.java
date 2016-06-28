package webhook.teamcity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.tests.TestName;

import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.NotNull;

import webhook.WebHook;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;


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
    private final WebHookTemplateResolver webHookTemplateResolver;
    private final WebHookContentBuilder webHookContentBuilder;
    
    
    public WebHookListener(SBuildServer sBuildServer, ProjectSettingsManager settings, 
    						WebHookMainSettings configSettings, WebHookPayloadManager manager,
    						WebHookFactory factory, WebHookTemplateResolver resolver,
    						WebHookContentBuilder contentBuilder) {

        myBuildServer = sBuildServer;
        mySettings = settings;
        myMainSettings = configSettings;
        myManager = manager;
        webHookFactory = factory;
        webHookTemplateResolver = resolver;
        webHookContentBuilder = contentBuilder;
        
        Loggers.SERVER.info("WebHookListener :: Starting");
    }
    
    public void register(){
        myBuildServer.addListener(this);
        Loggers.SERVER.info("WebHookListener :: Registering");
    }

	private void processBuildEvent(SRunningBuild sRunningBuild, BuildStateEnum state) {
			
			boolean overrideIsEnabled = false;

			Loggers.SERVER.debug("About to process WebHooks for " + sRunningBuild.getProjectId() + " at buildState " + state.getShortName());
			for (WebHookConfig whc : getListOfEnabledWebHooks(sRunningBuild.getProjectId())){
				WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));

				wh = webHookContentBuilder.buildWebHookContent(wh, whc, sRunningBuild, state, overrideIsEnabled);
				
				doPost(wh, whc.getPayloadFormat());
				Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());
	    	}
	}

	/** 
	 * Build a list of Enabled webhooks to pass to the POSTing logic.
	 * @param projectId
	 * @return
	 */
	private List<WebHookConfig> getListOfEnabledWebHooks(String projectId) {
		List<WebHookConfig> configs = new ArrayList<>();
		List<SProject> projects = new ArrayList<>();
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
						if (myManager.isRegisteredFormat(whc.getPayloadFormat())){
							configs.add(whc);
						} else {
							Loggers.ACTIVITIES.warn("WebHookListener :: No registered Payload Handler for " + whc.getPayloadFormat());
						}
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
	public void changesLoaded(SRunningBuild sRunningBuild){
		processBuildEvent(sRunningBuild, BuildStateEnum.CHANGES_LOADED);
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
		for (WebHookConfig whc : getListOfEnabledWebHooks(sBuildType.getProjectId())){
						WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
						WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
						WebHookTemplateContent templateForThisBuild = webHookTemplateResolver.findWebHookTemplate(BuildStateEnum.RESPONSIBILITY_CHANGED, sBuildType, payloadFormat.getFormatShortName(), whc.getPayloadTemplate());
						wh.setContentType(payloadFormat.getContentType());
						wh.setPayload(payloadFormat.responsibleChanged(sBuildType, 
									responsibilityInfoOld, 
									responsibilityInfoNew, 
									isUserAction, 
									WebHookContentBuilder.mergeParameters(whc.getParams(),sBuildType, templateForThisBuild.getPreferredDateTimeFormat()), whc.getEnabledTemplates(), templateForThisBuild));
						wh.setEnabled(whc.isEnabledForBuildType(sBuildType) && wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(wh, whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());
		}
     }

	@Override
	public void responsibleChanged(SProject project,
			Collection<TestName> testNames, ResponsibilityEntry entry,
			boolean isUserAction) {
		Loggers.SERVER.debug("About to process WebHooks for " + project.getProjectId() + " at buildState responsibilityChanged");
		for (WebHookConfig whc : getListOfEnabledWebHooks(project.getProjectId())){
						WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
						WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
						WebHookTemplateContent templateForThisBuild = webHookTemplateResolver.findWebHookTemplate(BuildStateEnum.RESPONSIBILITY_CHANGED, project, payloadFormat.getFormatShortName(), whc.getPayloadTemplate());
						wh.setContentType(payloadFormat.getContentType());
						wh.setPayload(payloadFormat.responsibleChanged(project, 
								testNames, 
								entry, 
									isUserAction, 
									whc.getParams(), whc.getEnabledTemplates(), templateForThisBuild));
						wh.setEnabled(wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(wh, whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());

     	}
	}

	@Override
	public void responsibleChanged(SProject project, TestNameResponsibilityEntry oldTestNameResponsibilityEntry, TestNameResponsibilityEntry newTestNameResponsibilityEntry, boolean isUserAction) {
		Loggers.SERVER.debug("About to process WebHooks for " + project.getProjectId() + " at buildState responsibilityChanged");
		for (WebHookConfig whc : getListOfEnabledWebHooks(project.getProjectId())){
						WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
						WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
						WebHookTemplateContent templateForThisBuild = webHookTemplateResolver.findWebHookTemplate(BuildStateEnum.RESPONSIBILITY_CHANGED, project, payloadFormat.getFormatShortName(), whc.getPayloadTemplate());
						wh.setContentType(payloadFormat.getContentType());
						wh.setPayload(payloadFormat.responsibleChanged(project, 
									oldTestNameResponsibilityEntry, 
									newTestNameResponsibilityEntry, 
									isUserAction, 
									whc.getParams(), whc.getEnabledTemplates(), templateForThisBuild));
						wh.setEnabled(wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(wh, whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());

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
		for (WebHookConfig whc : getListOfEnabledWebHooks(sBuildType.getProjectId())){
						WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
						WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
						WebHookTemplateContent templateForThisBuild = webHookTemplateResolver.findWebHookTemplate(BuildStateEnum.RESPONSIBILITY_CHANGED, sBuildType, payloadFormat.getFormatShortName(), whc.getPayloadTemplate());
						wh.setContentType(payloadFormat.getContentType());
						wh.setPayload(payloadFormat.responsibleChanged(sBuildType, 
									responsibilityEntryOld, 
									responsibilityEntryNew, 
									WebHookContentBuilder.mergeParameters(whc.getParams(),sBuildType, WebHookContentBuilder.getPreferredDateFormat(templateForThisBuild)),
									whc.getEnabledTemplates(), 
									templateForThisBuild)
								);
						wh.setEnabled(whc.isEnabledForBuildType(sBuildType) && wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(wh, whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());
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

}
