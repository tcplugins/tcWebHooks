package webhook.teamcity.payload.content;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;

public class WebHookPayloadContent {
		String buildStatus, buildStatusPrevious,
		buildResult, buildResultPrevious, buildResultDelta,
		notifyType,
		buildFullName,
		buildName,
		buildId,
		buildTypeId,
		buildStatusUrl,
		buildStatusHtml,
		projectName,
		projectId,
		buildNumber,
		agentName,
		agentOs,
		agentHostname,
		triggeredBy,
		comment,
		message,
		text;
		
		List<String> buildRunners;
		ExtraParametersMap extraParameters;
		
		/**
		 * Constructor: Only called by RepsonsibilityChanged.
		 * @param server
		 * @param buildType
		 * @param buildState
		 * @param extraParameters
		 */
		public WebHookPayloadContent(SBuildServer server, SBuildType buildType, BuildStateEnum buildState, SortedMap<String, String> extraParameters) {
			populateCommonContent(server, buildType, buildState);
			this.extraParameters =  new ExtraParametersMap(extraParameters);
		}

		/**
		 * Constructor: Called by everything except RepsonsibilityChanged.
		 * @param server
		 * @param sRunningBuild
		 * @param previousBuild
		 * @param buildState
		 * @param extraParameters
		 */
		public WebHookPayloadContent(SBuildServer server, SRunningBuild sRunningBuild, SFinishedBuild previousBuild, 
				BuildStateEnum buildState, 
				SortedMap<String, String> extraParameters) {
			
    		populateCommonContent(server, sRunningBuild, previousBuild, buildState);
    		populateMessageAndText(sRunningBuild, buildState);
    		populateArtifacts(sRunningBuild);
    		this.extraParameters =  new ExtraParametersMap(extraParameters);
		}

		private void populateArtifacts(SRunningBuild runningBuild) {
			//ArtifactsInfo artInfo = new ArtifactsInfo(runningBuild);
			//artInfo.
			
		}

		/**
		 * Used by RepsonsiblityChanged.
		 * Therefore, does not have access to a specific build instance.
		 * @param server
		 * @param buildType
		 * @param state
		 */
		private void populateCommonContent(SBuildServer server, SBuildType buildType, BuildStateEnum state) {
			setNotifyType(state.getShortName());
			setBuildRunner(buildType.getBuildRunners());
			setBuildFullName(buildType.getFullName().toString());
			setBuildName(buildType.getName());
			setBuildTypeId(buildType.getBuildTypeId());
			setProjectName(buildType.getProjectName());
			setProjectId(buildType.getProjectId());
			setBuildStatusUrl(server.getRootUrl() + "/viewLog.html?buildTypeId=" + buildType.getBuildTypeId() + "&buildId=lastFinished");
		}
		
		private void populateMessageAndText(SRunningBuild sRunningBuild,
				BuildStateEnum state) {
			// Message is a long form message, for on webpages or in email.
    		setMessage("Build " + sRunningBuild.getBuildType().getFullName().toString() 
    				+ " has " + state.getDescriptionSuffix() + ". This is build number " + sRunningBuild.getBuildNumber() 
    				+ ", has a status of \"" + this.buildResult + "\" and was triggered by " + sRunningBuild.getTriggeredBy().getAsString());
    		
			// Text is designed to be shorter, for use in Text messages and the like.    		
    		setText(sRunningBuild.getBuildType().getFullName().toString() 
    				+ " has " + state.getDescriptionSuffix() + ". Status: " + this.buildResult);
		}

		/**
		 * Used by everything except ResponsibilityChanged. Is passed a valid build instance. 
		 * @param server
		 * @param sRunningBuild
		 * @param previousBuild
		 * @param buildState
		 */
		private void populateCommonContent(SBuildServer server, SRunningBuild sRunningBuild, SFinishedBuild previousBuild,
				BuildStateEnum buildState) {
			setBuildStatus(sRunningBuild.getStatusDescriptor().getText());
			setBuildResult(sRunningBuild, previousBuild, buildState);
    		setNotifyType(buildState.getShortName());
    		setBuildRunner(sRunningBuild.getBuildType().getBuildRunners());
    		setBuildFullName(sRunningBuild.getBuildType().getFullName().toString());
    		setBuildName(sRunningBuild.getBuildType().getName());
			setBuildId(Long.toString(sRunningBuild.getBuildId()));
    		setBuildTypeId(sRunningBuild.getBuildType().getBuildTypeId());
    		setProjectName(sRunningBuild.getBuildType().getProjectName());
    		setProjectId(sRunningBuild.getBuildType().getProjectId());
    		setBuildNumber(sRunningBuild.getBuildNumber());
    		setAgentName(sRunningBuild.getAgentName());
    		setAgentOs(sRunningBuild.getAgent().getOperatingSystemName());
    		setAgentHostname(sRunningBuild.getAgent().getHostName());
    		setTriggeredBy(sRunningBuild.getTriggeredBy().getAsString());
    		setBuildStatusUrl(server.getRootUrl() + "/viewLog.html?buildTypeId=" + getBuildTypeId() + "&buildId=" + getBuildId());
			setBuildStatusHtml(server.getRootUrl(), buildState);
		}
		
		/**
		 * Determines a useful build result. The one from TeamCity can't be trusted because it
		 * is not set until all the Notifiers have run, of which we are one. 
		 * @param sRunningBuild
		 * @param previousBuild
		 * @param buildState
		 */
		private void setBuildResult(SRunningBuild sRunningBuild,
				SFinishedBuild previousBuild, BuildStateEnum buildState) {

			if (previousBuild != null){
				if (previousBuild.isFinished()){ 
					if (previousBuild.getStatusDescriptor().isSuccessful()){
						this.buildResultPrevious = WebHookPayload.BUILD_STATUS_SUCCESS;
					} else {
						this.buildResultPrevious = WebHookPayload.BUILD_STATUS_FAILURE;
					}
				} else {
					this.buildResultPrevious = WebHookPayload.BUILD_STATUS_RUNNING;
				}
			} else {
				this.buildResultPrevious = WebHookPayload.BUILD_STATUS_UNKNOWN;
			}

			if (buildState == BuildStateEnum.BEFORE_BUILD_FINISHED || buildState == BuildStateEnum.BUILD_FINISHED){ 
				if (sRunningBuild.getStatusDescriptor().isSuccessful()){
					this.buildResult = WebHookPayload.BUILD_STATUS_SUCCESS;
					if (this.buildResultPrevious.equals(this.buildResult)){
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_NO_CHANGE;
					} else {
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_FIXED;
					}
				} else {
					this.buildResult = WebHookPayload.BUILD_STATUS_FAILURE;
					if (this.buildResultPrevious.equals(this.buildResult)){
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_NO_CHANGE;
					} else {
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_BROKEN;
					}
				}
			} else {
				this.buildResult = WebHookPayload.BUILD_STATUS_RUNNING;
				this.buildResultDelta = WebHookPayload.BUILD_STATUS_UNKNOWN;
			}
			
		}

		// Getters and setters
		
		public String getBuildStatus() {
			return buildStatus;
		}

		public void setBuildStatus(String buildStatus) {
			this.buildStatus = buildStatus;
		}

		public String getBuildResultPrevious() {
			return buildResultPrevious;
		}

		public void setBuildResultPrevious(String buildResultPrevious) {
			this.buildResultPrevious = buildResultPrevious;
		}

		public String getBuildResultDelta() {
			return buildResultDelta;
		}

		public void setBuildResultDelta(String buildResultDelta) {
			this.buildResultDelta = buildResultDelta;
		}

		public String getNotifyType() {
			return notifyType;
		}

		public void setNotifyType(String notifyType) {
			this.notifyType = notifyType;
		}

		public List<String> getBuildRunner() {
			return buildRunners;
		}

		public void setBuildRunner(List<SBuildRunnerDescriptor> list) {
			if (list != null){
				buildRunners = new ArrayList<String>(); 
				for (SBuildRunnerDescriptor runner : list){
					buildRunners.add(runner.getRunType().getDisplayName());
				}
			}
		}

		public String getBuildFullName() {
			return buildFullName;
		}

		public void setBuildFullName(String buildFullName) {
			this.buildFullName = buildFullName;
		}

		public String getBuildName() {
			return buildName;
		}

		public void setBuildName(String buildName) {
			this.buildName = buildName;
		}

		public String getBuildId() {
			return buildId;
		}

		public void setBuildId(String buildId) {
			this.buildId = buildId;
		}

		public String getBuildTypeId() {
			return buildTypeId;
		}

		public void setBuildTypeId(String buildTypeId) {
			this.buildTypeId = buildTypeId;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		public String getProjectId() {
			return projectId;
		}

		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}

		public String getBuildNumber() {
			return buildNumber;
		}

		public void setBuildNumber(String buildNumber) {
			this.buildNumber = buildNumber;
		}

		public String getAgentName() {
			return agentName;
		}

		public void setAgentName(String agentName) {
			this.agentName = agentName;
		}

		public String getAgentOs() {
			return agentOs;
		}

		public void setAgentOs(String agentOs) {
			this.agentOs = agentOs;
		}

		public String getAgentHostname() {
			return agentHostname;
		}

		public void setAgentHostname(String agentHostname) {
			this.agentHostname = agentHostname;
		}

		public String getTriggeredBy() {
			return triggeredBy;
		}

		public void setTriggeredBy(String triggeredBy) {
			this.triggeredBy = triggeredBy;
		}

		public String getBuildStatusUrl() {
			return buildStatusUrl;
		}

		public void setBuildStatusUrl(String buildStatusUrl) {
			this.buildStatusUrl = buildStatusUrl;
		}

		public String getBuildStatusHtml() {
			return buildStatusHtml;
		}

		public void setBuildStatusHtml(String buildStatusHtml) {
			this.buildStatusHtml = buildStatusHtml;
		}

		private void setBuildStatusHtml(String rootUrl, BuildStateEnum buildState) {
			StringBuilder sb = new StringBuilder();
			
			sb.append("<span class=\"tcWebHooksMessage\"><a href=\"").append(rootUrl).append("/project.html?projectId=").append(getProjectId()).append("\">")
						.append(getProjectName()).append("</a> :: <a href=\"").append(rootUrl).append("/viewType.html?buildTypeId=")
						.append(getBuildTypeId()).append("\">").append(getBuildName()).append("</a>");
			
			sb.append(" # <a href=\"").append(rootUrl).append("/viewLog.html?buildTypeId=").append(getBuildTypeId())
						.append("&buildId=").append(getBuildId()).append("\"><strong>").append(getBuildNumber())
						.append("</strong></a>	has <strong>").append(buildState.getDescriptionSuffix()).append("</strong>");
			
			sb.append(" with a status of ")
						.append("<a href=\"").append(rootUrl).append("/viewLog.html?buildTypeId=").append(getBuildTypeId())
						.append("&buildId=").append(getBuildId()).append("\"> <strong>").append(this.buildResult)
						.append("</strong></a> and was triggered by <strong>").append(this.triggeredBy).append("</strong></span>");
			
			this.buildStatusHtml = sb.toString();
		}
		
		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getMessage() {
			return message;
		}


		public void setMessage(String message) {
			this.message = message;
		}


		public String getText() {
			return text;
		}


		public void setText(String text) {
			this.text = text;
		}

		public ExtraParametersMap getExtraParameters() {
			if (this.extraParameters.size() > 0){
				return extraParameters;
			} else {
				return null;
			}
				
		}

		public void setExtraParameters(SortedMap<String, String> extraParameters) {
			this.extraParameters = new ExtraParametersMap(extraParameters);
		}

		
}