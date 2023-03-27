package webhook.teamcity.executor;

import java.util.Map;

import jetbrains.buildServer.serverSide.SBuild;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookConfig;

public class BuildEventWebHookRunner extends AbstractWebHookExecutor implements WebHookRunner {
	
	private final SBuild sBuild;
	private String username;
	private String comment;
    private Map<String, String> extraAttributes;
	
	public BuildEventWebHookRunner(
			WebHookContentBuilder webHookContentBuilder, 
			WebHookHistoryRepository webHookHistoryRepository,
			WebHookHistoryItemFactory webHookHistoryItemFactory, 
			WebHookConfig whc, 
			BuildStateEnum state,
			String username,
			String comment,
			boolean overrideIsEnabled, 
			WebHook webhook, 
			SBuild sBuild,
			boolean isTest, 
			Map<String, String> extraAttributes) 
	{
		super(
			  webHookContentBuilder, 
			  webHookHistoryRepository, 
			  webHookHistoryItemFactory, 
			  whc, 
			  state,
			  overrideIsEnabled, 
			  webhook,
			  isTest);
		this.sBuild = sBuild;
		this.username = username;
		this.comment = comment;
		this.extraAttributes = extraAttributes;
	}
	
	@Override
	protected WebHook getWebHookContent() {
		return webHookContentBuilder.buildWebHookContent(webhook, whc, sBuild, state, username, comment, overrideIsEnabled, extraAttributes);
	}
	
	@Override
	public WebHookHistoryItem buildWebHookHistoryItem(WebHookErrorStatus errorStatus) {
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
