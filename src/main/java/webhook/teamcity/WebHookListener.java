package webhook.teamcity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.NotNull;

import webhook.WebHook;
import webhook.WebHookPayload;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.BuildState;



/**
 * WebHookListner
 * Listens for Server events and then triggers the execution of webhooks if configured.
 */
public class WebHookListener extends BuildServerAdapter {
    
    private final SBuildServer myBuildServer;
    private final ProjectSettingsManager mySettings;
    private final WebHookMainSettings myMainSettings;
    private final WebHookPayloadManager myManager;

    
    public WebHookListener(SBuildServer sBuildServer, ProjectSettingsManager settings, 
    						WebHookMainSettings configSettings, WebHookPayloadManager manager) {

        myBuildServer = sBuildServer;
        mySettings = settings;
        myMainSettings = configSettings;
        myManager = manager;
        
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
		webHook.setTriggerStateBitMask(webHookConfig.getStatemask());
		webHook.setProxy(myMainSettings.getProxyConfigForUrl(webHookConfig.getUrl()));
		Loggers.ACTIVITIES.debug("WebHookListener :: Webhook proxy set to " 
				+ webHook.getProxyHost() + " for " + webHookConfig.getUrl());
	}
    
	private void processBuildEvent(SRunningBuild sRunningBuild, Integer stateInt) {
		WebHookProjectSettings projSettings = 
    		(WebHookProjectSettings) mySettings.getSettings(sRunningBuild.getProjectId(), "webhooks");
    	if (projSettings.isEnabled()){
	    	List<WebHookConfig> whcl = projSettings.getWebHooksConfigs();
	    	Loggers.SERVER.debug("About to process WebHooks for " + 
	    			sRunningBuild.getProjectId() + " at buildState " + BuildState.getShortName(stateInt));
	    	for (Iterator<WebHookConfig> i = whcl.iterator(); i.hasNext();){
				WebHookConfig whc = i.next();
				WebHook wh = new WebHook();
				this.getFromConfig(wh, whc);
				if (myManager.isRegisteredFormat(whc.getPayloadFormat())){
					WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
					wh.setContentType(payloadFormat.getContentType());
					if (stateInt.equals(BuildState.BUILD_STARTED)){
						wh.setPayload(payloadFormat.buildStarted(sRunningBuild, whc.getParams()));
					} else if (stateInt.equals(BuildState.BUILD_INTERRUPTED)){
						wh.setPayload(payloadFormat.buildInterrupted(sRunningBuild, whc.getParams()));
					} else if (stateInt.equals(BuildState.BEFORE_BUILD_FINISHED)){
						wh.setPayload(payloadFormat.beforeBuildFinish(sRunningBuild, whc.getParams()));
					} else if (stateInt.equals(BuildState.BUILD_FINISHED)){
						wh.setPayload(payloadFormat.buildFinished(sRunningBuild, whc.getParams()));
					}
					
					doPost(wh, stateInt);
					
					Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());
				} else {
					Loggers.ACTIVITIES.warn("WebHookListener :: No registered Payload Handler for " + whc.getPayloadFormat());
				}
			}
    	} else {
    		Loggers.ACTIVITIES.debug("WebHookListener :: WebHooks are disasbled for  " + sRunningBuild.getProjectId());
    	}
	}

    public void buildStarted(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, BuildState.BUILD_STARTED);
    }	
	
    public void buildFinished(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, BuildState.BUILD_FINISHED);
    }    

    public void buildInterrupted(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, BuildState.BUILD_INTERRUPTED);
    }      

    public void beforeBuildFinish(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, BuildState.BEFORE_BUILD_FINISHED);
	}
    
    public void buildChangedStatus(SRunningBuild sRunningBuild, Status oldStatus, Status newStatus) {
    	WebHookProjectSettings projSettings = 
    		(WebHookProjectSettings) mySettings.getSettings(sRunningBuild.getProjectId(), "webhooks");
    	if (projSettings.isEnabled()){
	    	List<WebHookConfig> whcl = projSettings.getWebHooksConfigs();
	    	Loggers.SERVER.debug("About to process WebHooks for " + 
	    			sRunningBuild.getProjectId() + " at buildState statusChanged");
	    	for (Iterator<WebHookConfig> i = whcl.iterator(); i.hasNext();){
				WebHookConfig whc = i.next();
				WebHook wh = new WebHook();
				this.getFromConfig(wh, whc);
				if (myManager.isRegisteredFormat(whc.getPayloadFormat())){
					WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
					wh.setContentType(payloadFormat.getContentType());
					wh.setPayload(payloadFormat.buildChangedStatus(sRunningBuild, oldStatus, newStatus, whc.getParams()));
					

					doPost(wh, BuildState.BUILD_CHANGED_STATUS);
					Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());
				} else {
					Loggers.ACTIVITIES.warn("WebHookListener :: No registered Payload Handler for " + whc.getPayloadFormat());
				}
	    	}
    	} else {
    		Loggers.ACTIVITIES.debug("WebHookListener :: WebHooks are disasbled for  " + sRunningBuild.getProjectId());
    	}
    }

    public void responsibleChanged(@NotNull SBuildType sBuildType, 
    		@NotNull ResponsibilityInfo responsibilityInfoOld, @NotNull ResponsibilityInfo responsibilityInfoNew, boolean isUserAction) {
     	WebHookProjectSettings projSettings = 
    		(WebHookProjectSettings) mySettings.getSettings(sBuildType.getProjectId(), "webhooks");
     	if (projSettings.isEnabled()){
	    	List<WebHookConfig> whcl = projSettings.getWebHooksConfigs();
	    	Loggers.SERVER.debug("About to process WebHooks for " + 
	    			sBuildType.getProjectId() + " at buildState responsibilityChanged");
	    	for (Iterator<WebHookConfig> i = whcl.iterator(); i.hasNext();){
				WebHookConfig whc = i.next();
				WebHook wh = new WebHook();
				this.getFromConfig(wh, whc);

				if (myManager.isRegisteredFormat(whc.getPayloadFormat())){
					WebHookPayload payloadFormat = myManager.getFormat(whc.getPayloadFormat());
					wh.setContentType(payloadFormat.getContentType());
					wh.setPayload(payloadFormat.responsibleChanged(sBuildType, 
								responsibilityInfoOld, 
								responsibilityInfoNew, 
								isUserAction, 
								whc.getParams()));
					
					doPost(wh, BuildState.RESPONSIBILITY_CHANGED);
					Loggers.ACTIVITIES.debug("WebHookListener :: " + myManager.getFormat(whc.getPayloadFormat()).getFormatDescription());
				} else {
					Loggers.ACTIVITIES.warn("WebHookListener :: No registered Payload Handler for " + whc.getPayloadFormat());
				}
			}
    	} else {
    		Loggers.ACTIVITIES.debug("WebHooks are disasbled for  " + sBuildType.getProjectId());
     	}
     }

	private void doPost(WebHook wh, Integer bitMask) {
		try {
			/* Get the mask from the webhook.
			 * by default it will be all ones unless it is set 
			 * in the options for the webhook. 
			 * 
			 *  Then, "AND" that with the bitmask of the event 
			 *  we are triggering on. If the result is greater than
			 *  zero, fire off the webhook.
			 */
			if (BuildState.enabled(wh.getEventListBitMask(), bitMask)){
				wh.post();
				Loggers.SERVER.debug("WebHook triggered : " 
						+ wh.getParam("buildStatus") + " " + wh.getUrl() 
						+ " returned " + wh.getStatus() 
						+ " from bitMask " + bitMask.toString() 
						+ " " + wh.getErrorReason());	
				if (wh.isErrored()){
					Loggers.SERVER.error(wh.getErrorReason());
				}
				if ((wh.getStatus() == null || wh.getStatus() > HttpStatus.SC_OK))
					Loggers.ACTIVITIES.warn("WebHookListener :: " + wh.getParam("projectId") + " WebHook (url: " + wh.getUrl() + " proxy: " + wh.getProxyHost() + ":" + wh.getProxyPort()+") returned HTTP status " + wh.getStatus().toString());
			} else {
				Loggers.SERVER.debug("WebHook NOT triggered: " 
						+ wh.getParam("buildStatus") + " " + wh.getUrl() 
						+ " from bitMask " + bitMask.toString());	
			}
		} catch (FileNotFoundException e) {
			Loggers.SERVER.warn(this.getClass().getName() + ":doPost :: " 
					+ "A FileNotFoundException occurred while attempting to execute WebHook. See the following stacktrace");
			Loggers.SERVER.warn(e.toString());
		} catch (IOException e) {
			Loggers.SERVER.warn(this.getClass().getName() + ":doPost :: " 
					+ "An IOException occurred while attempting to execute WebHook. See the following stacktrace");
			Loggers.SERVER.warn(e.toString());
		}
	}

}
