/**
 * 
 */
package webhook.teamcity.payload.format;

import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.BuildState;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.WebHookPayloadContent;

public abstract class WebHookPayloadGeneric implements WebHookPayload {
	
	WebHookPayloadManager myManager;
	
	public WebHookPayloadGeneric(WebHookPayloadManager manager){
		this.setPayloadManager(manager);
	}

	public void setPayloadManager(WebHookPayloadManager manager){
		myManager = manager;
	}
	
	public abstract void register();
		
	
	public String beforeBuildFinish(SRunningBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadContent content = new WebHookPayloadContent(runningBuild, previousBuild, BuildState.BEFORE_BUILD_FINISHED, extraParameters);
		return getStatusAsString(content);
	}

	public String buildChangedStatus(SRunningBuild runningBuild, SFinishedBuild previousBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String, String> extraParameters) {
		
		WebHookPayloadContent content = new WebHookPayloadContent(runningBuild, previousBuild, BuildState.BUILD_CHANGED_STATUS, extraParameters);

		// Message is a long form message, for on webpages or in email.
		content.setMessage("Build " + runningBuild.getBuildType().getFullName().toString() 
				+ " has changed Status from " + oldStatus.getText() + " to " + newStatus.getText()
				+ " This is build number " + runningBuild.getBuildNumber() 
				+ " and was triggered by " + runningBuild.getTriggeredBy().getAsString());
		// Text is designed to be shorter, for use in Text messages and the like.
		content.setText(runningBuild.getBuildType().getFullName().toString() 
				+ " has changed Status from "  + oldStatus.getText() + " to " + newStatus.getText() + ".");
		
		content.setBuildStatus(newStatus.getText());
		content.setBuildStatusPrevious(oldStatus.getText());
		
		return getStatusAsString(content);
	}

	public String buildFinished(SRunningBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadContent content = new WebHookPayloadContent(runningBuild, previousBuild, BuildState.BUILD_FINISHED, extraParameters);
		return getStatusAsString(content);
	}

	public String buildInterrupted(SRunningBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadContent content = new WebHookPayloadContent(runningBuild, previousBuild, BuildState.BUILD_INTERRUPTED, extraParameters);
		return getStatusAsString(content);
	}

	public String buildStarted(SRunningBuild runningBuild, SFinishedBuild previousBuild, 
			SortedMap<String, String> extraParameters) {
		WebHookPayloadContent content = new WebHookPayloadContent(runningBuild, previousBuild, BuildState.BUILD_STARTED, extraParameters);
		return getStatusAsString(content);
	}

	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String, String> extraParameters) {
		
		WebHookPayloadContent content = new WebHookPayloadContent(buildType, BuildState.RESPONSIBILITY_CHANGED, extraParameters);
		content.setMessage("Build " + buildType.getFullName().toString()
				+ " has changed responsibility from " 
				+ " " + responsibilityInfoOld.getUser().getDescriptiveName()
				+ " to "
				+ responsibilityInfoNew.getUser().getDescriptiveName()
			);
		content.setText(buildType.getFullName().toString()
				+ " changed responsibility from " 
				+ responsibilityInfoOld.getUser().getUsername()
				+ " to "
				+ responsibilityInfoNew.getUser().getUsername()
			);
		
		content.setComment(responsibilityInfoNew.getComment());
		return getStatusAsString(content);
	}

	protected abstract String getStatusAsString(WebHookPayloadContent content);

	public abstract String getContentType();

	public abstract Integer getRank();

	public abstract void setRank(Integer rank);

	public abstract String getCharset();

	
}
