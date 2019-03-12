package webhook.teamcity.executor;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import lombok.AllArgsConstructor;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.WebHookConfig;

@AllArgsConstructor
public class WebHookRunnerFactory {
	
	private WebHookPayloadManager webHookPayloadManager;
	private WebHookContentBuilder webHookContentBuilder;
	private WebHookHistoryRepository webHookHistoryRepository;
	private WebHookHistoryItemFactory webHookHistoryItemFactory;

	public WebHookRunner getRunner(WebHook webhook, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, String username,
			String comment, boolean isTest) {
		return new BuildEventWebHookRunner(
				webHookPayloadManager, 
				webHookContentBuilder, 
				webHookHistoryRepository, 
				webHookHistoryItemFactory, 
				whc, 
				state,
				username,
				comment,
				isTest,	// Test enables override too.
				webhook,
				sBuild,
				isTest
			);
	}

	public WebHookRunner getRunner(
			WebHook webhook, WebHookConfig whc, BuildStateEnum state,
			WebHookResponsibilityHolder responsibilityHolder,
			boolean isTest) {
		return new ResponsibilityChangedWebHookRunner(
				webHookPayloadManager, 
				webHookContentBuilder, 
				webHookHistoryRepository, 
				webHookHistoryItemFactory, 
				whc,
				state,
				isTest,	// Test enables override too.
				webhook,
				responsibilityHolder,
				isTest
			);
	}

	public WebHookRunner getRunner(WebHook webhook, WebHookConfig whc, SQueuedBuild sQueuedBuild, BuildStateEnum state,
			String user, String comment, boolean isTest) {
		return new QueuedBuildWebHookRunner(
				webHookPayloadManager, 
				webHookContentBuilder, 
				webHookHistoryRepository, 
				webHookHistoryItemFactory, 
				whc, 
				state, 
				isTest,	// Test enables override too.
				webhook, 
				sQueuedBuild, 
				user, 
				comment,
				isTest
			);
	}

}
