package webhook.teamcity.executor;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import lombok.AllArgsConstructor;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.settings.WebHookConfig;

@AllArgsConstructor
public class WebHookThreadingExecutorImpl implements WebHookThreadingExecutor {
	
    private final WebHookRunnerFactory webHookRunnerFactory;
    private final ExecutorServices executorServices;

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SQueuedBuild sQueuedBuild, 
						BuildStateEnum state, String user, String comment, boolean isTest) 
	{
		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: About to schedule runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
		
		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, sQueuedBuild, state, user, comment, isTest);
		executorServices.getNormalExecutorService().execute(runner);
		
		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: Finished scheduling runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SBuildType sBuildType, 
			ResponsibilityEntry responsibilityEntryOld,	ResponsibilityEntry responsibilityEntryNew, boolean isTest) 
	{
		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: About to schedule runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
		
		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, sBuildType, responsibilityEntryOld, responsibilityEntryNew, isTest);
		executorServices.getNormalExecutorService().execute(runner);
		
		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: Finished scheduling runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, boolean isTest) 
	{
		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: About to schedule runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
		
		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, sBuild, state, isTest);
		executorServices.getNormalExecutorService().execute(runner);
		
		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: Finished scheduling runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}
	
}
