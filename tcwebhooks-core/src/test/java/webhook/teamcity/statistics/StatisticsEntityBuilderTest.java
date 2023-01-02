package webhook.teamcity.statistics;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import webhook.WebHookExecutionStats;
import webhook.teamcity.history.GeneralisedWebAddress;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebAddressTransformerImpl;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.settings.WebHookConfig;
import webhook.testframework.util.ConfigLoaderUtil;

public class StatisticsEntityBuilderTest {
	
	private String projectId = "project01";
	private String projectName ="My Project Name";
	private String buildTypeId ="bt01";
	private String buildTypeName = "My Build Type";
	private String buildTypeExternalId ="MyProjectName_MyBuildType";
	private Long buildId = 10000L;

	@Test
	public void testThatCountsAreDefinedCorrectly() {
		
		StatisticsEntity statsEntity = new StatisticsEntityBuilder()
				.counts(100, 250, 1575, 333000)
				.forDate(LocalDate.parse("2020-01-01"))
				.atTime(LocalDateTime.parse("2020-01-01T00:30:00"))
				.build();
		
		assertEquals(100, statsEntity.getStatisticsSnapshot().getOkCount());
		assertEquals(250, statsEntity.getStatisticsSnapshot().getErrorCount());
		assertEquals(1575, statsEntity.getStatisticsSnapshot().getSkippedCount());
		assertEquals(333000, statsEntity.getStatisticsSnapshot().getTotalCount());
	}
	
	@Test
	public void testThatTemplateStatsAreDefinedCorrectly() throws JDOMException, IOException {
		
		LocalDate myDate = LocalDate.parse("2020-01-01");
		
		StatisticsEntity existingstatsEntity = new StatisticsEntityBuilder()
				.counts(100, 250, 1575, 333000)
				.forDate(LocalDate.parse("2020-01-01"))
				.atTime(LocalDateTime.parse("2020-01-01T00:30:00"))
				.build();
		
		StatisticsEntity statsEntity = new StatisticsEntityBuilder()
				.existingStatisticsEntity(existingstatsEntity)
				.stats(buildStats(LocalDateTime.parse("2020-01-01T01:30:00")))
				.forDate(myDate)
				.atTime(LocalDateTime.parse("2020-01-01T01:30:00"))
				.build();
		

		
		assertEquals(101, statsEntity.getStatisticsSnapshot().getOkCount());
		assertEquals(250, statsEntity.getStatisticsSnapshot().getErrorCount());
		assertEquals(1575, statsEntity.getStatisticsSnapshot().getSkippedCount());
		assertEquals(333001, statsEntity.getStatisticsSnapshot().getTotalCount());
	}

	private Map<LocalDate, List<WebHookHistoryItem>> buildStats(LocalDateTime timestamp) throws JDOMException, IOException {
		WebAddressTransformer webAddressTransformer = new WebAddressTransformerImpl();
		
		WebHookConfig webHookConfig = ConfigLoaderUtil.getFirstWebHookInConfig(new File("src/test/resources/project-settings-test-all-states-enabled.xml"));
		WebHookExecutionStats webHookExecutionStats = new WebHookExecutionStats();
		webHookExecutionStats.setStatusCode(200);
		webHookExecutionStats.setStatusReason("OK");
		WebHookErrorStatus webhookErrorStatus = null;
		GeneralisedWebAddress generalisedWebAddress = webAddressTransformer.getGeneralisedHostName(webHookConfig.getUrl());
		WebHookHistoryItem item = new WebHookHistoryItem(projectId , projectName , buildTypeId , buildTypeName, buildTypeExternalId, buildId, webHookConfig, webHookExecutionStats, webhookErrorStatus, timestamp, generalisedWebAddress, false);
		Map<LocalDate, List<WebHookHistoryItem>> items = new HashMap<>();
		items.put(timestamp.toLocalDate(), Collections.singletonList(item));
		return items;
	}

}
