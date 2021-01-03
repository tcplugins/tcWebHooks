package webhook.teamcity.statistics;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jdom.JDOMException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.ServerPaths;
import webhook.teamcity.exception.StatisticsFileOperationException;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.statistics.StatisticsManagerImpl;

public class StatisticsManagerImplTest extends BaseStatisticsTest {
	
	@Test
	public void testUpdateStatistics() throws StatisticsFileOperationException, JDOMException, IOException {
		WebHookHistoryRepository webHookHistoryRepository = Mockito.mock(WebHookHistoryRepository.class);
		Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(ArgumentMatchers.eq(LocalDate.parse("2020-01-01")), ArgumentMatchers.eq(0))).thenReturn(buildStats(LocalDateTime.parse("2020-01-01T01:30:00")));
		ServerPaths serverPaths = Mockito.mock(ServerPaths.class);
		Mockito.when(serverPaths.getConfigDir()).thenReturn(folder.getRoot().getAbsolutePath());
		
		WebHookMainSettings settings = Mockito.mock(WebHookMainSettings.class);
		Mockito.when(settings.isReportStatisticsEnabled()).thenReturn(true);
		
		MockingStatisticsJaxHelper jaxHelper = new MockingStatisticsJaxHelper();
		StatisticsManagerImpl statisticsManager = new StatisticsManagerImpl(webHookHistoryRepository, jaxHelper, serverPaths, null, null, settings);
		statisticsManager.updateStatistics(LocalDateTime.parse("2020-01-01T03:00:00.000"));
		assertEquals(1, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-01-01T03:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
		Mockito.when(webHookHistoryRepository.findHistoryAllItemsGroupedByDayInclusive(ArgumentMatchers.eq(LocalDate.parse("2020-01-01")), ArgumentMatchers.eq(0))).thenReturn(buildStats(LocalDateTime.parse("2020-01-01T04:30:00")));
		statisticsManager.updateStatistics(LocalDateTime.parse("2020-01-01T05:00:00.000"));
		assertEquals(2, jaxHelper.getWriteCount());
		assertEquals(LocalDateTime.parse("2020-01-01T05:00:00.000"), jaxHelper.getLastBean().getLastUpdated());
		
	}
}
