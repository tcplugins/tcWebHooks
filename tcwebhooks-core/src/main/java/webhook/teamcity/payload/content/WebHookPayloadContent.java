package webhook.teamcity.payload.content;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.intellij.util.containers.hash.LinkedHashMap;

import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.vcs.SVcsModification;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.util.VariableMessageBuilder;
import webhook.teamcity.payload.util.WebHooksBeanUtilsVariableResolver;

public class WebHookPayloadContent {
		String buildStatus,
		buildResult, buildResultPrevious, buildResultDelta,
		notifyType,
		buildFullName,
		buildName,
		buildId,
		buildTypeId,
		buildInternalTypeId,
		buildExternalTypeId,
		buildStatusUrl,
		buildStatusHtml,
		buildStartTime,
		currentTime,
		buildFinishTime,
		rootUrl,
		projectName,
		projectId,
		projectInternalId,
		projectExternalId,
		buildNumber,
		agentName,
		agentOs,
		agentHostname,
		triggeredBy,
		comment,
		message,
		text,
		branchName,
		branchDisplayName,
		buildStateDescription,
		responsibilityUserOld,
		responsibilityUserNew;
		Boolean branchIsDefault;
		
		Branch branch;
		List<String> buildRunners;
		WebHooksComment buildComment; 
		List<String> buildTags;
		ExtraParametersMap extraParameters;
		private ExtraParametersMap teamcityProperties;
		private List<WebHooksChanges> changes = new ArrayList<>();
		
		/**
		 * Constructor: Only called by RepsonsibilityChanged.
		 * @param server
		 * @param buildType
		 * @param buildState
		 * @param extraParameters
		 */
		public WebHookPayloadContent(SBuildServer server, SBuildType buildType, BuildStateEnum buildState, Map<String, String> extraParameters, Map<String,String> templates) {
			populateCommonContent(server, buildType, buildState, templates);
			this.extraParameters =  new ExtraParametersMap(extraParameters);
			this.teamcityProperties =  new ExtraParametersMap(buildType.getParametersProvider().getAll());
		}

		/**
		 * Constructor: Called by everything except RepsonsibilityChanged.
		 * @param server
		 * @param sRunningBuild
		 * @param previousBuild
		 * @param buildState
		 * @param extraParameters
		 */
		public WebHookPayloadContent(SBuildServer server, SBuild sRunningBuild, SFinishedBuild previousBuild, 
				BuildStateEnum buildState, 
				Map<String, String> extraParameters, 
				Map<String, String> teamcityProperties,
				Map<String, String> templates) {
			
			this.extraParameters =  new ExtraParametersMap(extraParameters);
			this.teamcityProperties =  new ExtraParametersMap(teamcityProperties);
    		populateCommonContent(server, sRunningBuild, previousBuild, buildState, templates);
    		populateMessageAndText(sRunningBuild, buildState, templates);
    		populateArtifacts(sRunningBuild);
		}

		private void populateArtifacts(SBuild runningBuild) {
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
		private void populateCommonContent(SBuildServer server, SBuildType buildType, BuildStateEnum state, Map<String,String> templates) {
			
			setNotifyType(state.getShortName());
			setBuildRunners(buildType.getBuildRunners());
			setBuildFullName(buildType.getFullName().toString());
			setBuildName(buildType.getName());
			setBuildTypeId(TeamCityIdResolver.getBuildTypeId(buildType));
    		setBuildInternalTypeId(TeamCityIdResolver.getInternalBuildId(buildType));
    		setBuildExternalTypeId(TeamCityIdResolver.getExternalBuildId(buildType));
			setProjectName(buildType.getProjectName());
			setProjectId(TeamCityIdResolver.getProjectId(buildType.getProject()));
			setProjectInternalId(TeamCityIdResolver.getInternalProjectId(buildType.getProject()));
			setProjectExternalId(TeamCityIdResolver.getExternalProjectId(buildType.getProject()));
			setBuildStatusUrl(server.getRootUrl() + "/viewLog.html?buildTypeId=" + buildType.getBuildTypeId() + "&buildId=lastFinished");
			setBuildStateDescription(state.getDescriptionSuffix());
		}
		
		private void populateMessageAndText(SBuild sRunningBuild,
				BuildStateEnum state, Map<String,String> templates) {
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
		private void populateCommonContent(SBuildServer server, SBuild sRunningBuild, SFinishedBuild previousBuild,
				BuildStateEnum buildState, Map<String, String> templates) {
			
			SimpleDateFormat format =  new SimpleDateFormat(); //preferred for locate first, and then override if found.
			if (teamcityProperties.containsKey("webhook.preferedDateFormat")){
				try {
					format = new SimpleDateFormat(teamcityProperties.get("webhook.preferredDateFormat"));
				} 
				catch (NullPointerException npe){}
				catch (IllegalArgumentException iea) {}
				
			} else if (extraParameters.containsKey("preferredDateFormat")){
				try {
					format = new SimpleDateFormat(extraParameters.get("preferredDateFormat"));
				} 
				catch (NullPointerException npe){}
				catch (IllegalArgumentException iea) {}
			} 
			
			setBuildStartTime(format.format(sRunningBuild.getStartDate()));
			
			if (sRunningBuild instanceof SRunningBuild) {
				if (((SRunningBuild) sRunningBuild).getFinishDate() != null){
					setBuildFinishTime(format.format(((SRunningBuild) sRunningBuild).getFinishDate()));
				}
			}
			
			setCurrentTime(format.format(new Date()));

			setBuildStatus(sRunningBuild.getStatusDescriptor().getText());
			setBuildResult(sRunningBuild, previousBuild, buildState);
    		setNotifyType(buildState.getShortName());
    		setBuildRunners(sRunningBuild.getBuildType().getBuildRunners());
    		setBuildFullName(sRunningBuild.getBuildType().getFullName().toString());
    		setBuildName(sRunningBuild.getBuildType().getName());
			setBuildId(Long.toString(sRunningBuild.getBuildId()));
			setBuildTypeId(TeamCityIdResolver.getBuildTypeId(sRunningBuild.getBuildType()));
    		setBuildInternalTypeId(TeamCityIdResolver.getInternalBuildId(sRunningBuild.getBuildType()));
    		setBuildExternalTypeId(TeamCityIdResolver.getExternalBuildId(sRunningBuild.getBuildType()));
    		setProjectName(sRunningBuild.getBuildType().getProjectName());
    		setProjectId(TeamCityIdResolver.getProjectId(sRunningBuild.getBuildType().getProject()));
    		setProjectInternalId(TeamCityIdResolver.getInternalProjectId(sRunningBuild.getBuildType().getProject()));
    		setProjectExternalId(TeamCityIdResolver.getExternalProjectId(sRunningBuild.getBuildType().getProject()));
    		setBuildNumber(sRunningBuild.getBuildNumber());
    		setAgentName(sRunningBuild.getAgentName());
    		setAgentOs(sRunningBuild.getAgent().getOperatingSystemName());
    		setAgentHostname(sRunningBuild.getAgent().getHostName());
    		setTriggeredBy(sRunningBuild.getTriggeredBy().getAsString());
    		setComment(WebHooksComment.build(sRunningBuild.getBuildComment()));
    		setTags(sRunningBuild.getTags());
    		setChanges(sRunningBuild.getContainingChanges());
    		try {
    			if (sRunningBuild.getBranch() != null){
	    			setBranch(sRunningBuild.getBranch());
	    			setBranchName(getBranch().getName());
	    			setBranchDisplayName(getBranch().getDisplayName());
	    			setBranchIsDefault(getBranch().isDefaultBranch());
    			} else {
    				Loggers.SERVER.debug("WebHookPayloadContent :: Branch is null. Either feature branch support is not configured or Teamcity does not support feature branches on this VCS");
    			}
    			
    		} catch (NoSuchMethodError e){
    			Loggers.SERVER.debug("WebHookPayloadContent :: Could not get Branch Info by calling sRunningBuild.getBranch(). Probably an old version of TeamCity");
    		}
    		setBuildStatusUrl(server.getRootUrl() + "/viewLog.html?buildTypeId=" + getBuildTypeId() + "&buildId=" + getBuildId());
    		setBuildStateDescription(buildState.getDescriptionSuffix());
    		setRootUrl(server.getRootUrl());
			setBuildStatusHtml(buildState, templates.get(WebHookPayloadDefaultTemplates.HTML_BUILDSTATUS_TEMPLATE));
		}
		
		public List<String> getBuildTags() {
			return buildTags;
		}
		
		private void setTags(List<String> tags) {
			this.buildTags = new ArrayList<>();
			this.buildTags.addAll(tags);
		}

		public WebHooksComment getBuildComment() {
			return buildComment;
		}
		
		private void setComment(WebHooksComment webHooksComment) {
			this.buildComment = webHooksComment;
			if (webHooksComment != null){
				this.comment = webHooksComment.getComment();
			}
		}
		
		private void setChanges(List<SVcsModification> modifications){
			this.changes = WebHooksChangeBuilder.build(modifications);
		}
		
		public List<WebHooksChanges> getChanges(){
			return changes;
		}

		public String getBuildInternalTypeId() {
			return this.buildInternalTypeId;
		}

		public void setBuildInternalTypeId(String internalBuildId) {
			this.buildInternalTypeId = internalBuildId;
		}
		
		public String getBuildExternalTypeId() {
			return this.buildExternalTypeId;
		}
		
		public void setBuildExternalTypeId(String externalBuildId) {
			this.buildExternalTypeId = externalBuildId;
		}

		public String getProjectInternalId() {
			return this.projectInternalId;
		}
		
		public void setProjectInternalId(String projectId) {
			this.projectInternalId = projectId;
		}

		private Branch getBranch() {
			return this.branch;
		}
		
		public void setBranch(Branch branch) {
			this.branch = new WebHooksBranchImpl(branch);
		}
		
		public String getBranchName() {
			return this.branchName;
		}
			 
		public void setBranchName(String branchName) {
			this.branchName = branchName;
		}
		
		public String getBranchDisplayName() {
			return this.branchDisplayName;
		}
		
		public void setBranchDisplayName(String displayName) {
			this.branchDisplayName = displayName;
		}

		public Boolean getBranchIsDefault() {
			return branchIsDefault;
		}
		
		public Boolean isBranchIsDefault() {
			return branchIsDefault;
		}

		public void setBranchIsDefault(boolean branchIsDefault) {
			this.branchIsDefault = branchIsDefault;
		}

		/**
		 * Determines a useful build result. The one from TeamCity can't be trusted because it
		 * is not set until all the Notifiers have run, of which we are one. 
		 * @param sRunningBuild
		 * @param previousBuild
		 * @param buildState
		 */
		private void setBuildResult(SBuild sRunningBuild,
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

		public String getBuildResult() {
			return buildResult;
		}

		public void setBuildResult(String buildResult) {
			this.buildResult = buildResult;
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

		public List<String> getBuildRunners() {
			return buildRunners;
		}

		public void setBuildRunners(List<SBuildRunnerDescriptor> list) {
			if (list != null){
				buildRunners = new ArrayList<>();
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
		
		public String getProjectExternalId() {
			return projectExternalId;
		}
		
		public void setProjectExternalId(String projectExternalId) {
			this.projectExternalId = projectExternalId;
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

		public String getRootUrl() {
			return rootUrl;
		}

		public void setRootUrl(String rootUrl) {
			this.rootUrl = rootUrl;
		}

		public String getBuildStateDescription() {
			return buildStateDescription;
		}

		public void setBuildStateDescription(String buildStateDescription) {
			this.buildStateDescription = buildStateDescription;
		}

		public String getBuildStatusHtml() {
			return buildStatusHtml;
		}

		public void setBuildStatusHtml(String buildStatusHtml) {
			this.buildStatusHtml = buildStatusHtml;
		}

		
		private void setBuildStatusHtml(BuildStateEnum buildState, final String htmlStatusTemplate) {
			
			VariableMessageBuilder builder = VariableMessageBuilder.create(htmlStatusTemplate, new WebHooksBeanUtilsVariableResolver(this, getAllParameters()));
			this.buildStatusHtml = builder.build();
		}
		
		public String getBuildStartTime() {
			return buildStartTime;
		}
		
		public void setBuildStartTime(String timeString) {
			this.buildStartTime = timeString;
		}
		
		public String getBuildFinishTime() {
			return buildFinishTime;
		}
		
		public void setBuildFinishTime(String finishTime) {
			this.buildFinishTime = finishTime;
		}
		
		public String getCurrentTime() {
			return currentTime;
		}
		
		public void setCurrentTime(String now) {
			this.currentTime = now;
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
		
		public void setResponsibilityUserOld(String responsibilityUserOld) {
			this.responsibilityUserOld = responsibilityUserOld;
		}
		
		public void setResponsibilityUserNew(String responsibilityUserNew) {
			this.responsibilityUserNew = responsibilityUserNew;
		}
		
		public String getResponsibilityUserOld() {
			return responsibilityUserOld;
		}
		
		public String getResponsibilityUserNew() {
			return responsibilityUserNew;
		}
		
		public Map<String, ExtraParametersMap> getAllParameters(){
			Map<String, ExtraParametersMap> allParameters = new LinkedHashMap<String, ExtraParametersMap>();
			
			allParameters.put("teamcity", this.teamcityProperties);
			allParameters.put("webhook", this.extraParameters);
			
			return allParameters;
			
		}

		public ExtraParametersMap getExtraParameters() {
			if (this.extraParameters.size() > 0){
				VariableMessageBuilder builder;
				WebHooksBeanUtilsVariableResolver resolver = new WebHooksBeanUtilsVariableResolver(this, getAllParameters());
				ExtraParametersMap resolvedParametersMap = new ExtraParametersMap(extraParameters);

//				ExtraParametersMap resolvedParametersMap = new ExtraParametersMap(this.teamcityProperties);
//				resolvedParametersMap.putAll(extraParameters);

				for (Entry<String,String> entry  : extraParameters.getEntriesAsSet()){
					builder = VariableMessageBuilder.create(entry.getValue(), resolver);
					resolvedParametersMap.put(entry.getKey(), builder.build());
				}
				resolver = new WebHooksBeanUtilsVariableResolver(this, getAllParameters());
				for (Entry<String,String> entry  : extraParameters.getEntriesAsSet()){
					builder = VariableMessageBuilder.create(entry.getValue(), resolver);
					resolvedParametersMap.put(entry.getKey(), builder.build());
				}
				return resolvedParametersMap;
			} else {
				return null;
			}
				
		}

		public void setExtraParameters(SortedMap<String, String> extraParameters) {
			this.extraParameters = new ExtraParametersMap(extraParameters);
		}

		
}
