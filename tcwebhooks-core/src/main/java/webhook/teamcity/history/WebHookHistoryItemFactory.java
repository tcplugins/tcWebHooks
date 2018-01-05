package webhook.teamcity.history;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.WebHookExecutionStats;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.settings.WebHookConfig;

public interface WebHookHistoryItemFactory {
	
	public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, 
													WebHookExecutionStats webHookExecutionStats, 
													SBuild sBuild, 
													WebHookErrorStatus errorStatus);

	public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, 
													WebHookExecutionStats executionStats,
													SBuildType sBuildType, 
													WebHookErrorStatus errorStatus);

	public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, 	
													WebHookExecutionStats executionStats,
													SProject project, 
													WebHookErrorStatus errorStatus);

}