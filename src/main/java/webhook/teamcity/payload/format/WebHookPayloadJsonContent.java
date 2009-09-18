package webhook.teamcity.payload.format;

import java.util.SortedMap;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.BuildState;

public class WebHookPayloadJsonContent {
		String buildStatus, buildStatusPrevious,
		notifyType,
		buildRunner,
		buildFullName,
		buildName,
		buildId,
		buildTypeId,
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
		
		
		public WebHookPayloadJsonContent(SBuildType buildType, Integer buildState, SortedMap<String, String> params) {
			populateCommonContent(buildType, buildState);
		}

		public WebHookPayloadJsonContent(SRunningBuild sRunningBuild,
				Integer buildState, 
				SortedMap<String, String> extraParameters) {
			
    		populateCommonContent(sRunningBuild, buildState);
    		populateMessageAndText(sRunningBuild, buildState);
		}

		private void populateCommonContent(SBuildType buildType,
				Integer buildState) {
			setNotifyType(BuildState.getShortName(buildState));
			setBuildRunner(buildType.getBuildRunner().getDisplayName());
			setBuildFullName(buildType.getFullName().toString());
			setBuildName(buildType.getName());
			setBuildTypeId(buildType.getBuildTypeId());
			setProjectName(buildType.getProjectName());
			setProjectId(buildType.getProjectId());
		}
		
		private void populateMessageAndText(SRunningBuild sRunningBuild,
				Integer buildState) {
			// Message is a long form message, for on webpages or in email.
    		setMessage("Build " + sRunningBuild.getBuildType().getFullName().toString() 
    				+ " has " + BuildState.getDescriptionSuffix(buildState) + ". This is build number " + sRunningBuild.getBuildNumber() 
    				+ ", has a status of \"" + sRunningBuild.getStatusDescriptor().getText() + "\" and was triggered by " + sRunningBuild.getTriggeredBy().getAsString());
    		
			// Text is designed to be shorter, for use in Text messages and the like.    		
    		setText(sRunningBuild.getBuildType().getFullName().toString() 
    				+ " has " + BuildState.getDescriptionSuffix(buildState) + ". Status: " + sRunningBuild.getStatusDescriptor().getText());
		}

		private void populateCommonContent(SRunningBuild sRunningBuild,
				Integer buildState) {
			setBuildStatus(sRunningBuild.getStatusDescriptor().getText());
    		setNotifyType(BuildState.getShortName(buildState));
    		setBuildRunner(sRunningBuild.getBuildType().getBuildRunner().getDisplayName());
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
		}
		
		// Getters and setters
		
		public String getBuildStatus() {
			return buildStatus;
		}

		public void setBuildStatus(String buildStatus) {
			this.buildStatus = buildStatus;
		}

		public String getBuildStatusPrevious() {
			return buildStatusPrevious;
		}

		public void setBuildStatusPrevious(String buildStatusPrevious) {
			this.buildStatusPrevious = buildStatusPrevious;
		}

		public String getNotifyType() {
			return notifyType;
		}

		public void setNotifyType(String notifyType) {
			this.notifyType = notifyType;
		}

		public String getBuildRunner() {
			return buildRunner;
		}

		public void setBuildRunner(String buildRunner) {
			this.buildRunner = buildRunner;
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

		
}