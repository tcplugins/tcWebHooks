package webhook.teamcity.payload.format;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.tests.TestName;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;

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
	public String beforeBuildFinish(SBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildChangedStatus(SBuild runningBuild,
			SFinishedBuild previousBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildFinished(SBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String buildInterrupted(SBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String changesLoaded(SBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}
	
	@Override
	public String buildStarted(SBuild runningBuild,
			SFinishedBuild previousBuild,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
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
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String responsibleChanged(SBuildType sBuildType,
			ResponsibilityEntry responsibilityEntryOld,
			ResponsibilityEntry responsibilityEntryNew,
			SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
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
	public String responsibleChanged(SProject project,
			TestNameResponsibilityEntry oldTestNameResponsibilityEntry,
			TestNameResponsibilityEntry newTestNameResponsibilityEntry,
			boolean isUserAction, SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
	}

	@Override
	public String responsibleChanged(SProject project,
			Collection<TestName> testNames, ResponsibilityEntry entry,
			boolean isUserAction, SortedMap<String,String> extraParameters, Map<String,String> templates, WebHookTemplateContent webHookTemplate) {
		return "";
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
	
}
