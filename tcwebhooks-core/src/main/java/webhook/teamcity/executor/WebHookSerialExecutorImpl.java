package webhook.teamcity.executor;

import java.util.Collection;
import java.util.Map;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.users.SUser;
import lombok.AllArgsConstructor;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.statistics.StatisticsReport;

@AllArgsConstructor
public class WebHookSerialExecutorImpl implements WebHookSerialExecutor, WebHookStatisticsExecutor {
	
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
	public void execute(WebHook webhook, WebHookConfig whc, BuildStateEnum state,
			WebHookResponsibilityHolder responsibilityHolder, boolean isTest) {
		
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		webHookRunnerFactory.getRunner(webhook, whc, state, responsibilityHolder, isTest).run();
		
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, String username,
			String comment, boolean isTest, Map<String,String> extraAttributes) 
	{
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		webHookRunnerFactory.getRunner(webhook, whc, sBuild, state, username, comment, isTest, extraAttributes).run();
		
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, BuildStateEnum state, 
			StatisticsReport report, SProject rootProject, boolean isTest) {
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

		webHookRunnerFactory.getRunner(webhook, whc, state, report, rootProject, isTest).run();
		
		Loggers.SERVER.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " + 
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());
	}

    @Override
    public void execute(WebHook webhook, WebHookConfig whc, SProject sProject,
            Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups, BuildStateEnum state, SUser user, boolean isTest) {
        Loggers.SERVER.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " + 
                webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());

        webHookRunnerFactory.getRunner(webhook, whc, sProject, state, mutedOrUnmutedGroups, isTest).run();
        
        Loggers.SERVER.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " + 
                webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey());    }
	
}
