package webhook.teamcity.statistics;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import webhook.teamcity.exception.StatisticsFileOperationException;
import webhook.teamcity.history.WebHookHistoryItem;

public interface StatisticsManager {
	
	public void updateStatistics(LocalDateTime now) throws StatisticsFileOperationException;
	public List<StatisticsEntity> getHistoricalStatistics(LocalDate fromDate, LocalDate toDate);
	public void reportStatistics(LocalDateTime now);
	public List<StatisticsEntity> getUnreportedHistoricalStatisticsEntities(LocalDate startDate, LocalDate endDate);
	public StatisticsEntity buildStatisticsEntity(StatisticsEntity statisticsEntity, List<WebHookHistoryItem> webHookExecutionStats, LocalDateTime dateTime);

}
