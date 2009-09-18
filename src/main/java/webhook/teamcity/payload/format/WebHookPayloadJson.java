/**
 * 
 */
package webhook.teamcity.payload.format;

import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.BuildState;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class WebHookPayloadJson implements WebHookPayload {
	
	SortedMap<String,Object> paramList;
	WebHookPayloadManager myManager;
	Integer rank = 2;
	String charset = "UTF-8";
	
	public WebHookPayloadJson(WebHookPayloadManager manager){
		myManager = manager;
		paramList =  new TreeMap<String,Object>();
	}

	public void register(){

		myManager.registerPayloadFormat(this);
	}
	
	public String getFormatDescription() {
		return "JSON (beta)";
	}

	public String getFormatShortName() {
		return "json";
	}

	public String beforeBuildFinish(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadJsonContent content = new WebHookPayloadJsonContent(runningBuild, BuildState.BEFORE_BUILD_FINISHED, extraParameters);
		return this.getStatusAsString(content);
	}

	public String buildChangedStatus(SRunningBuild runningBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String, String> extraParameters) {
		
		WebHookPayloadJsonContent content = new WebHookPayloadJsonContent(runningBuild, BuildState.BUILD_CHANGED_STATUS, extraParameters);

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
		
		return this.getStatusAsString(content);
	}

	public String buildFinished(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadJsonContent content = new WebHookPayloadJsonContent(runningBuild, BuildState.BUILD_STARTED, extraParameters);
		return this.getStatusAsString(content);
	}

	public String buildInterrupted(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadJsonContent content = new WebHookPayloadJsonContent(runningBuild, BuildState.BUILD_INTERRUPTED, extraParameters);
		return this.getStatusAsString(content);
	}

	public String buildStarted(SRunningBuild runningBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadJsonContent content = new WebHookPayloadJsonContent(runningBuild, BuildState.BUILD_STARTED, extraParameters);
		return this.getStatusAsString(content);
	}

	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String, String> extraParameters) {
		
		WebHookPayloadJsonContent content = new WebHookPayloadJsonContent(buildType, BuildState.RESPONSIBILITY_CHANGED, extraParameters);
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
		return this.getStatusAsString(content);
	}

	private String getStatusAsString(WebHookPayloadJsonContent content){

		XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("build", WebHookPayloadJsonContent.class);
		return xstream.toXML(content);

	}

	public String getContentType() {
		return "application/json";
	}

	public Integer getRank() {
		return this.rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getCharset() {
		return this.charset;
	}

	
}
