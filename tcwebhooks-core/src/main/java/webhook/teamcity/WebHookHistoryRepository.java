package webhook.teamcity;

import java.util.List;

public interface WebHookHistoryRepository {
	
	public void addHistoryItem(WebHookHistoryItem histoyItem);
	public List<WebHookHistoryItem> findHistoryItemsForProject(String projectId);
	public List<WebHookHistoryItem> findHistoryItemsForBuildType(String buildTypeId);
	public List<WebHookHistoryItem> findHistoryItemsForBuild(Long buildId);
	public List<WebHookHistoryItem> findHistoryItemsInError();
	
}
