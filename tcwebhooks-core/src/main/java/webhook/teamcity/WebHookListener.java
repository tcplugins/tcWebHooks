package webhook.teamcity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.responsibility.TestNameResponsibilityEntry;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import webhook.WebHook;
import webhook.teamcity.WebHookSettingsEventHandler.WebHookSettingsEventImpl;
import webhook.teamcity.executor.WebHookExecutor;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.executor.WebHookStatisticsExecutor;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.statistics.StatisticsReport;
import webhook.teamcity.statistics.WebHooksStatisticsReportEventListener;


/**
 * WebHookListner
 * Listens for Server events and then triggers the execution of webhooks if configured.
 */
public class WebHookListener extends BuildServerAdapter implements WebHooksStatisticsReportEventListener {
	private static final Logger LOG = Logger.getInstance(WebHookListener.class.getName());
	private static final String WEB_HOOK_LISTENER = "WebHookListener :: ";
	private static final String ABOUT_TO_PROCESS_WEB_HOOKS_FOR = "About to process WebHooks for ";
	private static final String AT_BUILD_STATE = " at buildState ";
	public static final String WEBHOOKS_SETTINGS_ATTRIBUTE_NAME = "webhooks";
	private final SBuildServer myBuildServer;
	private final WebHookSettingsManager mySettings;
	private final WebHookMainSettings myMainSettings;
	private final WebHookTemplateManager myManager;
	private final WebHookFactory webHookFactory;
	private final WebHookExecutor webHookExecutor;
	private final WebHookStatisticsExecutor webHookStatisticsExecutor;
	private final WebHookSettingsEventHandler webHookSettingsEventHandler;


	public WebHookListener(SBuildServer sBuildServer, WebHookSettingsManager settings,
							WebHookMainSettings configSettings, WebHookTemplateManager manager,
							WebHookFactory factory, WebHookExecutor executor,
							WebHookStatisticsExecutor statisticsExecutor,
							WebHookSettingsEventHandler settingsEventHandler) {

		myBuildServer = sBuildServer;
		mySettings = settings;
		myMainSettings = configSettings;
		myManager = manager;
		webHookFactory = factory;
		webHookExecutor = executor;
		webHookStatisticsExecutor = statisticsExecutor;
		webHookSettingsEventHandler = settingsEventHandler;

		LOG.info(WEB_HOOK_LISTENER + "Starting");
	}

	public void register(){
		myBuildServer.addListener(this);
		LOG.debug(WEB_HOOK_LISTENER + "Registering");
	}

	private void processBuildEvent(SBuild sBuild, BuildStateEnum state, Map<String, String> serviceMessageAttributes) {

		LOG.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + sBuild.getProjectId() + AT_BUILD_STATE + state.getShortName());
		for (WebHookConfig whc : getListOfEnabledWebHooks(state, sBuild.getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			webHookExecutor.execute(wh, whc, sBuild, state, null, null, false, serviceMessageAttributes);
		}
	}
	private void processQueueEvent(SQueuedBuild sBuild, BuildStateEnum state, String user, String comment) {

		LOG.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + sBuild.getBuildType().getProjectId() + AT_BUILD_STATE + state.getShortName());
		for (WebHookConfig whc : getListOfEnabledWebHooks(state, sBuild.getBuildType().getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			webHookExecutor.execute(wh, whc, sBuild, state, user, comment, false);
		}
	}

	private void processResponsibilityEvent(BuildStateEnum state, WebHookResponsibilityHolder responsibilityHolder) {

		LOG.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + responsibilityHolder.getSProject().getProjectId() + AT_BUILD_STATE + state.getShortName());
		for (WebHookConfig whc : getListOfEnabledWebHooks(state, responsibilityHolder.getSProject().getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			webHookExecutor.execute(wh, whc, state, responsibilityHolder, false);
		}
	}

	private void processPinEvent(SBuild sBuild, BuildStateEnum state, String user, String comment) {

		LOG.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + sBuild.getBuildType().getProjectId() + AT_BUILD_STATE + state.getShortName());
		for (WebHookConfig whc : getListOfEnabledWebHooks(state, sBuild.getBuildType().getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			webHookExecutor.execute(wh, whc, sBuild, state, user, comment, false, Collections.emptyMap());
		}
	}

	private void processTestEvent(BuildStateEnum state, @Nullable SUser user, Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups) {
		if (!mutedOrUnmutedGroups.keySet().isEmpty()) {
			Set<SProject> projects = new TreeSet<>();
			for(MuteInfo unmuted : mutedOrUnmutedGroups.keySet()) {
				for (STest t: unmuted.getTests()) {
					projects.add(this.myBuildServer.getProjectManager().findProjectById(t.getProjectId()));
				}
			}
			for (SProject project : projects) {
				LOG.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + project.getProjectId()+ AT_BUILD_STATE + state.getShortName());
				for (WebHookConfig whc : getListOfEnabledWebHooks(state, project.getProjectId())){
					WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
					webHookExecutor.execute(wh, whc, project, mutedOrUnmutedGroups, state, user, false);
				}
			}
		}
	}


	/**
	 * Build a list of Enabled webhooks to pass to the POSTing logic.
	 * @param projectId
	 * @return
	 */
	private List<WebHookConfig> getListOfEnabledWebHooks(BuildStateEnum state, String projectId) {
		List<WebHookConfig> configs = new ArrayList<>();
		List<SProject> projects = new ArrayList<>();
		SProject myProject = myBuildServer.getProjectManager().findProjectById(projectId);
		projects.addAll(myProject.getProjectPath());
		for (SProject project : projects){
			WebHookProjectSettings projSettings = mySettings.getSettings(project.getProjectId());
			if (projSettings.isEnabled()){
				for (WebHookConfig whc : projSettings.getWebHooksConfigs()){
					if (!whc.isEnabledForSubProjects().booleanValue() && !myProject.getProjectId().equals(project.getProjectId())){
						// Sub-projects are disabled and we are a subproject.
						if (LOG.isDebugEnabled()){
							LOG.debug(this.getClass().getSimpleName() + ":getListOfEnabledWebHooks() "
									+ ":: subprojects not enabled. myProject is: " + myProject.getProjectId() + ". webhook project is: " + project.getProjectId()+ " : " + whc.getUniqueKey());
						}
						continue;
					}

					if (whc.getEnabled().booleanValue() && whc.isEnabledForBuildState(state)){
						if (myManager.isRegisteredTemplate(whc.getPayloadTemplate())){
							whc.setProjectExternalId(project.getExternalId());
							whc.setProjectInternalId(project.getProjectId());
							configs.add(whc);
							LOG.debug("WebHookListener :: WebHook added to list of enabled webhooks. Enabled for " + state.toString() + ".  " + whc.getUrl() + " (" + whc.getPayloadTemplate() + ")" + " : " + whc.getUniqueKey());
						} else {
							LOG.warn("WebHookListener :: No registered Template: " + whc.getPayloadTemplate() + " for " + whc.getUniqueKey());
						}
					} else if (whc.getEnabled().booleanValue() && !whc.isEnabledForBuildState(state)) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("WebHookListener :: WebHook skipped. Not enabled for " + state.toString() + ".  " + whc.getUrl() + " (" + whc.getPayloadTemplate() + ")" + " : " + whc.getUniqueKey());
						}
					} else {
						LOG.debug(this.getClass().getSimpleName()
								+ ":processBuildEvent() :: WebHook disabled. Will not process " + whc.getUrl() + " (" + whc.getPayloadTemplate() + ")" + " : " + whc.getUniqueKey());
					}
				}
			} else {
				LOG.debug("WebHookListener :: WebHooks are disasbled for  " + projectId);
			}
		}
		return configs;
	}

	@Override
	public void buildStarted(SRunningBuild sRunningBuild){
		processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_STARTED, Collections.emptyMap());
	}

	@Override
	public void changesLoaded(SRunningBuild sRunningBuild){
		processBuildEvent(sRunningBuild, BuildStateEnum.CHANGES_LOADED, Collections.emptyMap());
	}

	@Override
	public void buildFinished(SRunningBuild sRunningBuild){
		processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_FINISHED, Collections.emptyMap());
	}

	@Override
	public void buildInterrupted(SRunningBuild sRunningBuild) {
		processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_INTERRUPTED, Collections.emptyMap());
	}

	@Override
	public void beforeBuildFinish(SRunningBuild sRunningBuild) {
		processBuildEvent(sRunningBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, Collections.emptyMap());
	}

	/**
	 * Called when responsibility for several tests at once is changed. <br>
	 * Some events may be omitted when the responsibility was changed on another node,
	 * i.e. it's not called on read-only node when the responsibility was assigned and
	 * immediately removed on the main server.
	 *
	 * @since 6.0
	 */
	@Override
	public void responsibleChanged(
			SProject project,
			Collection<TestName> testNames,
			ResponsibilityEntry entry,
			boolean isUserAction) {

		processResponsibilityEvent(
				BuildStateEnum.RESPONSIBILITY_CHANGED,
				WebHookResponsibilityHolder
					.builder()
					.state(entry.getState())
					.responsibilityEntryNew(entry)
					.sProject(project)
					.testNames(testNames)
					.isUserAction(isUserAction)
					.build()
				);
	}

	/**
	 * New version of responsibleChanged, which has some bugfixes, but
	 * is only available in versions 7.0 and above.
	 * @param bt
	 * @param oldValue
	 * @param newValue
	 * @since 7.0
	 */
	@Override
	public void responsibleChanged(@NotNull SBuildType sBuildType,
			@NotNull ResponsibilityEntry responsibilityEntryOld,
			@NotNull ResponsibilityEntry responsibilityEntryNew) {

		processResponsibilityEvent(
				BuildStateEnum.RESPONSIBILITY_CHANGED,
				WebHookResponsibilityHolder
					.builder()
					.sBuildType(sBuildType)
					.sProject(sBuildType.getProject())
					.responsibilityEntryOld(responsibilityEntryOld)
					.responsibilityEntryNew(responsibilityEntryNew)
					.build()
				);
	}

	/**
	 * Called when responsibility for several build problems at once is changed.<br>
	 * Some events may be omitted when the responsibility was changed on another
	 * node, i.e. it's not called on read-only node when the responsibility was
	 * assigned and immediately removed on the main server.
	 *
	 * @since 8.0
	 */
	@Override
	public void responsibleChanged(
			@NotNull SProject project,
			@NotNull Collection<BuildProblemInfo> buildProblems,
			@Nullable ResponsibilityEntry entry)
	{
		processResponsibilityEvent(
				BuildStateEnum.RESPONSIBILITY_CHANGED,
				WebHookResponsibilityHolder
					.builder()
					.sProject(project)
					.buildProblems(buildProblems)
					.responsibilityEntryNew(entry)
					.build()
			);
	}

/**
	* Called when responsibility for several tests at once is changed.
	* @param project the project
	* @param entry the new responsibility entry for each test
	* @since 7.0
	*/
	@Override
	public void responsibleRemoved(
			SProject project,
			TestNameResponsibilityEntry entry)
	{
		processResponsibilityEvent(
				BuildStateEnum.RESPONSIBILITY_CHANGED,
				WebHookResponsibilityHolder
					.builder()
					.state(ResponsibilityEntry.State.FIXED)
					.sProject(project)
					.testNameResponsibilityEntry(entry)
					.build()
			);
	}

	/**
	 * Support for listening to events where a user has modified a build result
	 * and "marked as successful" or "marked as failed".
	 *
	 * Checks if the list of problems has gone to zero (from greater than zero)
	 * or vice versa. If so, it fires the finished event.
	 */
	@Override
	public void buildProblemsChanged(SBuild build, List<BuildProblemData> before, List<BuildProblemData> after) {
		if (build instanceof SFinishedBuild
				&& (
					! before.isEmpty() &&   after.isEmpty()		// Problems count changed to zero (muted)
					|| before.isEmpty() && ! after.isEmpty() 		// Problems count changed from zero to greater than zero.
				)
			)
		{
			this.processBuildEvent(build, BuildStateEnum.BUILD_FINISHED, Collections.emptyMap());
		}

	}

	@Override
	public void buildTypeAddedToQueue(SQueuedBuild queuedBuild) {
		this.processQueueEvent(queuedBuild, BuildStateEnum.BUILD_ADDED_TO_QUEUE, null, null);
	}

	@Override
	public void buildRemovedFromQueue(SQueuedBuild queuedBuild, User user, String comment) {
		// Only send a webhook if the build was actively removed from the queue by a user.
		if (user != null) {
			this.processQueueEvent(queuedBuild, BuildStateEnum.BUILD_REMOVED_FROM_QUEUE, user.getUsername(), comment);
		}
	}

	@Override
	public void buildPinned(SBuild build, User user, String comment) {
		this.processPinEvent(
				build,
				BuildStateEnum.BUILD_PINNED,
				user != null ? user.getUsername() : null,
				comment);
	}

	@Override
	public void buildUnpinned(SBuild build, User user, String comment) {
		this.processPinEvent(
				build,
				BuildStateEnum.BUILD_UNPINNED,
				user != null ? user.getUsername() : null,
				comment);
	}

	@Override
	public void serverStartup() {
		mySettings.initialise();
	}
	
	@Override
	public void projectRestored(String projectId) {
	    LOG.debug("WebHookListener :: Handling projectRestored event for project: " + projectId);
	    this.webHookSettingsEventHandler.handleEvent(new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_CHANGED, projectId, null, null));
	}
	
	@Override
	public void projectMoved(SProject project, SProject originalParentProject) {
	    LOG.debug("WebHookListener :: Handling projectMoved event for project: " + project.getProjectId());
	    this.webHookSettingsEventHandler.handleEvent(new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_CHANGED, project.getProjectId(), null, null));
	}
	
	@Override
	public void projectExternalIdChanged(SProject project, java.lang.String oldExternalId, java.lang.String newExternalId) {
	    LOG.debug("WebHookListener :: Handling projectExternalIdChanged event for project: " + project.getProjectId());
	    this.mySettings.handleProjectChangedEvent(new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_CHANGED, project.getProjectId(), null, null));
	}
	
	@Override
	public void projectPersisted(String projectId) {
        LOG.debug("WebHookListener :: Handling projectPersisted event for project: " + projectId);
        this.webHookSettingsEventHandler.handleEvent(new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_PERSISTED, projectId, null, null));
	}
	
	@Override
	public void projectRemoved(SProject project) {
	    LOG.debug("WebHookListener :: Handling projectRemoved event for project: " + project.getProjectId());
	    this.mySettings.removeAllWebHooksFromCacheForProject(project.getProjectId());
	}
	
	@Override
	public void projectArchived(String projectId) {
	    LOG.debug("WebHookListener :: Handling projectArchived event for project: " + projectId);
	    this.mySettings.removeAllWebHooksFromCacheForProject(projectId);
	}
	
	@Override
	public void projectDearchived(String projectId) {
	    LOG.debug("WebHookListener :: Handling projectDearchived event for project: " + projectId);
	    this.webHookSettingsEventHandler.handleEvent(new WebHookSettingsEventImpl(WebHookSettingsEventType.PROJECT_CHANGED, projectId, null, null));
	}
	
	@Override
	public void buildTypeUnregistered(SBuildType buildType) {
	    LOG.debug(String.format("WebHookListener :: Handling buildTypeUnregistered event for buildType: %s (%s)", buildType.getExternalId(), buildType.getInternalId()));
	    this.webHookSettingsEventHandler.handleEvent(new WebHookSettingsEventImpl(WebHookSettingsEventType.BUILD_TYPE_DELETED, buildType.getProjectId(), buildType.getBuildTypeId(), buildType));
	}
	
	@Override
	public void reportStatistics(WebHookConfig reportingWebhookConfig, StatisticsReport statisticsReport) {
		WebHook reportingWebhook = webHookFactory.getWebHook(reportingWebhookConfig, myMainSettings.getProxyConfigForUrl(reportingWebhookConfig.getUrl()));
		reportingWebhook.setEnabledForBuildState(BuildStateEnum.REPORT_STATISTICS, reportingWebhook.isEnabled());
		webHookStatisticsExecutor.execute(reportingWebhook, reportingWebhookConfig, BuildStateEnum.REPORT_STATISTICS, statisticsReport, myBuildServer.getProjectManager().getRootProject(), false);
	}

	@Override
	public void reportStatistics(StatisticsReport statisticsReport) {
		for (WebHookConfig whc : getListOfEnabledWebHooks(BuildStateEnum.REPORT_STATISTICS, myBuildServer.getProjectManager().getRootProject().getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			if (Boolean.TRUE.equals(wh.isEnabled()) && wh.getBuildStates().enabled(BuildStateEnum.REPORT_STATISTICS)) {
				webHookStatisticsExecutor.execute(wh, whc, BuildStateEnum.REPORT_STATISTICS, statisticsReport, myBuildServer.getProjectManager().getRootProject(), false);
			}
		}

	}

	public void serviceMessageReceived(SRunningBuild runningBuild, Map<String, String> serviceMessageAttributes) {
		this.processBuildEvent(runningBuild, BuildStateEnum.SERVICE_MESSAGE_RECEIVED, serviceMessageAttributes);
	}

	@Override
	public void testsMuted(MuteInfo muteInfo) {
	    this.processTestEvent(BuildStateEnum.TESTS_MUTED, null, Collections.singletonMap(muteInfo, muteInfo.getTests()));
	}

	@Override
	public void testsUnmuted(SUser user, Map<MuteInfo, Collection<STest>> unmutedGroups) {
		this.processTestEvent(BuildStateEnum.TESTS_UNMUTED, user, unmutedGroups);
	}

}
