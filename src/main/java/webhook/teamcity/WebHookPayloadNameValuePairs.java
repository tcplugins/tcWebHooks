/**
 * 
 */
package webhook.teamcity;

import java.util.HashMap;
import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.WebHookPayload;

public class WebHookPayloadNameValuePairs implements WebHookPayload {
	
	SortedMap<String,Object> paramList;
	WebHookPayloadManager myManager;
	
	@SuppressWarnings("unchecked")
	public WebHookPayloadNameValuePairs(WebHookPayloadManager manager){
		myManager = manager;
		paramList =  (SortedMap<String, Object>) new HashMap();
	}

	public void register(){
		myManager.registerPayloadFormat(this);
	}
	
	public String getFormatDescription() {
		return "Name Value Pairs";
	}

	public String getFormatShortName() {
		return "nvpairs";
	}

	public String beforeBuildFinish(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String buildChangedStatus(SRunningBuild runningBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String buildFinished(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String buildInterrupted(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String buildStarted(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String, String> extraParameters) {
		// TODO Auto-generated method stub
		return null;
	}


	private void addCommonParams(SRunningBuild sRunningBuild) {
		paramList.put("buildRunner", sRunningBuild.getBuildType().getBuildRunner().getDisplayName());
		paramList.put("buildFullName", sRunningBuild.getBuildType().getFullName().toString());
		paramList.put("buildName", sRunningBuild.getBuildType().getName());
		paramList.put("buildId", sRunningBuild.getBuildType().getBuildTypeId());
		paramList.put("projectName", sRunningBuild.getBuildType().getProjectName());
		paramList.put("projectId", sRunningBuild.getBuildType().getProjectId());
		paramList.put("buildNumber", sRunningBuild.getBuildNumber());
		paramList.put("agentName", sRunningBuild.getAgentName());
		paramList.put("agentOs", sRunningBuild.getAgent().getOperatingSystemName());
		paramList.put("agentHostname", sRunningBuild.getAgent().getHostName());
		paramList.put("triggeredBy", sRunningBuild.getTriggeredBy().getAsString());
	}
	
	private void addCommonParams(SBuildType buildType) {
		paramList.put("buildRunner", buildType.getBuildRunner().getDisplayName());
		paramList.put("buildFullName", buildType.getFullName().toString());
		paramList.put("buildName", buildType.getName());
		paramList.put("buildId", buildType.getBuildTypeId());
		paramList.put("projectName", buildType.getProjectName());
		paramList.put("projectId", buildType.getProjectId());
	}
	
	private void addMessageParam(SRunningBuild sRunningBuild, String msgType){
		// Message is a long form message, for on webpages or in email.
		paramList.put("message", "Build " + sRunningBuild.getBuildType().getFullName().toString() 
				+ " has " + msgType + ". This is build number " + sRunningBuild.getBuildNumber() 
				+ " and was triggered by " + sRunningBuild.getTriggeredBy().getAsString());
		// Text is designed to be shorter, for use in Text messages and the like.
		paramList.put("text", sRunningBuild.getBuildType().getFullName().toString() 
				+ " has " + msgType + ".");

	}

	
}
