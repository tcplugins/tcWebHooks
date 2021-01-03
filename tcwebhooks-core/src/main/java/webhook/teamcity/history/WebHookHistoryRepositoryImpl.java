package webhook.teamcity.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.LocalDate;

import webhook.teamcity.Loggers;

public class WebHookHistoryRepositoryImpl implements WebHookHistoryRepository {
	
	Map<UUID,WebHookHistoryItem> webHookHistoryItems = Collections.synchronizedMap(new MaxSizeHashMap<UUID,WebHookHistoryItem>(10000)); 
	Comparator<WebHookHistoryItem> chronComparator = new WebHookHistoryRepositoryDateSortingCompator(SortDirection.ASC);
	Comparator<WebHookHistoryItem> reverseChronComparator = new WebHookHistoryRepositoryDateSortingCompator(SortDirection.DESC);
	
	AtomicInteger okCounter = new AtomicInteger(0);
	AtomicInteger erroredCounter = new AtomicInteger(0);
	AtomicInteger disabledCounter = new AtomicInteger(0);
	AtomicInteger totalCounter = new AtomicInteger(0);
	
	@Override
	public void addHistoryItem(WebHookHistoryItem histoyItem) {
		this.webHookHistoryItems.put(histoyItem.getWebHookExecutionStats().getTrackingId(), histoyItem);
		if (histoyItem.getWebHookExecutionStats().isEnabled()  && ! histoyItem.getWebHookExecutionStats().isErrored()) {
			okCounter.incrementAndGet();
		} else if (histoyItem.getWebHookExecutionStats().isEnabled()) {
			erroredCounter.incrementAndGet();
		} else if (! histoyItem.getWebHookExecutionStats().isEnabled()) {
			disabledCounter.incrementAndGet();
		}
		totalCounter.incrementAndGet();
	}
	
	@Override
	public WebHookHistoryItem getHistoryItem(String trackingId) {
		try {
			UUID uuid = UUID.fromString(trackingId);
			return webHookHistoryItems.get(uuid);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	@Override
	public PagedList<WebHookHistoryItem> findHistoryItemsForProject(String projectId, int pageNumber, int pageSize) {
		List<WebHookHistoryItem> projItems = new ArrayList<>();
		synchronized (webHookHistoryItems) {
			for (WebHookHistoryItem item : webHookHistoryItems.values()) {
				if (item.getProjectId().equals(projectId)) {
					projItems.add(item);
				}
			}
		}
		Collections.sort(projItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, projItems);
	}

	@Override
	public PagedList<WebHookHistoryItem> findHistoryItemsForBuildType(String buildTypeId, int pageNumber, int pageSize) {
		List<WebHookHistoryItem> buildTypeItems = new ArrayList<>();
		synchronized (webHookHistoryItems) {
			for (WebHookHistoryItem item : webHookHistoryItems.values()) {
				if (item.getBuildTypeId().equals(buildTypeId)) {
					buildTypeItems.add(item);
				}
			}
		}
		Collections.sort(buildTypeItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, buildTypeItems);
	}

	@Override
	public PagedList<WebHookHistoryItem> findHistoryItemsForBuild(Long buildId, int pageNumber, int pageSize) {
		List<WebHookHistoryItem> buildItems = new ArrayList<>();
		synchronized (webHookHistoryItems) {
			for (WebHookHistoryItem item : webHookHistoryItems.values()) {
				if (item.getBuildId().equals(buildId)) {
					buildItems.add(item);
				}
			}
		}
		Collections.sort(buildItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, buildItems);
	}
	
	@Override
	public PagedList<WebHookHistoryItem> findHistoryItemsForWebHookConfigId(String webHookId, int pageNumber, int pageSize) {
		List<WebHookHistoryItem> historyItems = new ArrayList<>();
		synchronized (webHookHistoryItems) {
			for (WebHookHistoryItem item : webHookHistoryItems.values()) {
				if (webHookId.equals(item.getWebHookConfig().getUniqueKey())) {
					historyItems.add(item);
				}
			}
		}
		Collections.sort(historyItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, historyItems);
	}
	
	@Override
	public Map<String, Integer> findHistoryItemCountsForWebHookConfigId(String webhookId) {
		List<WebHookHistoryItem> historyItems = new ArrayList<>();
		synchronized (webHookHistoryItems) {
			for (WebHookHistoryItem item : webHookHistoryItems.values()) {
				if (webhookId.equals(item.getWebHookConfig().getUniqueKey())) {
					historyItems.add(item);
				}
			}
		}
		Map<String, Integer> itemCounts = new HashMap<>();
		itemCounts.put("ok", findHistoryItemsOK(historyItems).size());
		itemCounts.put("errored", findHistoryItemsInError(historyItems).size());
		itemCounts.put("disabled", findHistoryItemsDisabled(historyItems).size());
		itemCounts.put("total", historyItems.size());
		return itemCounts;
	}	

	@Override
	public PagedList<WebHookHistoryItem> findHistoryErroredItems(int pageNumber, int pageSize) {
		List<WebHookHistoryItem> errorItems = findHistoryItemsInError(getAllStoreItems());
		Collections.sort(errorItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, errorItems);
	}
	
	@Override
	public PagedList<WebHookHistoryItem> findHistoryDisabledItems(int pageNumber, int pageSize) {
		List<WebHookHistoryItem> disabledItems = findHistoryItemsDisabled(getAllStoreItems());
		Collections.sort(disabledItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, disabledItems);
	}
	
	@Override
	public PagedList<WebHookHistoryItem> findHistoryOkItems(int pageNumber, int pageSize) {
		List<WebHookHistoryItem> okItems =  findHistoryItemsOK(getAllStoreItems());
		Collections.sort(okItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, okItems);
	}

	@Override
	public PagedList<WebHookHistoryItem> findHistoryAllItems(int pageNumber, int pageSize) {
		List<WebHookHistoryItem> allitems = getAllStoreItems();
		Collections.sort(allitems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, allitems);
	}	

	private List<WebHookHistoryItem> findHistoryItemsInError(Collection<WebHookHistoryItem> webHookHistoryItemsCollection) {
		List<WebHookHistoryItem> errorItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItemsCollection) {
			if (item.getWebHookExecutionStats().isErrored()) {
				errorItems.add(item);
			}
		}
		return errorItems;
	}
	
	private List<WebHookHistoryItem> findHistoryItemsDisabled(Collection<WebHookHistoryItem> webHookHistoryItemsCollection) {
		List<WebHookHistoryItem> disabledItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItemsCollection) {
			if (! item.getWebHookExecutionStats().isEnabled()) {
				disabledItems.add(item);
			}
		}
		return disabledItems;
	}
	
	private List<WebHookHistoryItem> findHistoryItemsOK(Collection<WebHookHistoryItem> webHookHistoryItemsCollection) {
		List<WebHookHistoryItem> okItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItemsCollection) {
			if (item.getWebHookExecutionStats().isEnabled()  && ! item.getWebHookExecutionStats().isErrored()) {
				okItems.add(item);
			}
		}
		return okItems;
	}
	
	@Override
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryDisabledItemsGroupedByDay(LocalDate untilDate, int numberOfDays) {
		boolean errored = false;
		boolean disabled = true;
		Set<LocalDate> arrayOfDatesInclusive = getArrayOfDatesInclusive(untilDate, numberOfDays);
		return findHistoryItemsGroupedByDay(arrayOfDatesInclusive, findItemsInRange(arrayOfDatesInclusive, errored, disabled));
	}

	@Override
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryErroredItemsGroupedByDayInclusive(LocalDate untilDate, int numberOfDays) {
		boolean errored = true;
		boolean disabled = false;
		Set<LocalDate> arrayOfDatesInclusive = getArrayOfDatesInclusive(untilDate, numberOfDays);
		return findHistoryItemsGroupedByDay(arrayOfDatesInclusive, findItemsInRange(arrayOfDatesInclusive, errored, disabled));
	}
	
	@Override
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryOkItemsGroupedByDayInclusive(LocalDate untilDate, int numberOfDays) {
		boolean errored = false;
		boolean disabled = false;
		Set<LocalDate> arrayOfDatesInclusive = getArrayOfDatesInclusive(untilDate, numberOfDays);
		return findHistoryItemsGroupedByDay(arrayOfDatesInclusive, findItemsInRange(arrayOfDatesInclusive, errored, disabled));
	}
	
	@Override
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryAllItemsGroupedByDayInclusive(LocalDate untilDate, int numberOfDays) {
		Set<LocalDate> arrayOfDatesInclusive = getArrayOfDatesInclusive(untilDate, numberOfDays);
		return findHistoryItemsGroupedByDay(arrayOfDatesInclusive, findAllItemsSince(arrayOfDatesInclusive));
	}
	
	private Map<LocalDate, List<WebHookHistoryItem>> findHistoryItemsGroupedByDay(Set<LocalDate> dateRange, List<WebHookHistoryItem> items) {
		Map<LocalDate, List<WebHookHistoryItem>> groupedHistoryItems = new LinkedHashMap<>(dateRange.size());
		for (LocalDate thisDate : dateRange) {
			List<WebHookHistoryItem> thisDateList = new ArrayList<>();
			for (WebHookHistoryItem item : items) {
				if (thisDate.isEqual(item.getTimestamp().toLocalDate())) {
					thisDateList.add(item);
				}
			}
			groupedHistoryItems.put(thisDate, thisDateList);
		}
		return groupedHistoryItems;
	}

	private List<WebHookHistoryItem> findItemsInRange(Set<LocalDate> dates, boolean isErrored, boolean isDisabled) {
		List<WebHookHistoryItem> itemsSince = new ArrayList<>();
		synchronized (webHookHistoryItems) {
			for (WebHookHistoryItem item : this.webHookHistoryItems.values()) {
				LocalDate itemDate = item.getTimestamp().toLocalDate(); 
				if (dates.contains(itemDate) 
					&& item.getWebHookExecutionStats().isErrored() == isErrored 
					&& item.getWebHookExecutionStats().isEnabled() != isDisabled) 
				{
					itemsSince.add(item);
				}
			}
		}
		return itemsSince;
	}
	
	private List<WebHookHistoryItem> findAllItemsSince(Set<LocalDate> dateRange) {
		List<WebHookHistoryItem> erroredItemsSince = new ArrayList<>();
		Loggers.SERVER.debug("WebHookHistoryRepositoryImpl :: Waiting on synchronized block for webhookHistoryItems");
		synchronized (webHookHistoryItems) {
			for (WebHookHistoryItem item : this.webHookHistoryItems.values()) {
				if (dateRange.contains(item.getTimestamp().toLocalDate())) {
					erroredItemsSince.add(item);
				}
			}
		}
		Loggers.SERVER.debug("WebHookHistoryRepositoryImpl :: Exited synchronized block for webhookHistoryItems");
		return erroredItemsSince;
	}

	private Set<LocalDate> getArrayOfDatesInclusive(LocalDate untilDate, int numberOfDays) {
		Set<LocalDate> days = new HashSet<>();
		days.add(untilDate);
		for (int i = numberOfDays; i > 0 ; i--) {
			days.add(untilDate.minusDays(i));
		}
		Loggers.SERVER.debug(String.format("WebHookHistoryRepositoryImpl :: Resolving requested dates to: [%s]", days.toString()));
		return days;
	}

	@Override
	public int getTotalCount() {
		return this.totalCounter.get();
	}


	@Override
	public int getDisabledCount() {
		return this.disabledCounter.get();
	}	
	
	@Override
	public int getErroredCount() {
		return this.erroredCounter.get();
	}

	@Override
	public int getOkCount() {
		return this.okCounter.get();
	}

	@Override
	public int getTotalStoreItems() {
		synchronized (webHookHistoryItems) {
			return webHookHistoryItems.size();
		}
	}
	
	private List<WebHookHistoryItem> getAllStoreItems() {
		List<WebHookHistoryItem> allItems = new ArrayList<>();
		synchronized (webHookHistoryItems) {
			allItems.addAll(this.webHookHistoryItems.values());
		}
		return allItems;
	}

	public class WebHookHistoryRepositoryDateSortingCompator implements Comparator<WebHookHistoryItem> {
		
		SortDirection sortDirection;
		
		public WebHookHistoryRepositoryDateSortingCompator(SortDirection sortDirection) {
			this.sortDirection = sortDirection;
		}
		
		@Override
		public int compare(WebHookHistoryItem o1, WebHookHistoryItem o2) {
			
			if (sortDirection == SortDirection.DESC) {
				if (o1.getTimestamp().isBefore(o2.getTimestamp())){
					return 1;
				} else if (o1.getTimestamp().isAfter(o2.getTimestamp())){
					return -1;
				} else {
					return 0;
				}
			} else {
				if (o1.getTimestamp().isAfter(o2.getTimestamp())){
					return 1;
				} else if (o1.getTimestamp().isBefore(o2.getTimestamp())){
					return -1;
				} else {
					return 0;
				}				
			}
		}
	}
	
	public enum SortDirection {
		ASC,
		DESC;
	}

}
