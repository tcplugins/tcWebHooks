package webhook.teamcity.payload.format;

import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
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

	@Override
	public String beforeBuildFinish(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	@Override
	public String buildChangedStatus(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	@Override
	public String buildFinished(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	@Override
	public String buildInterrupted(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	@Override
	public String buildStarted(SRunningBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	@Override
	public String getCharset() {
		return this.charset;
	}

	@Override
	public String getContentType() {
		return this.contentType; 
	}

	@Override
	public String getFormatDescription() {
		return this.description;
	}

	@Override
	public String getFormatShortName() {
		return this.shortName;
	}

	@Override
	public String getFormatToolTipText() {
		return "Send a POST request with no content";
	}

	@Override
	public Integer getRank() {
		return this.rank;
	}

	@Override
	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	@Override
	public String responsibleChanged(SBuildType sBuildType,
			ResponsibilityEntry responsibilityEntryOld,
			ResponsibilityEntry responsibilityEntryNew,
			SortedMap<String, String> params) {
		return "";
	}
	
	@Override
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	
	@Override
	public void register(){
		myManager.registerPayloadFormat(this);
	}
	@Override
	public void setPayloadManager(WebHookPayloadManager webhookPayloadManager) {
		myManager = webhookPayloadManager;
	}
}
