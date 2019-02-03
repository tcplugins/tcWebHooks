package webhook.teamcity.executor;

import jetbrains.buildServer.responsibility.ResponsibilityEntry;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import lombok.AllArgsConstructor;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.settings.WebHookConfig;

@AllArgsConstructor
public class WebHookSerialExecutorImpl implements WebHookSerialExecutor {
	
    private final WebHookRunnerFactory webHookRunnerFactory;

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SQueuedBuild sQueuedBuild, 
						BuildStateEnum state, String user, String comment, boolean isTest) 
	{
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		webHookRunnerFactory.getRunner(webhook, whc, sQueuedBuild, state, user, comment, isTest).run();
		
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SBuildType sBuildType, 
			ResponsibilityEntry responsibilityEntryOld,	ResponsibilityEntry responsibilityEntryNew, boolean isTest) 
	{
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		webHookRunnerFactory.getRunner(webhook, whc, sBuildType, responsibilityEntryOld, responsibilityEntryNew, isTest).run();
		
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, boolean isTest) 
	{
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		webHookRunnerFactory.getRunner(webhook, whc, sBuild, state, isTest).run();
		
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}
	
}
