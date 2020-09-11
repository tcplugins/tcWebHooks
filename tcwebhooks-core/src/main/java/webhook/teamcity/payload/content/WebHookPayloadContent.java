package webhook.teamcity.payload.content;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;

import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.vcs.SVcsModification;
import lombok.Getter;
import lombok.Setter;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookResponsibility;
import webhook.teamcity.payload.util.StringUtils;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolver;
import webhook.teamcity.payload.variableresolver.VariableResolver;

public class WebHookPayloadContent {
		private static final String MAX_CHANGE_FILE_LIST_SIZE = "maxChangeFileListSize";
		private static final String WEBHOOK_MAX_CHANGE_FILE_LIST_SIZE = "webhook." + MAX_CHANGE_FILE_LIST_SIZE;
		
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
		
		@Getter @Setter Boolean buildIsPersonal;
		@Getter @Setter BuildStateEnum buildEventType;
		@Getter @Setter BuildStateEnum derivedBuildEventType;
		
		Branch branch;
		List<String> buildRunners;
		WebHooksComment buildComment; 
		List<String> buildTags;
		ExtraParameters extraParameters;
		private ExtraParameters teamcityProperties;
		@Getter private int maxChangeFileListSize = 100;
		@Getter private boolean maxChangeFileListCountExceeded = false;
		@Getter private int changeFileListCount = 0;
		private List<WebHooksChanges> changes = new ArrayList<>();
		private WebHookResponsibility responsibilityInfo;
		private String pinEventUsername;

		@Getter @Setter private SBuild build;
		@Getter @Setter private SProject project;
		@Getter @Setter private SBuildType buildType;
		
		/**
		 * Constructor: Only called by RepsonsibilityChanged.
		 * @param variableResolverFactory
		 * @param server
		 * @param buildType
		 * @param buildState
		 * @param extraParameters
		 * @param customTemplates (legacy, eg buildStatusHtmlTemplate)
		 */
		public WebHookPayloadContent(VariableResolverFactory variableResolverFactory, SBuildServer server, WebHookResponsibilityHolder responsibilityHolder, BuildStateEnum buildState, ExtraParameters extraParameters, Map<String,String> templates) {
			populateCommonContent(variableResolverFactory, server, responsibilityHolder, buildState, templates);
			this.extraParameters =  new ExtraParameters(extraParameters);
			if (responsibilityHolder.getSBuildType() != null) {
				this.teamcityProperties =  new ExtraParameters(responsibilityHolder.getSBuildType().getParametersProvider().getAll());
			}
		}
		
		/**
		 * Constructor: Only called by Add and Remove from Queue.
		 * @param server
		 * @param buildType
		 * @param buildState
		 * @param extraParameters2
		 */
		public WebHookPayloadContent(VariableResolverFactory variableResolverFactory, SBuildServer server, SQueuedBuild sQueuedBuild, 
				BuildStateEnum buildState, ExtraParameters extraParameters, Map<String,String> templates, String user, String comment) {
			populateCommonContent(variableResolverFactory, server, sQueuedBuild.getBuildType(), buildState, templates);
			setBuildId(String.valueOf(sQueuedBuild.getBuildPromotion().getId()));
			setTriggeredBy(sQueuedBuild.getTriggeredBy().getAsString());
			this.extraParameters =  extraParameters;
			this.teamcityProperties =  extraParameters.getTeamcityParameters();
		}

		/**
		 * Constructor: Called by Pin events.
		 * @param variableResolverFactory
		 * @param server
		 * @param sBuild
		 * @param previousBuild
		 * @param buildState
		 * @param extraParameters
		 * @param teamcityParameters
		 * @param customTemplates (legacy, eg buildStatusHtmlTemplate)
		 */
		public WebHookPayloadContent(VariableResolverFactory variableResolverFactory, SBuildServer server, SBuild sBuild, 
				BuildStateEnum buildState, 
				ExtraParameters extraParameters, 
				Map<String, String> customTemplates,
				String username,
				String comment) {
			
			this.extraParameters =  extraParameters;
			this.teamcityProperties =  extraParameters.getTeamcityParameters();
    		populateCommonContent(variableResolverFactory, server, sBuild, null, buildState, customTemplates);
    		populateMessageAndText(sBuild, buildState);
    		populateArtifacts(sBuild);
    		if (username != null) {
    			this.pinEventUsername = username; 
    		}
    		if (comment != null) {
    			this.comment = comment;
    		}
		}
		
		/**
		 * Constructor: Called by everything except RepsonsibilityChanged.
		 * @param variableResolverFactory
		 * @param server
		 * @param sRunningBuild
		 * @param previousBuild
		 * @param buildState
		 * @param extraParameters
		 * @param customTemplates (legacy, eg buildStatusHtmlTemplate)
		 */
		public WebHookPayloadContent(VariableResolverFactory variableResolverFactory, SBuildServer server, SBuild sRunningBuild, SFinishedBuild previousBuild, 
				BuildStateEnum buildState, 
				ExtraParameters extraParameters, 
				Map<String, String> customTemplates) {
			
			this.extraParameters =  extraParameters;
			this.teamcityProperties =  extraParameters.getTeamcityParameters();
    		populateCommonContent(variableResolverFactory, server, sRunningBuild, previousBuild, buildState, customTemplates);
    		populateMessageAndText(sRunningBuild, buildState);
    		populateArtifacts(sRunningBuild);
		}

		private void populateArtifacts(SBuild runningBuild) {
			//ArtifactsInfo artInfo = new ArtifactsInfo(runningBuild);
			//artInfo.
			
		}

		/**
		 * Used by RepsonsiblityChanged.
		 * Therefore, does not have access to a specific build instance.
	 	 * @param variableResolverFactory
		 * @param server
	 	 * @param responsibilityHolder
		 * @param state
	 	 * @param templates
		 */
		private void populateCommonContent(VariableResolverFactory variableResolverFactory, SBuildServer server, WebHookResponsibilityHolder responsibilityHolder, BuildStateEnum state, Map<String,String> templates) {

			setAdditionalContext(null, responsibilityHolder.getSBuildType(), responsibilityHolder.getSProject());
			setResponsibilityInfo(responsibilityHolder);
			setNotifyType(state.getShortName());
			setBuildEventType(state);
			setDerivedBuildEventType(state);
			setProjectName(responsibilityHolder.getSProject().getName());
			setProjectId(TeamCityIdResolver.getProjectId(responsibilityHolder.getSProject()));
			setProjectInternalId(TeamCityIdResolver.getInternalProjectId(responsibilityHolder.getSProject()));
			setProjectExternalId(TeamCityIdResolver.getExternalProjectId(responsibilityHolder.getSProject()));
			setRootUrl(StringUtils.stripTrailingSlash(server.getRootUrl()) + "/");
			setBuildStateDescription(state.getDescriptionSuffix());
			String oldUser = "Nobody";
			String newUser = "Nobody";
			try {
				oldUser = responsibilityHolder.getResponsibilityEntryOld().getResponsibleUser().getDescriptiveName();
			} catch (Exception e) {}
			try {
				newUser = responsibilityHolder.getResponsibilityEntryNew().getResponsibleUser().getDescriptiveName();
				setComment(responsibilityHolder.getResponsibilityEntryNew().getComment());
				
			} catch (Exception e) {}
			
			if (responsibilityHolder.getSBuildType() != null) {
				SBuildType sBuildType = responsibilityHolder.getSBuildType();
				setBuildRunners(sBuildType.getBuildRunners());
				setBuildFullName(sBuildType.getFullName());
				setBuildName(sBuildType.getName());
				setBuildTypeId(TeamCityIdResolver.getBuildTypeId(sBuildType));
	    		setBuildInternalTypeId(TeamCityIdResolver.getInternalBuildId(sBuildType));
	    		setBuildExternalTypeId(TeamCityIdResolver.getExternalBuildId(sBuildType));
	    		setBuildStatusUrl(getRootUrl() + "viewLog.html?buildTypeId=" + sBuildType.getBuildTypeId() + "&buildId=lastFinished");
	    		setMessage("Build " + sBuildType.getFullName()
	    		+ " has changed responsibility from " 
	    		+ oldUser
	    		+ " to "
	    		+ newUser
	    		+ " with comment '" 
	    		+ getComment().trim()
	    		+ "'"
	    				);
	    		setText(sBuildType.getFullName()
	    				+ " changed responsibility from " 
	    				+ oldUser
	    				+ " to "
	    				+ newUser
	    				+ " with comment '" 
	    				+ getComment().trim()
	    				+ "'"
	    				);
			}
			
			setResponsibilityUserOld(oldUser);
			setResponsibilityUserNew(newUser);

		}

	private void setAdditionalContext(SBuild sBuild, SBuildType sBuildType, SProject sProject) {
			this.setBuild(sBuild);
			this.setBuildType(sBuildType);
			this.setProject(sProject);
	}

	public void setResponsibilityInfo(WebHookResponsibilityHolder responsibilityHolder) {
			this.responsibilityInfo = WebHookResponsibility.build(responsibilityHolder);
		}
		
		public WebHookResponsibility getResponsibilityInfo() {
			return responsibilityInfo;
		}

		/**
		 * Used by Build Queued.
		 * Therefore, does not have access to a specific build instance.
		 * @param server
		 * @param buildType
		 * @param state
		 */
		private void populateCommonContent(VariableResolverFactory variableResolverFactory, SBuildServer server, SBuildType buildType, BuildStateEnum state, Map<String,String> templates) {

			setAdditionalContext(null, buildType, buildType.getProject());
			setNotifyType(state.getShortName());
			setBuildEventType(state);
			setDerivedBuildEventType(state);
			setBuildRunners(buildType.getBuildRunners());
			setBuildFullName(buildType.getFullName());
			setBuildName(buildType.getName());
			setBuildTypeId(TeamCityIdResolver.getBuildTypeId(buildType));
			setBuildInternalTypeId(TeamCityIdResolver.getInternalBuildId(buildType));
			setBuildExternalTypeId(TeamCityIdResolver.getExternalBuildId(buildType));
			setProjectName(buildType.getProjectName());
			setProjectId(TeamCityIdResolver.getProjectId(buildType.getProject()));
			setProjectInternalId(TeamCityIdResolver.getInternalProjectId(buildType.getProject()));
			setProjectExternalId(TeamCityIdResolver.getExternalProjectId(buildType.getProject()));
			setRootUrl(StringUtils.stripTrailingSlash(server.getRootUrl()) + "/");
			setBuildStatusUrl(getRootUrl() + "viewLog.html?buildTypeId=" + buildType.getBuildTypeId() + "&buildId=lastFinished");
			setBuildStateDescription(state.getDescriptionSuffix());
		}
		
		private void populateMessageAndText(SBuild sRunningBuild,
				BuildStateEnum state) {
			// Message is a long form message, for on webpages or in email.
    		setMessage("Build " + sRunningBuild.getBuildType().getFullName() 
    				+ " has " + state.getDescriptionSuffix() + ". This is build number " + sRunningBuild.getBuildNumber() 
    				+ ", has a status of \"" + this.buildResult + "\" and was triggered by " + sRunningBuild.getTriggeredBy().getAsString());
    		
			// Text is designed to be shorter, for use in Text messages and the like.    		
    		setText(sRunningBuild.getBuildType().getFullName() 
    				+ " has " + state.getDescriptionSuffix() + ". Status: " + this.buildResult);
		}

		/**
		 * Used by everything except ResponsibilityChanged. Is passed a valid build instance. 
		 * @param server
		 * @param sBuild
		 * @param previousBuild
		 * @param buildState
		 */
		private void populateCommonContent(VariableResolverFactory variableResolverFactory, SBuildServer server, SBuild sBuild, SFinishedBuild previousBuild,
				BuildStateEnum buildState, Map<String, String> templates) {
			
			SimpleDateFormat format =  new SimpleDateFormat(); //preferred for locale first, and then override if found.
			if (teamcityProperties.containsKey("webhook.preferedDateFormat")){
				try {
					format = new SimpleDateFormat(teamcityProperties.get("webhook.preferredDateFormat"));
				} 
				catch (NullPointerException | IllegalArgumentException ex){}
				
			} else if (extraParameters.containsKey("preferredDateFormat")){
				try {
					format = new SimpleDateFormat(extraParameters.get("preferredDateFormat"));
				} 
				catch (NullPointerException | IllegalArgumentException ex) {}
			} 
			
			setBuildStartTime(format.format(sBuild.getStartDate()));
			
			if (sBuild instanceof SRunningBuild && ((SRunningBuild) sBuild).getFinishDate() != null) {
				setBuildFinishTime(format.format(((SRunningBuild) sBuild).getFinishDate()));
			}
			if (sBuild instanceof SFinishedBuild && ((SFinishedBuild) sBuild).getFinishDate() != null) {
				setBuildFinishTime(format.format(((SFinishedBuild) sBuild).getFinishDate()));
			}
			
			setCurrentTime(format.format(new Date()));

			setAdditionalContext(sBuild, sBuild.getBuildType(), sBuild.getBuildType().getProject());
			setBuildEventType(buildState);
			setBuildResult(sBuild, previousBuild, buildState);
			setBuildStatus(sBuild.getStatusDescriptor().getText(), buildState);
    		setNotifyType(buildState.getShortName());
    		setBuildRunners(sBuild.getBuildType().getBuildRunners());
    		setBuildFullName(sBuild.getBuildType().getFullName());
    		setBuildName(sBuild.getBuildType().getName());
			setBuildId(Long.toString(sBuild.getBuildId()));
			setBuildTypeId(TeamCityIdResolver.getBuildTypeId(sBuild.getBuildType()));
    		setBuildInternalTypeId(TeamCityIdResolver.getInternalBuildId(sBuild.getBuildType()));
    		setBuildExternalTypeId(TeamCityIdResolver.getExternalBuildId(sBuild.getBuildType()));
    		setProjectName(sBuild.getBuildType().getProjectName());
    		setProjectId(TeamCityIdResolver.getProjectId(sBuild.getBuildType().getProject()));
    		setProjectInternalId(TeamCityIdResolver.getInternalProjectId(sBuild.getBuildType().getProject()));
    		setProjectExternalId(TeamCityIdResolver.getExternalProjectId(sBuild.getBuildType().getProject()));
    		setBuildNumber(sBuild.getBuildNumber());
    		setAgentName(sBuild.getAgentName());
    		setAgentOs(sBuild.getAgent().getOperatingSystemName());
    		setAgentHostname(sBuild.getAgent().getHostName());
    		setTriggeredBy(sBuild.getTriggeredBy().getAsString());
    		setComment(WebHooksComment.build(sBuild.getBuildComment()));
    		setTags(sBuild.getTags());
    		int fileChangeCount = 0;
    		for (SVcsModification mod : sBuild.getContainingChanges()) {
    			fileChangeCount += mod.getChangeCount();
    		}
    		this.changeFileListCount = fileChangeCount;
			setChanges(sBuild.getContainingChanges(), includeVcsFileList());
    		try {
    			if (sBuild.getBranch() != null){
	    			setBranch(sBuild.getBranch());
	    			setBranchName(getBranch().getName());
	    			setBranchDisplayName(getBranch().getDisplayName());
	    			setBranchIsDefault(getBranch().isDefaultBranch());
    			} else {
    				Loggers.SERVER.debug("WebHookPayloadContent :: Branch is null. Either feature branch support is not configured or Teamcity does not support feature branches on this VCS");
    			}
    			
    		} catch (NoSuchMethodError e){
    			Loggers.SERVER.debug("WebHookPayloadContent :: Could not get Branch Info by calling sRunningBuild.getBranch(). Probably an old version of TeamCity");
    		}
    		setRootUrl(StringUtils.stripTrailingSlash(server.getRootUrl()) + "/");
    		setBuildStatusUrl(getRootUrl() + "viewLog.html?buildTypeId=" + getBuildTypeId() + "&buildId=" + getBuildId());
    		setBuildStateDescription(buildState.getDescriptionSuffix());
			setBuildStatusHtml(variableResolverFactory, templates.get(WebHookPayloadDefaultTemplates.HTML_BUILDSTATUS_TEMPLATE));
			setBuildIsPersonal(sBuild.isPersonal());
		}
		
		private boolean includeVcsFileList() {
			// Firstly update the "maxChangeFileListSize" value from webhook config or build parameters.
			if (extraParameters.containsKey(MAX_CHANGE_FILE_LIST_SIZE)) {
				try {
					this.maxChangeFileListSize = Integer.parseInt(extraParameters.get(MAX_CHANGE_FILE_LIST_SIZE));
				} catch (NumberFormatException ex) {
					Loggers.SERVER.info("WebHookPayloadContent : Unable to convert 'maxChangeFileListSize' value to a valid integer. Defaut value will be used '" + this.maxChangeFileListSize + "'");
				}
			} else if (teamcityProperties.containsKey(WEBHOOK_MAX_CHANGE_FILE_LIST_SIZE)) {
				try {
					this.maxChangeFileListSize = Integer.parseInt(teamcityProperties.get(WEBHOOK_MAX_CHANGE_FILE_LIST_SIZE));
				} catch (NumberFormatException ex) {
					Loggers.SERVER.info("WebHookPayloadContent : Unable to convert 'webhook.maxChangeFileListSize' value to a valid integer. Defaut value will be used '" + this.maxChangeFileListSize + "'");
				}
			} else if (Objects.nonNull(TeamCityProperties.getPropertyOrNull(WEBHOOK_MAX_CHANGE_FILE_LIST_SIZE))){
				try {
					this.maxChangeFileListSize = Integer.parseInt(TeamCityProperties.getProperty(WEBHOOK_MAX_CHANGE_FILE_LIST_SIZE));
				} catch (NumberFormatException ex) {
					Loggers.SERVER.info("WebHookPayloadContent : Unable to convert TeamCity global property 'webhook.maxChangeFileListSize' value to a valid integer. Defaut value will be used '" + this.maxChangeFileListSize + "'");
				}
			}
			
			// If the value is negative, checking is disabled and maxChangeFileListSize is effectively unlimited.
			if (this.maxChangeFileListSize < 0) {
				this.maxChangeFileListCountExceeded = false;
				return true;
				
			// Or calculate that the count we found is less then the preferred one.				
			} else if (this.changeFileListCount > this.maxChangeFileListSize) { 
				this.maxChangeFileListCountExceeded = true;
				return false;
			}
			
			return true;
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
		
		private void setChanges(List<SVcsModification> modifications, boolean includeVcsFileModifications){
			this.changes = WebHooksChangeBuilder.build(modifications, includeVcsFileModifications);
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

		public Branch getBranch() {
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
						this.derivedBuildEventType = BuildStateEnum.BUILD_SUCCESSFUL;
					} else {
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_FIXED;
						this.derivedBuildEventType = BuildStateEnum.BUILD_FIXED;

					}
				} else {
					this.buildResult = WebHookPayload.BUILD_STATUS_FAILURE;
					if (this.buildResultPrevious.equals(this.buildResult)){
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_NO_CHANGE;
						this.derivedBuildEventType = BuildStateEnum.BUILD_FAILED;

					} else {
						this.buildResultDelta = WebHookPayload.BUILD_STATUS_BROKEN;
						this.derivedBuildEventType = BuildStateEnum.BUILD_BROKEN;

					}
				}
			} else if (buildState == BuildStateEnum.BUILD_INTERRUPTED ) {
				this.buildResult = WebHookPayload.BUILD_STATUS_INTERRUPTED;
				this.buildResultDelta = WebHookPayload.BUILD_STATUS_UNKNOWN;
				this.derivedBuildEventType = BuildStateEnum.BUILD_INTERRUPTED;

			} else if (buildState == BuildStateEnum.BUILD_PINNED || buildState == BuildStateEnum.BUILD_UNPINNED) {
				this.derivedBuildEventType = buildState;			
			} else {
				this.buildResult = WebHookPayload.BUILD_STATUS_RUNNING;
				this.buildResultDelta = WebHookPayload.BUILD_STATUS_UNKNOWN;
				this.derivedBuildEventType = buildState;
			}
			
		}

		// Getters and setters
		
		public String getBuildStatus() {
			return buildStatus;
		}

		public void setBuildStatus(String buildStatus, BuildStateEnum buildState) {
			this.buildStatus = "Running".equalsIgnoreCase(buildStatus) 
					&& BuildStateEnum.BUILD_FINISHED.equals(buildState)
					&& Objects.nonNull(derivedBuildEventType)
					
				? this.derivedBuildEventType.getBuildStatusDescription()
				: buildStatus;
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

		
		private void setBuildStatusHtml(VariableResolverFactory variableResolverFactory, final String htmlStatusTemplate) {
			
			VariableMessageBuilder builder = variableResolverFactory.createVariableMessageBuilder(
						variableResolverFactory.buildVariableResolver(
									this.getProject(), new SimpleSerialiser(), this, getAllParameters()
								)
					);
			this.buildStatusHtml = builder.build(htmlStatusTemplate);
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
		
		public String getPinEventUsername() {
			return pinEventUsername;
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
		
		public ExtraParameters getAllParameters(){
			return this.extraParameters;
		}

		public ExtraParameters getExtraParameters(VariableResolverFactory variableResolverFactory) {
			if (!this.extraParameters.isEmpty()){
				VariableMessageBuilder builder;
				VariableResolver resolver = variableResolverFactory.buildVariableResolver(getProject(), new SimpleSerialiser(), this, getAllParameters());
				ExtraParameters resolvedParametersMap = new ExtraParameters();

				builder = variableResolverFactory.createVariableMessageBuilder(resolver);
				for (Entry<String,String> entry  : extraParameters.getEntriesAsSet()){
					resolvedParametersMap.put(entry.getKey(), builder.build(entry.getValue()));
				}
				resolver = new WebHooksBeanUtilsVariableResolver(getProject(), new SimpleSerialiser(),this, getAllParameters(), null);
				builder = variableResolverFactory.createVariableMessageBuilder(resolver);
				for (Entry<String,String> entry  : extraParameters.getEntriesAsSet()){
					resolvedParametersMap.put(entry.getKey(), builder.build(entry.getValue()));
				}
				return resolvedParametersMap;
			} else {
				return null;
			}
		}

		public void setExtraParameters(SortedMap<String, String> extraParameters) {
			this.extraParameters = new ExtraParameters(extraParameters);
		}

		public static class SimpleSerialiser implements WebHookContentObjectSerialiser {

			@Override
			public Object serialiseObject(Object object) {
				return object;
			}
		}
}
