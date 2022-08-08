package webhook.teamcity.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import jetbrains.buildServer.serverSide.ServerPaths;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.exception.StatisticsFileOperationException;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;

public class StatisticsManagerImpl implements StatisticsManager {
	
	private static final String YEAR_MONTH_DATE = "yyyy-MM-dd";
	
	private final WebHookHistoryRepository webHookHistoryRepository;
	private StatisticsJaxHelper myJaxHelpher;
	
	private final ServerPaths myServerPaths;

	private final StatisticsReportAssembler myStatisticReportAssembler;

	private final WebHooksStatisticsReportEventListener myStatisticsEventListener;
	private final WebHookMainSettings myWebHookMainSettings;

	public StatisticsManagerImpl(
			WebHookHistoryRepository webHookHistoryRepository, 
			StatisticsJaxHelper jaxHelper,
			ServerPaths serverPaths,
			StatisticsReportAssembler statisticReportAssembler,
			WebHooksStatisticsReportEventListener statisticsEventListener,
			WebHookMainSettings webHookMainSettings
			) {
		this.webHookHistoryRepository = webHookHistoryRepository;
		this.myJaxHelpher = jaxHelper;
		this.myServerPaths = serverPaths;
		this.myStatisticReportAssembler = statisticReportAssembler;
		this.myStatisticsEventListener = statisticsEventListener;
		this.myWebHookMainSettings = webHookMainSettings;

	}
	
	
	// If new day, then do last stats from yesterday, and write them out.
	// Queue job to send any undelivered stats (with re-tries).
	
	// Find stats since last snapshot was written (probably an hour ago)
	// Load stats for today. 
	// Update stats for today. 
	// Write stats for today.

	@Override
	public void updateStatistics(LocalDateTime now) throws StatisticsFileOperationException {
		
		File dirPath = new File(getConfigDir());
		if ( !dirPath.isDirectory() && !dirPath.mkdir() ) {
			throw new StatisticsFileOperationException(String.format("Unable to create directory: [%s]", dirPath.toString()));
		}
		
		findAndUpdateStatsForDate(now);
		
		if (now.getHourOfDay() == 0) {
			LocalDateTime yesterday = now.minusDays(1).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
			findAndUpdateStatsForDate(yesterday);
		}
		
	}


	private void findAndUpdateStatsForDate(LocalDateTime dateTime) {
		Loggers.SERVER.debug("StatisticsManagerImpl :: Starting findAndUpdateStatsForDate for date: " + dateTime);
		StatisticsEntity previousStatisticsEntity = loadLastStatisticsEntity(dateTime.toLocalDate());
		Loggers.SERVER.debug("StatisticsManagerImpl :: Loaded previous stats from time:  " + previousStatisticsEntity.getLastUpdated());
		
		List<WebHookHistoryItem> allItemsForDate = webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(dateTime.toLocalDate(), 0).get(dateTime.toLocalDate());
		Loggers.SERVER.debug(String.format("StatisticsManagerImpl :: Found %s items from repository for date %s ", allItemsForDate.size(), dateTime.toLocalDate()));
		List<WebHookHistoryItem> newItemsForDate = new ArrayList<>();
		for (WebHookHistoryItem item :allItemsForDate) {
			if (item.getTimestamp().isAfter(previousStatisticsEntity.getLastUpdated())) {
				newItemsForDate.add(item);
			}
		}
		Loggers.SERVER.debug(String.format("StatisticsManagerImpl :: Filtered items contains %s items from repository newer than %s ", newItemsForDate.size(), previousStatisticsEntity.getLastUpdated()));
		
		StatisticsEntity updatedPayload = this.buildStatisticsEntity(previousStatisticsEntity, newItemsForDate, dateTime);
		Loggers.SERVER.debug(String.format("StatisticsManagerImpl :: New payload data created with time: %s ", updatedPayload.getLastUpdated()));
		try {
			myJaxHelpher.writeFile(updatedPayload, StatisticsEntity.class, getConfigFilePath(dateTime.toLocalDate()));
		} catch (JAXBException ex) {
			Loggers.SERVER.warn(String.format("StatisticsManagerImpl :: Unable to update WebHook Statistics file for date '%s'", dateTime), ex);
		}
	}
	
	@Override
	public StatisticsEntity buildStatisticsEntity(StatisticsEntity statisticsEntity, List<WebHookHistoryItem> webHookHistoryItems, LocalDateTime dateTime) {
		
		return new StatisticsEntityBuilder()
			.existingStatisticsEntity(statisticsEntity)
			.atTime(dateTime)
			.forDate(dateTime.toLocalDate())
			.stats(Collections.singletonMap(dateTime.toLocalDate(), webHookHistoryItems))
			.build();
		
	}
	
	@Override
	public List<StatisticsEntity> getHistoricalStatistics(LocalDate startDate, LocalDate endDate) {
		int days = Days.daysBetween(startDate, endDate).getDays();
		List<StatisticsEntity> snapShots = new ArrayList<>();
		for(int i = 0; i < days; i++) {
			snapShots.add(loadLastStatisticsEntity(startDate.plusDays(i)));
		}
		return snapShots;
	}
	@Override
	public List<StatisticsEntity> getUnreportedHistoricalStatisticsEntities(LocalDate startDate, LocalDate endDate) {
		int days = Days.daysBetween(startDate, endDate).getDays();
		List<StatisticsEntity> snapShots = new ArrayList<>();
		for(int i = 0; i < days; i++) {
			StatisticsEntity entity = loadLastStatisticsEntity(startDate.plusDays(i));
			if (! Boolean.TRUE.equals(entity.reported)) {
				snapShots.add(entity);
			}
		}
		return snapShots;
	}
	
	@Override
	public void reportStatistics(LocalDateTime now) {
		
		if (! this.myWebHookMainSettings.isReportStatisticsEnabled()) {
			return;
		}
		
		// find unreported stats 
		List<StatisticsEntity> unreportedStats = getUnreportedHistoricalStatisticsEntities(now.toLocalDate().minusDays(14), now.toLocalDate());
		
		// if it was more than 4 days ago, then build stats until yesterday.
		if (unreportedStats.size() >= this.myWebHookMainSettings.getReportStatisticsFrequency()) {
			try {
				WebHookConfig whc = new WebHookConfig("_Root", "_Root", "http://localhost:8111/webhooks/endpoint.html", Boolean.TRUE, new BuildState().enable(BuildStateEnum.REPORT_STATISTICS), "statistics-report", false, false, null, null, true);
				StatisticsReport report = this.myStatisticReportAssembler.assembleStatisticsReports(new CryptValueHasher(), unreportedStats);
				myStatisticsEventListener.reportStatistics(whc, report);
				markStatisticsAsReported(unreportedStats);
				myStatisticsEventListener.reportStatistics(report);
			} catch (RuntimeException ex) {
				Loggers.SERVER.warn("StatisticsManagerImpl :: Unable to send WebHooks statistics report", ex);
			}
		} else {
			Loggers.SERVER.debug(String.format("StatisticsManagerImpl :: Skipping report sending. Only %s days of reports to send.", unreportedStats.size()));
		}
		// Mark stats as sent for all included dates.
		
	}
	
	private void markStatisticsAsReported(List<StatisticsEntity> unreportedStats) {
		for (StatisticsEntity entity : unreportedStats) {
			entity.setReported(Boolean.TRUE);
			try {
				myJaxHelpher.writeFile(entity, StatisticsEntity.class, getConfigFilePath(entity.statisticsSnapshot.getDate()));
			} catch (JAXBException ex) {
				Loggers.SERVER.warn(String.format("StatisticsManagerImpl :: Unable to mark WebHook Statistics file as reported for date '%s'", entity.statisticsSnapshot.getDate()), ex);
			}
		}
		
	}


	private StatisticsEntity loadLastStatisticsEntity(LocalDate date) {
		try {
			return myJaxHelpher.readFile(getConfigFilePath(date), StatisticsEntity.class);
		} catch (FileNotFoundException | JAXBException e) {
			//  Initialise at midnight.
			return new StatisticsEntity().at(getMidnight(date)).withSnapshot(new StatisticsSnapshot().at(date));
		}
	}
	
	private LocalDateTime getMidnight(LocalDate date) {
		return new LocalDateTime()
				.withYear(date.getYear()).withMonthOfYear(date.getMonthOfYear()).withDayOfMonth(date.getDayOfMonth())
				.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
	}

	private String getConfigDir() {
		return new StringBuilder()
				.append(myServerPaths.getConfigDir())
				.append(File.separator )
				.append("webhooks-statistics")
				.toString();
	}
	
	private String getConfigFilePath(LocalDate date) {

		return new StringBuilder()
				.append(getConfigDir())
				.append(File.separator)
				.append("stats-" )
				.append(date.toString(YEAR_MONTH_DATE))
				.append(".xml").toString();
	}

}
