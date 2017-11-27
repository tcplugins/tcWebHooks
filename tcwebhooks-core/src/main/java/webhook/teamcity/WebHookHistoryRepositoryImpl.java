package webhook.teamcity;

import java.util.ArrayList;
import java.util.List;

public class WebHookHistoryRepositoryImpl implements WebHookHistoryRepository {
	
	List<WebHookHistoryItem> webHookHistoryItems = new ArrayList<>();

	@Override
	public void addHistoryItem(WebHookHistoryItem histoyItem) {
		this.webHookHistoryItems.add(histoyItem);
		Loggers.SERVER.debug(histoyItem.toString());
	}

	@Override
	public List<WebHookHistoryItem> findHistoryItemsForProject(String projectId) {
		List<WebHookHistoryItem> projItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems) {
			if (item.getProjectId().equals(projectId)) {
				projItems.add(item);
			}
		}
		return projItems;
	}

	@Override
	public List<WebHookHistoryItem> findHistoryItemsForBuildType(String buildTypeId) {
		List<WebHookHistoryItem> buildTypeItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems) {
			if (item.getBuildTypeId().equals(buildTypeId)) {
				buildTypeItems.add(item);
			}
		}
		return buildTypeItems;
	}

	@Override
	public List<WebHookHistoryItem> findHistoryItemsForBuild(Long buildId) {
		List<WebHookHistoryItem> buildItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems) {
			if (item.getBuildId().equals(buildId)) {
				buildItems.add(item);
			}
		}
		return buildItems;
	}

	@Override
	public List<WebHookHistoryItem> findHistoryItemsInError() {
		List<WebHookHistoryItem> errorItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems) {
			if (item.getWebHook().isErrored()) {
				errorItems.add(item);
			}
		}
		return errorItems;
	}

}
