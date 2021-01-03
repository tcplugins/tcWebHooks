package webhook.teamcity.executor;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SProject;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.statistics.StatisticsReport;

public interface WebHookStatisticsExecutor {
	
	public void execute(
			@NotNull WebHook webHook,
			@NotNull WebHookConfig whc,
			@NotNull BuildStateEnum state,
			@NotNull StatisticsReport report,
			@NotNull SProject rootProject, 
			boolean isTest
		);

}
