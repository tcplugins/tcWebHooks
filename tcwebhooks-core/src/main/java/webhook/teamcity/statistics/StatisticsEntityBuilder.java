package webhook.teamcity.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.statistics.StatisticsSnapshot.StatisticsItem;

public class StatisticsEntityBuilder {
	
	private LocalDate date;
	private LocalDateTime lastModified;
	private StatisticsEntity statisticsEntity;
	private Map<LocalDate, List<WebHookHistoryItem>> stats;
	private int okCount = 0;
	private int erroredCount = 0;
	private int skippedCount = 0;
	private int totalCount = 0;
	private ValueHasher valueHasher = new NoOpValueHasher();

	public StatisticsEntityBuilder existingStatisticsEntity(StatisticsEntity existingStatisticsEntity) {
		this.statisticsEntity = existingStatisticsEntity;
		return this;
	}

	public StatisticsEntityBuilder stats(Map<LocalDate,List<WebHookHistoryItem>> stats) {
		this.stats = stats;
		return this;
	}
	
	public StatisticsEntityBuilder counts(int okCount, int erroredCount, int skippedCount, int totalCount) {
		this.okCount = okCount;
		this.erroredCount = erroredCount;
		this.skippedCount = skippedCount;
		this.totalCount = totalCount;
		return this;
	}
	
	public StatisticsEntityBuilder forDate(LocalDate date) {
		this.date = date;
		return this;
	}
	
	public StatisticsEntityBuilder atTime(LocalDateTime lastModifiedDt) {
		this.lastModified = lastModifiedDt;
		return this;
	}
	
	public StatisticsEntityBuilder withHasher(ValueHasher hasher) {
		this.valueHasher = hasher;
		return this;
	}
	
	public StatisticsEntity build() {
		if (this.statisticsEntity == null) {
			this.statisticsEntity = new StatisticsEntity().at(this.lastModified);
			this.statisticsEntity.setStatisticsSnapshot(new StatisticsSnapshot().at(this.date));
		}

		StatisticsSnapshot statisticsSnapshot = statisticsEntity.getStatisticsSnapshot();
		if (stats != null && stats.get(date) != null) {
			for (WebHookHistoryItem item : stats.get(date)) {
				if (item.getTimestamp().isAfter(statisticsEntity.getLastUpdated())) {
 					if (item.getWebHookExecutionStats().isEnabled() && item.getWebHookExecutionStats().isErrored()) {
 						this.erroredCount++;
 						this.totalCount++;
 					} else if (item.getWebHookExecutionStats().isEnabled()) {
 						this.okCount++;
 						this.totalCount++;
 					} else{
 						this.skippedCount++;
 						this.totalCount++;
 					}
					addUrlStatistic(
							this.statisticsEntity.getStatisticsSnapshot(), 
							getGeneralisedWebAddress(item), 
							item.getWebHookExecutionStats().getBuildState(),
							item.getWebHookExecutionStats().getStatusCode());
					addTemplateStatistic(
							this.statisticsEntity.getStatisticsSnapshot(), 
							item.getWebHookConfig().getPayloadTemplate(), 
							item.getWebHookExecutionStats().getBuildState(),
							item.getWebHookExecutionStats().getStatusCode());
				}
				
			}
		}
		statisticsSnapshot.addErrorCount(this.erroredCount);
		statisticsSnapshot.addOkCount(this.okCount);
		statisticsSnapshot.addSkippedCount(this.skippedCount);
		statisticsSnapshot.addTotalCount(this.totalCount);
		this.statisticsEntity.setLastUpdated(this.lastModified);
		return this.statisticsEntity;
	}
	
	public StatisticsSnapshot copy(StatisticsSnapshot existingSnapShot) {
		StatisticsSnapshot newSS = new StatisticsSnapshot().at(existingSnapShot.getDate());
		newSS.addErrorCount(existingSnapShot.getErrorCount());
		newSS.addOkCount(existingSnapShot.getOkCount());
		newSS.addSkippedCount(existingSnapShot.getSkippedCount());
		newSS.addTotalCount(existingSnapShot.getTotalCount());
		for (Map.Entry<String, StatisticsItem> entry : existingSnapShot.getTemplates().entrySet()) {
			newSS.getTemplates().put(
					valueHasher.hash(entry.getKey()), 
					new StatisticsItem(
							valueHasher.hash(entry.getValue().getName()), 
							entry.getValue().getInvocations(), 
							entry.getValue().getStatuses()));
		}
		for (Map.Entry<String, StatisticsItem> entry : existingSnapShot.getUrls().entrySet()) {
			newSS.getUrls().put(
					valueHasher.hash(entry.getKey()), 
					new StatisticsItem(
							valueHasher.hash(entry.getValue().getName()), 
							entry.getValue().getInvocations(), 
							entry.getValue().getStatuses()));
		}
		return newSS;
	}

	private String getGeneralisedWebAddress(WebHookHistoryItem item) {
		if (item.getGeneralisedWebAddress() != null && item.getGeneralisedWebAddress().getGeneralisedAddress() != null) {
			return item.getGeneralisedWebAddress().getGeneralisedAddress();
		}
		return "null";
	}
	
	private void addUrlStatistic(StatisticsSnapshot payloadToAppendTo, String url, BuildStateEnum buildStateEnum, int status) {
		StatisticsItem urlStats = payloadToAppendTo.getUrls().get(url);
		if (urlStats == null) {
			urlStats = new StatisticsItem(url, 0, new ArrayList<>());
		}
		payloadToAppendTo.getUrls().put(url, populateStats(urlStats,  url, buildStateEnum, status));
	}
	private void addTemplateStatistic(StatisticsSnapshot payloadToAppendTo, String templateId, BuildStateEnum buildStateEnum, int status) {
		StatisticsItem templateStats = payloadToAppendTo.getTemplates().get(templateId);
		if (templateStats == null) {
			templateStats = new StatisticsItem(templateId, 0, new ArrayList<>());
		}
		payloadToAppendTo.getTemplates().put(templateId, populateStats(templateStats, templateId, buildStateEnum, status));
	}

	private StatisticsItem populateStats(StatisticsItem statsObject, String key, BuildStateEnum buildStateEnum, int status) {
		statsObject.name = key;
		int statusCount = statsObject.getStatus(buildStateEnum, status);
		statusCount++;
		statsObject.putStatus(buildStateEnum,status, statusCount);
		statsObject.invocations++;
		return statsObject;
	}

}
