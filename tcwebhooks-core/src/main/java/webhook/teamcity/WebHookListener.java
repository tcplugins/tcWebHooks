package webhook.teamcity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import jetbrains.buildServer.serverSide.problems.BuildProblemInfo;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.tests.TestName;
import jetbrains.buildServer.users.User;
import webhook.WebHook;
import webhook.teamcity.executor.WebHookExecutor;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;


/**
 * WebHookListner
 * Listens for Server events and then triggers the execution of webhooks if configured.
 */
public class WebHookListener extends BuildServerAdapter {
    
	private static final String ABOUT_TO_PROCESS_WEB_HOOKS_FOR = "About to process WebHooks for ";
	private static final String WEB_HOOK_LISTENER = "WebHookListener :: ";
	public static final String WEBHOOKS_SETTINGS_ATTRIBUTE_NAME = "webhooks";
	private final SBuildServer myBuildServer;
    private final ProjectSettingsManager mySettings;
    private final WebHookMainSettings myMainSettings;
    private final WebHookPayloadManager myManager;
    private final WebHookFactory webHookFactory;
    private final WebHookExecutor webHookExecutor;
    
    
    public WebHookListener(SBuildServer sBuildServer, ProjectSettingsManager settings, 
    						WebHookMainSettings configSettings, WebHookPayloadManager manager,
    						WebHookFactory factory, WebHookTemplateResolver resolver,
    						WebHookContentBuilder contentBuilder, WebHookHistoryRepository historyRepository,
    						WebHookHistoryItemFactory historyItemFactory,
    						WebHookExecutor executor) {

        myBuildServer = sBuildServer;
        mySettings = settings;
        myMainSettings = configSettings;
        myManager = manager;
        webHookFactory = factory;
        webHookExecutor = executor;
        
        Loggers.SERVER.info(WEB_HOOK_LISTENER + " :: Starting");
    }
    
    public void register(){
        myBuildServer.addListener(this);
        Loggers.SERVER.info(WEB_HOOK_LISTENER + " :: Registering");
    }

	private void processBuildEvent(SBuild sBuild, BuildStateEnum state) {
			
		Loggers.SERVER.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + sBuild.getProjectId() + " at buildState " + state.getShortName());
		for (WebHookConfig whc : getListOfEnabledWebHooks(sBuild.getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			webHookExecutor.execute(wh, whc, sBuild, state, null, null, false);
    	}
	}
	private void processQueueEvent(SQueuedBuild sBuild, BuildStateEnum state, String user, String comment) {
		
		Loggers.SERVER.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + sBuild.getBuildType().getProjectId() + " at buildState " + state.getShortName());
		for (WebHookConfig whc : getListOfEnabledWebHooks(sBuild.getBuildType().getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			webHookExecutor.execute(wh, whc, sBuild, state, user, comment, false);
		}
	}
	
	private void processResponsibilityEvent(BuildStateEnum state, WebHookResponsibilityHolder responsibilityHolder) {
		
		Loggers.SERVER.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + responsibilityHolder.getSProject().getProjectId() + " at buildState " + state.getShortName());
		for (WebHookConfig whc : getListOfEnabledWebHooks(responsibilityHolder.getSProject().getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			webHookExecutor.execute(wh, whc, state, responsibilityHolder, false);
		}
	}
	
	private void processPinEvent(SBuild sBuild, BuildStateEnum state, String user, String comment) {
		
		Loggers.SERVER.debug(ABOUT_TO_PROCESS_WEB_HOOKS_FOR + sBuild.getBuildType().getProjectId() + " at buildState " + state.getShortName());
		for (WebHookConfig whc : getListOfEnabledWebHooks(sBuild.getBuildType().getProjectId())){
			WebHook wh = webHookFactory.getWebHook(whc, myMainSettings.getProxyConfigForUrl(whc.getUrl()));
			webHookExecutor.execute(wh, whc, sBuild, state, user, comment, false);
		}
	}

	/** 
	 * Build a list of Enabled webhooks to pass to the POSTing logic.
	 * @param projectId
	 * @return
	 */
	private List<WebHookConfig> getListOfEnabledWebHooks(String projectId) {
		List<WebHookConfig> configs = new ArrayList<>();
		List<SProject> projects = new ArrayList<>();
		SProject myProject = myBuildServer.getProjectManager().findProjectById(projectId);
		projects.addAll(myProject.getProjectPath());
		for (SProject project : projects){
			WebHookProjectSettings projSettings = (WebHookProjectSettings) mySettings.getSettings(project.getProjectId(), WEBHOOKS_SETTINGS_ATTRIBUTE_NAME);
	    	if (projSettings.isEnabled()){
		    	for (WebHookConfig whc : projSettings.getWebHooksConfigs()){
		    		if (whc.isEnabledForSubProjects() == false && !myProject.getProjectId().equals(project.getProjectId())){
		    			// Sub-projects are disabled and we are a subproject.
		    			if (Loggers.ACTIVITIES.isDebugEnabled()){
			    			Loggers.ACTIVITIES.debug(this.getClass().getSimpleName() + ":getListOfEnabledWebHooks() "
			    					+ ":: subprojects not enabled. myProject is: " + myProject.getProjectId() + ". webhook project is: " + project.getProjectId());
		    			}
		    			continue;
		    		}
		    		
		    		if (whc.getEnabled()){
						if (myManager.isRegisteredFormat(whc.getPayloadFormat())){
							whc.setProjectExternalId(project.getExternalId());
							whc.setProjectInternalId(project.getProjectId());
							configs.add(whc);
						} else {
							Loggers.ACTIVITIES.warn("WebHookListener :: No registered Payload Handler for " + whc.getPayloadFormat());
						}
		    		} else {
		    			Loggers.ACTIVITIES.debug(this.getClass().getSimpleName() 
		    					+ ":processBuildEvent() :: WebHook disabled. Will not process " + whc.getUrl() + " (" + whc.getPayloadFormat() + ")");
		    		}
				}
	    	} else {
	    		Loggers.ACTIVITIES.debug("WebHookListener :: WebHooks are disasbled for  " + projectId);
	    	}
		}
    	return configs;
	}

	@Override
    public void buildStarted(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_STARTED);
    }	
	
	@Override
	public void changesLoaded(SRunningBuild sRunningBuild){
		processBuildEvent(sRunningBuild, BuildStateEnum.CHANGES_LOADED);
	}	
	
    @Override
    public void buildFinished(SRunningBuild sRunningBuild){
    	processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_FINISHED);
    }    

    @Override
    public void buildInterrupted(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, BuildStateEnum.BUILD_INTERRUPTED);
    }      

    @Override
    public void beforeBuildFinish(SRunningBuild sRunningBuild) {
    	processBuildEvent(sRunningBuild, BuildStateEnum.BEFORE_BUILD_FINISHED);
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
			this.processBuildEvent(build, BuildStateEnum.BUILD_FINISHED);
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
			String username = user != null ? user.getUsername() : null;
			this.processQueueEvent(queuedBuild, BuildStateEnum.BUILD_REMOVED_FROM_QUEUE, username, comment);
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
	
}
