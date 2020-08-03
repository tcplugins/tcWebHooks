package webhook.teamcity.history;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

public interface WebHookHistoryRepository {
	/**
	 * Adds a {@link WebHookHistoryItem} to the repository
	 * @param historyItem
	 */
	public void addHistoryItem(WebHookHistoryItem historyItem);
	/**
	 * Get {@link WebHookHistoryItem} from repository
	 * @param trackingId to identify the history item
	 * @return {@link WebHookHistoryItem} or null if no matching item found.
	 */
	public WebHookHistoryItem getHistoryItem(String trackingId);
	public PagedList<WebHookHistoryItem> findHistoryItemsForProject(String projectId, int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryItemsForBuildType(String buildTypeId, int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryItemsForBuild(Long buildId, int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryErroredItems(int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryDisabledItems(int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryOkItems(int pageNumber, int pageSize);
	public PagedList<WebHookHistoryItem> findHistoryAllItems(int pageNumber, int pageSize);
	public Map<LocalDate,List<WebHookHistoryItem>> findHistoryErroredItemsGroupedByDay(LocalDate untilDate, int numberOfDays);
	public Map<LocalDate,List<WebHookHistoryItem>> findHistoryDisabledItemsGroupedByDay(LocalDate untilDate, int numberOfDays);
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryOkItemsGroupedByDay(LocalDate untilDate, int numberOfDays);
	public Map<LocalDate,List<WebHookHistoryItem>> findHistoryAllItemsGroupedByDay(LocalDate untilDate, int numberOfDays);
	public int getTotalCount();
	public int getErroredCount();
	public int getDisabledCount();
	public int getOkCount();
	public int getTotalStoreItems();
}
