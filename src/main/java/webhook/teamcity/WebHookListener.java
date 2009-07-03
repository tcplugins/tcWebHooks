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
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;



/**
 * WebHookListner
 * Listens for Server events and then triggers the execution of webhooks if configured.
 */
public class WebHookListener extends BuildServerAdapter {
    
    private final SBuildServer myBuildServer;
    private final ProjectSettingsManager mySettings;
    private final WebHookMainSettings myMainSettings;

    
    public WebHookListener(SBuildServer sBuildServer, ProjectSettingsManager settings, WebHookMainSettings configSettings) {

        myBuildServer = sBuildServer;
        mySettings = settings;
        myMainSettings = configSettings;
        
        Loggers.SERVER.info("WebHookListener :: Starting");
    }
    
    public void register(){
        myBuildServer.addListener(this);
        Loggers.SERVER.info("WebHookListener :: Registering");
    }

	public void getFromConfig(WebHook webHook, WebHookConfig webHookConfig){
		webHook.setUrl(webHookConfig.getUrl());
		webHook.setEnabled(webHookConfig.getEnabled());
		webHook.addParams(webHookConfig.getParams());
		webHook.setTriggerStateBitMask(webHookConfig.getStatemask());
		webHook.setProxy(myMainSettings.getProxyConfigForUrl(webHookConfig.getUrl()));
		Loggers.ACTIVITIES.debug("WebHookListener :: Webhook proxy set to " 
				+ webHook.getProxyHost() + " for " + webHookConfig.getUrl());
	}
    
	private void processBuildEvent(SRunningBuild sRunningBuild, String state,
			String stateShort, Integer stateInt) {
		WebHookProjectSettings projSettings = 
    		(WebHookProjectSettings) mySettings.getSettings(sRunningBuild.getProjectId(), "webhooks");
    	if (projSettings.isEnabled()){
	    	List<WebHookConfig> whcl = projSettings.getWebHooksConfigs();
	    	Loggers.SERVER.debug("About to process WebHooks for " + 
	    			sRunningBuild.getProjectId() + " at buildState " + state);
	    	for (Iterator<WebHookConfig> i = whcl.iterator(); i.hasNext();){
				WebHookConfig whc = i.next();
				WebHook wh = new WebHook();
				this.getFromConfig(wh, whc);
				wh.addParam("notifyType", state);
				addMessageParam(sRunningBuild, wh, stateShort);
				wh.addParam("buildStatus", sRunningBuild.getStatusDescriptor().getText());
				addCommonParams(sRunningBuild, wh);
				doPost(wh, stateInt);
			}
    	} else {
    		Loggers.ACTIVITIES.debug("WebHookListener :: WebHooks are disasbled for  " + sRunningBuild.getProjectId());
    	}
	}

    public void buildStarted(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, "buildStarted", "Started", BuildState.BUILD_STARTED);
    }	
	
    public void buildFinished(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, "buildFinished", "Finished", BuildState.BUILD_FINISHED);
    }    

    public void buildInterrupted(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, "buildInterrupted", "been Interrupted", BuildState.BUILD_INTERRUPTED);
    }      

    public void beforeBuildFinish(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, "buildNearlyFinished", "nearly Finished", BuildState.BEFORE_BUILD_FINISHED);
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
				wh.addParam("notifyType", "statusChanged");
				addMessageParam(sRunningBuild, wh, "changed Status from "  + oldStatus.getText() + " to " + newStatus.getText());
				wh.addParam("buildStatus", newStatus.getText());
				wh.addParam("buildStatusPrevious", oldStatus.getText());
				
				addCommonParams(sRunningBuild, wh);
				doPost(wh, BuildState.BUILD_CHANGED_STATUS);
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
				wh.addParam("notifyType", "responsibilityChanged");
				
				wh.addParam("message", "Build " + sBuildType.getFullName().toString()
						+ " has changed responsibility from " 
						+ " " + responsibilityInfoOld.getUser().getDescriptiveName()
						+ " to "
						+ responsibilityInfoNew.getUser().getDescriptiveName()
					);
				
				wh.addParam("text", sBuildType.getFullName().toString()
						+ " changed responsibility from " 
						+ responsibilityInfoOld.getUser().getUsername()
						+ " to "
						+ responsibilityInfoNew.getUser().getUsername()
					);
				
				addCommonParams(sBuildType, wh);
				wh.addParam("comment", responsibilityInfoNew.getComment());
				doPost(wh, BuildState.BUILD_INTERRUPTED);
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

	private void addMessageParam(SRunningBuild sRunningBuild, WebHook webHook, String msgType){
		// Message is a long form message, for on webpages or in email.
		webHook.addParam("message", "Build " + sRunningBuild.getBuildType().getFullName().toString() 
				+ " has " + msgType + ". This is build number " + sRunningBuild.getBuildNumber() 
				+ " and was triggered by " + sRunningBuild.getTriggeredBy().getAsString());
		// Text is designed to be shorter, for use in Text messages and the like.
		webHook.addParam("text", sRunningBuild.getBuildType().getFullName().toString() 
				+ " has " + msgType + ".");

	}

	private void addCommonParams(SRunningBuild sRunningBuild, WebHook webHook) {
		webHook.addParam("buildRunner", sRunningBuild.getBuildType().getBuildRunner().getDisplayName());
		webHook.addParam("buildFullName", sRunningBuild.getBuildType().getFullName().toString());
		webHook.addParam("buildName", sRunningBuild.getBuildType().getName());
		webHook.addParam("buildId", sRunningBuild.getBuildType().getBuildTypeId());
		webHook.addParam("projectName", sRunningBuild.getBuildType().getProjectName());
		webHook.addParam("projectId", sRunningBuild.getBuildType().getProjectId());
		webHook.addParam("buildNumber", sRunningBuild.getBuildNumber());
		webHook.addParam("agentName", sRunningBuild.getAgentName());
		webHook.addParam("agentOs", sRunningBuild.getAgent().getOperatingSystemName());
		webHook.addParam("agentHostname", sRunningBuild.getAgent().getHostName());
		webHook.addParam("triggeredBy", sRunningBuild.getTriggeredBy().getAsString());
	}
	
	private void addCommonParams(SBuildType buildType, WebHook webHook) {
		webHook.addParam("buildRunner", buildType.getBuildRunner().getDisplayName());
		webHook.addParam("buildFullName", buildType.getFullName().toString());
		webHook.addParam("buildName", buildType.getName());
		webHook.addParam("buildId", buildType.getBuildTypeId());
		webHook.addParam("projectName", buildType.getProjectName());
		webHook.addParam("projectId", buildType.getProjectId());
	}

}
