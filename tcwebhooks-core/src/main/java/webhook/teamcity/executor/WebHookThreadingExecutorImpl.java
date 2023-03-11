package webhook.teamcity.executor;

import java.util.Collection;
import java.util.Map;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.users.SUser;
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
	public void execute(WebHook webhook, WebHookConfig whc, BuildStateEnum state, WebHookResponsibilityHolder responsibilityHolder,
			boolean isTest) {
		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: About to schedule runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, state, responsibilityHolder, isTest);
		executorServices.getNormalExecutorService().execute(runner);

		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: Finished scheduling runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, String user, String comment,
			boolean isTest, Map<String,String> extraAttributes) {
		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: About to schedule runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, sBuild, state, user, comment, isTest, extraAttributes);
		executorServices.getNormalExecutorService().execute(runner);

		Loggers.SERVER.debug("WebHookThreadingExecutorImpl :: Finished scheduling runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SProject sProject,
			Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups, BuildStateEnum state, SUser user, boolean isTest) {

		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, sProject, state, mutedOrUnmutedGroups, isTest);
		executorServices.getNormalExecutorService().execute(runner);


		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

	}

}
