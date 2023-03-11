package webhook.teamcity.payload.format;

import java.util.Collection;
import java.util.Map;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;

public class WebHookPayloadEmpty implements WebHookPayload, WebHookContentObjectSerialiser {

	public static final String FORMAT_SHORT_NAME = "empty";
	private static final String CHARSET = "UTF-8";
	private static final String CONTENT_TYPE = "text/plain";
	private static final String DESCRIPTION = "None";
	private Integer rank;
	private WebHookPayloadManager myManager;

	public WebHookPayloadEmpty(WebHookPayloadManager manager){
		this.setPayloadManager(manager);
	}

	@Override
	public String buildAddedToQueue(SQueuedBuild sQueuedBuild, ExtraParameters extraParameters,
			Map<String, String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildRemovedFromQueue(SQueuedBuild sQueuedBuild, ExtraParameters extraParameters,
			Map<String, String> templates, WebHookTemplateContent webHookTemplate, String user, String comment) {
		return "";
	}

	@Override
	public String buildPinned(SBuild sBuild, ExtraParameters extraParameters, Map<String, String> templates,
			WebHookTemplateContent webHookTemplate, String username, String comment) {
		return "";
	}

	@Override
	public String buildUnpinned(SBuild sBuild, ExtraParameters extraParameters, Map<String, String> templates,
			WebHookTemplateContent webHookTemplate, String username, String comment) {
		return "";
	}

	@Override
	public String serviceMessageReceived(SBuild sRunningBuild, SFinishedBuild previousBuild,
			ExtraParameters extraParameters, Map<String, String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String beforeBuildFinish(SBuild runningBuild,
			SFinishedBuild previousBuild,
			ExtraParameters extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildChangedStatus(SBuild runningBuild,
			SFinishedBuild previousBuild,
			Status oldStatus, Status newStatus,
			ExtraParameters extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildFinished(SBuild runningBuild,
			SFinishedBuild previousBuild,
			ExtraParameters extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildInterrupted(SBuild runningBuild,
			SFinishedBuild previousBuild,
			ExtraParameters extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String changesLoaded(SBuild runningBuild,
			SFinishedBuild previousBuild,
			ExtraParameters extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildStarted(SBuild runningBuild,
			SFinishedBuild previousBuild,
			ExtraParameters extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String getCharset() {
		return CHARSET;
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public String getFormatDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
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
	public String responsibilityChanged(WebHookResponsibilityHolder responsibilityHolder,
			ExtraParameters mergeParameters, Map<String, String> enabledTemplates,
			WebHookTemplateContent templateForThisBuild) {
		return "";
	}

	@Override
	public String testsMuted(SProject sProject, Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups, ExtraParameters extraParameters,
			Map<String, String> enabledTemplates, WebHookTemplateContent templateForThisBuild) {
		return "";
	}

	@Override
	public String testsUnMuted(SProject sProject, Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups, ExtraParameters extraParameters,
			Map<String, String> enabledTemplates, WebHookTemplateContent templateForThisBuild) {
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

	@Override
	public WebHookStringRenderer getWebHookStringRenderer() {
		return new WebHookStringRenderer() {

			@Override
			public String render(String input) {
				return "<i>This payload returns an empty payload</i>";
			}

			@Override
			public String render(Map<String, String[]> input) throws WebHookHtmlRendererException {
				return "<i>This payload returns an empty payload</i>";
			}

		};
	}

	@Override
	public Object serialiseObject(Object object) {
		return "";
	}

	@Override
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.LEGACY;
	}

}
