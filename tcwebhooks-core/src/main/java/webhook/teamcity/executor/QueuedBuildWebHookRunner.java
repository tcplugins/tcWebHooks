package webhook.teamcity.executor;

import jetbrains.buildServer.serverSide.SQueuedBuild;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookConfig;

public class QueuedBuildWebHookRunner extends AbstractWebHookExecutor implements WebHookRunner {

    private final SQueuedBuild sQueuedBuild;
    private final String user;
	private final String comment;
	
	public QueuedBuildWebHookRunner(
			WebHookContentBuilder webHookContentBuilder,
			WebHookHistoryRepository webHookHistoryRepository,
			WebHookHistoryItemFactory webHookHistoryItemFactory,
			WebHookConfig whc,
			BuildStateEnum state,
			boolean isOverrideEnabled,
			WebHook webhook,
			SQueuedBuild sQueuedBuild,
			String user,
			String comment,
			boolean isTest) 
	{
		super (
			 webHookContentBuilder,
			 webHookHistoryRepository,
			 webHookHistoryItemFactory,
			 whc,
			 state,
			 isOverrideEnabled,
			 webhook,
			 isTest);
		this.sQueuedBuild = sQueuedBuild;
		this.user = user;
		this.comment = comment;
	}


	@Override
	protected WebHook getWebHookContent() {
		return webHookContentBuilder.buildWebHookContent(webhook, whc, sQueuedBuild, state, user, comment, overrideIsEnabled);
	}
	
	@Override
	protected WebHookHistoryItem buildWebHookHistoryItem(WebHookErrorStatus errorStatus) {
		if (this.isTest) {
			return webHookHistoryItemFactory.getWebHookHistoryTestItem(
					whc,
					webhook.getExecutionStats(), 
					sQueuedBuild.getBuildType(),
					errorStatus
			);
			
		} else {
			return webHookHistoryItemFactory.getWebHookHistoryItem(
					whc,
					webhook.getExecutionStats(), 
					sQueuedBuild.getBuildType(),
					errorStatus
			);
		}
	}

}
