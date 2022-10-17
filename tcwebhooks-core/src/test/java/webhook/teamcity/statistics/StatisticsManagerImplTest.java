package webhook.teamcity.statistics;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.ServerPaths;
import webhook.teamcity.exception.StatisticsFileOperationException;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookMainSettings;

public class StatisticsManagerImplTest extends BaseStatisticsTest {
	
	@Test
	public void testUpdateStatistics() throws StatisticsFileOperationException, JDOMException, IOException {
		WebHookHistoryRepository webHookHistoryRepository = Mockito.mock(WebHookHistoryRepository.class);
		Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(LocalDate.parse("2020-01-01"), 0)).thenReturn(buildStats(LocalDateTime.parse("2020-01-01T01:30:00")));
		ServerPaths serverPaths = Mockito.mock(ServerPaths.class);
		Mockito.when(serverPaths.getConfigDir()).thenReturn(folder.getRoot().getAbsolutePath());
		
		WebHookMainSettings settings = Mockito.mock(WebHookMainSettings.class);
		Mockito.when(settings.isReportStatisticsEnabled()).thenReturn(true);
		
		MockingStatisticsJaxHelper jaxHelper = new MockingStatisticsJaxHelper();
		StatisticsManagerImpl statisticsManager = new StatisticsManagerImpl(webHookHistoryRepository, jaxHelper, serverPaths, null, null, settings);
		statisticsManager.updateStatistics(LocalDateTime.parse("2020-01-01T03:00:00.000"));
		assertEquals(1, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-01-01T03:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
		Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(LocalDate.parse("2020-01-01"), 0)).thenReturn(buildStats(LocalDateTime.parse("2020-01-01T04:30:00")));
		statisticsManager.updateStatistics(LocalDateTime.parse("2020-01-01T05:00:00.000"));
		assertEquals(2, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-01-01T05:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
	}
	
	@Test
	public void testCleanupOldStatistics() throws StatisticsFileOperationException, JDOMException, IOException {
		WebHookHistoryRepository webHookHistoryRepository = Mockito.mock(WebHookHistoryRepository.class);
		ServerPaths serverPaths = Mockito.mock(ServerPaths.class);
		Mockito.when(serverPaths.getConfigDir()).thenReturn(folder.getRoot().getAbsolutePath());
		
		WebHookMainSettings settings = Mockito.mock(WebHookMainSettings.class);
		Mockito.when(settings.isReportStatisticsEnabled()).thenReturn(true);
		
		MockingStatisticsJaxHelper jaxHelper = new MockingStatisticsJaxHelper();
		StatisticsManagerImpl statisticsManager = new StatisticsManagerImpl(webHookHistoryRepository, jaxHelper, serverPaths, null, null, settings);
		for (int m = 1; m <= 6; m++) { // months
			for (int d = 1; d <= 9; d++) { // days
				Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(LocalDate.parse(String.format("2020-0%d-0%d", m, d)), 0)).thenReturn(buildStats(LocalDateTime.parse(String.format("2020-0%d-0%dT01:30:00", m, d))));
				statisticsManager.updateStatistics(LocalDateTime.parse(String.format("2020-0%d-0%dT03:00:00.000", m,d)));
			}
		}
		assertEquals(54, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-06-09T03:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
		// Check that we have 6 months times 9 files per month.
		assertEquals(54, new File(statisticsManager.getConfigDir()).listFiles().length);
		statisticsManager.cleanupOldStatistics(LocalDateTime.parse("2021-04-01T00:30:00.000"));
		// Now that we have removed all the ones from Jan to March, we should only have 27 left.
		assertEquals(27, new File(statisticsManager.getConfigDir()).listFiles().length);
	}
}
