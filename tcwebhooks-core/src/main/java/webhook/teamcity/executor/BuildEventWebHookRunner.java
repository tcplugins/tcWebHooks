package webhook.teamcity.executor;

import jetbrains.buildServer.serverSide.SBuild;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.WebHookConfig;

public class BuildEventWebHookRunner extends AbstractWebHookExecutor implements WebHookRunner {
	
	private final SBuild sBuild;
	
	public BuildEventWebHookRunner(
			WebHookPayloadManager webhookPayloadManager,
			WebHookContentBuilder webHookContentBuilder, 
			WebHookHistoryRepository webHookHistoryRepository,
			WebHookHistoryItemFactory webHookHistoryItemFactory, 
			WebHookConfig whc, 
			BuildStateEnum state,
			boolean overrideIsEnabled, 
			WebHook webhook, 
			SBuild sBuild,
			boolean isTest) 
	{
		super(webhookPayloadManager, 
			  webHookContentBuilder, 
			  webHookHistoryRepository, 
			  webHookHistoryItemFactory, 
			  whc, 
			  state,
			  overrideIsEnabled, 
			  webhook,
			  isTest);
		this.sBuild = sBuild;
	}
	
	@Override
	protected WebHook getWebHookContent() {
		return webHookContentBuilder.buildWebHookContent(webhook, whc, sBuild, state, overrideIsEnabled);
	}
	
	@Override
	protected WebHookHistoryItem buildWebHookHistoryItem(WebHookErrorStatus errorStatus) {
		if (this.isTest) {
			return webHookHistoryItemFactory.getWebHookHistoryTestItem(
					whc,
					webhook.getExecutionStats(), 
					sBuild,
					errorStatus
				);			
		} else {
			return webHookHistoryItemFactory.getWebHookHistoryItem(
					whc,
					webhook.getExecutionStats(), 
					sBuild,
					errorStatus
				);
		}
	}

}
