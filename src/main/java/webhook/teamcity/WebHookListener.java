package webhook.teamcity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
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
    
    private final SBuildServer myBuildServer;
    private final ProjectSettingsManager mySettings;
    private final WebHookMainSettings myMainSettings;
    private final WebHookPayloadManager myManager;
    private final WebHookFactory webHookFactory;
    
    
    public WebHookListener(SBuildServer sBuildServer, ProjectSettingsManager settings, 
    						WebHookMainSettings configSettings, WebHookPayloadManager manager,
    						WebHookFactory factory) {

        myBuildServer = sBuildServer;
        mySettings = settings;
        myMainSettings = configSettings;
        myManager = manager;
        webHookFactory = factory;
        
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
		webHook.setProxy(myMainSettings.getProxyConfigForUrl(webHookConfig.getUrl()));
		Loggers.ACTIVITIES.debug("WebHookListener :: Webhook proxy set to " 
				+ webHook.getProxyHost() + " for " + webHookConfig.getUrl());
	}
    
	private void processBuildEvent(SRunningBuild sRunningBuild, BuildStateEnum state) {
		WebHookProjectSettings projSettings = 
    		(WebHookProjectSettings) mySettings.getSettings(sRunningBuild.getProjectId(), "webhooks");
    	if (projSettings.isEnabled()){
	    	Loggers.SERVER.debug("About to process WebHooks for " + 
	    			sRunningBuild.getProjectId() + " at buildState " + state.getShortName());
	    	for (WebHookConfig whc : projSettings.getWebHooksConfigs()){
	    		if (whc.getEnabled()){
					WebHook wh = webHookFactory.getWebHook();
					this.getFromConfig(wh, whc);
					if (myManager.isRegisteredFormat(whc.getPayloadFormat())){
						WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
						wh.setContentType(payloadFormat.getContentType());
						
						if (state.equals(BuildStateEnum.BUILD_STARTED)){
							wh.setPayload(payloadFormat.buildStarted(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), whc.getParams()));
							wh.setEnabled(wh.getBuildStates().enabled(BuildStateEnum.BUILD_STARTED));
						} else if (state.equals(BuildStateEnum.BUILD_INTERRUPTED)){
							wh.setPayload(payloadFormat.buildInterrupted(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), whc.getParams()));
							wh.setEnabled(wh.getBuildStates().enabled(BuildStateEnum.BUILD_INTERRUPTED));
						} else if (state.equals(BuildStateEnum.BEFORE_BUILD_FINISHED)){
							wh.setPayload(payloadFormat.beforeBuildFinish(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), whc.getParams()));
							wh.setEnabled(wh.getBuildStates().enabled(BuildStateEnum.BEFORE_BUILD_FINISHED));
						} else if (state.equals(BuildStateEnum.BUILD_FINISHED)){
							wh.setEnabled(wh.getBuildStates().enabled(
									BuildStateEnum.BUILD_FINISHED, 
									sRunningBuild.getStatusDescriptor().isSuccessful(),
									this.hasBuildChangedHistoricalState(sRunningBuild)));
							wh.setPayload(payloadFormat.buildFinished(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), whc.getParams()));;
						}
						
						doPost(wh, whc.getPayloadFormat());
						
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());
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
    		Loggers.ACTIVITIES.debug("WebHookListener :: WebHooks are disasbled for  " + sRunningBuild.getProjectId());
    	}
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
    
    @Override
    public void responsibleChanged(@NotNull SBuildType sBuildType, 
    		@NotNull ResponsibilityInfo responsibilityInfoOld, @NotNull ResponsibilityInfo responsibilityInfoNew, boolean isUserAction) {
     	WebHookProjectSettings projSettings = 
    		(WebHookProjectSettings) mySettings.getSettings(sBuildType.getProjectId(), "webhooks");
     	if (projSettings.isEnabled()){
	    	Loggers.SERVER.debug("About to process WebHooks for " + 
	    			sBuildType.getProjectId() + " at buildState responsibilityChanged");
	    	for (WebHookConfig whc : projSettings.getWebHooksConfigs()){
	    		if (whc.getEnabled()){
	    			WebHook wh = webHookFactory.getWebHook();
					this.getFromConfig(wh, whc);
	
					if (myManager.isRegisteredFormat(whc.getPayloadFormat())){
						WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
						wh.setContentType(payloadFormat.getContentType());
						wh.setPayload(payloadFormat.responsibleChanged(sBuildType, 
									responsibilityInfoOld, 
									responsibilityInfoNew, 
									isUserAction, 
									whc.getParams()));
						wh.setEnabled(wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
						doPost(wh, whc.getPayloadFormat());
						Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());
					
					} else {
						Loggers.ACTIVITIES.warn("WebHookListener :: No registered Payload Handler for " + whc.getPayloadFormat());
					}
	    		} else {
	    			Loggers.ACTIVITIES.debug(this.getClass().getSimpleName() 
	    					+ ":responsibleChanged() :: WebHook disabled. Will not process " + whc.getUrl() + " (" + whc.getPayloadFormat() + ")");
	    		}
			}
    	} else {
    		Loggers.ACTIVITIES.debug("WebHooks are disasbled for  " + sBuildType.getProjectId());
     	}
     }

	@Override
	public void responsibleChanged(SProject project,
			Collection<TestName> testNames, ResponsibilityEntry entry,
			boolean isUserAction) {
		// TODO Auto-generated method stub
		super.responsibleChanged(project, testNames, entry, isUserAction);
	}

	@Override
	public void responsibleChanged(SProject project,
			TestNameResponsibilityEntry oldValue,
			TestNameResponsibilityEntry newValue, boolean isUserAction) {
		// TODO Auto-generated method stub
		super.responsibleChanged(project, oldValue, newValue, isUserAction);
	}
    
    
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

}
