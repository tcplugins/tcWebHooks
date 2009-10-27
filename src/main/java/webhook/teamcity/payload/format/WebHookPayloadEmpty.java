package webhook.teamcity.payload.format;

import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;

public class WebHookPayloadEmpty implements WebHookPayload {

	private final String charset = "UTF-8";
	private final String contentType = "text/plain";
	private final String description = "None";
	private final String shortName = "empty";
	private Integer rank;
	private WebHookPayloadManager myManager;
	
	public WebHookPayloadEmpty(WebHookPayloadManager manager){
		this.setPayloadManager(manager);
	}
	public String beforeBuildFinish(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	public String buildChangedStatus(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	public String buildFinished(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	public String buildInterrupted(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	public String buildStarted(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	public String getCharset() {
		return this.charset;
	}

	public String getContentType() {
		return this.contentType; 
	}

	public String getFormatDescription() {
		return this.description;
	}

	public String getFormatShortName() {
		return this.shortName;
	}

	public String getFormatToolTipText() {
		return "Send a POST request with no content";
	}

	public Integer getRank() {
		return this.rank;
	}

	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}
	
	public void register(){
		myManager.registerPayloadFormat(this);
	}
	public void setPayloadManager(WebHookPayloadManager webhookPayloadManager) {
		myManager = webhookPayloadManager;
	}

}
