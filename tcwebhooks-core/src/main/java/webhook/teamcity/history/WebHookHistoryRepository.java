package webhook.teamcity.history;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

public interface WebHookHistoryRepository {
	
	public void addHistoryItem(WebHookHistoryItem histoyItem);
	public PagedList<WebHookHistoryItem> findHistoryItemsForProject(String projectId, int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryItemsForBuildType(String buildTypeId, int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryItemsForBuild(Long buildId, int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryErroredItems(int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryDisabledItems(int pageNumber, int pageSize);
	public Map<LocalDate,List<WebHookHistoryItem>> findHistoryErroredItemsGroupedByDay(LocalDate untilDate, int numberOfDays);
	public Map<LocalDate,List<WebHookHistoryItem>> findHistoryDisabledItemsGroupedByDay(LocalDate untilDate, int numberOfDays);
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryOkItemsGroupedByDay(LocalDate untilDate, int numberOfDays);
	public Map<LocalDate,List<WebHookHistoryItem>> findHistoryAllItemsGroupedByDay(LocalDate untilDate, int numberOfDays);
	public int getTotalCount();
	public int getErroredCount();
	public int getDisabledCount();
	public int getOkCount();
}
