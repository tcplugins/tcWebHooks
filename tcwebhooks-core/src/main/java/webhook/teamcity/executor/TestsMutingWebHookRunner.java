package webhook.teamcity.executor;

import java.util.Collection;
import java.util.Map;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookConfig;

public class TestsMutingWebHookRunner extends AbstractWebHookExecutor implements WebHookRunner {
	
	private Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups;
    private SProject sProject;

	public TestsMutingWebHookRunner(
			WebHookContentBuilder webHookContentBuilder,
			WebHookHistoryRepository webHookHistoryRepository,
			WebHookHistoryItemFactory webHookHistoryItemFactory,
			WebHook webhook,
			WebHookConfig whc,
			SProject sProject,
			BuildStateEnum state,
			Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups,
			boolean isTest) 
	{
		super (
			 webHookContentBuilder,
			 webHookHistoryRepository,
			 webHookHistoryItemFactory,
			 whc,
			 state, isTest,
			 webhook,
			 isTest);
		this.mutedOrUnmutedGroups = mutedOrUnmutedGroups;
		this.sProject = sProject;
	}

	@Override
	protected WebHook getWebHookContent() {
		return webHookContentBuilder.buildWebHookContent(webhook, whc, sProject, mutedOrUnmutedGroups, state, overrideIsEnabled);
	}
	
	@Override
	public WebHookHistoryItem buildWebHookHistoryItem(WebHookErrorStatus errorStatus) {
		if (this.isTest) {
				return webHookHistoryItemFactory.getWebHookHistoryTestItem(
						whc,
						webhook.getExecutionStats(), 
						this.sProject,
						errorStatus
				);
		} else {
				return webHookHistoryItemFactory.getWebHookHistoryItem(
						whc,
						webhook.getExecutionStats(), 
						this.sProject,
						errorStatus
				);
		}
	}

}
