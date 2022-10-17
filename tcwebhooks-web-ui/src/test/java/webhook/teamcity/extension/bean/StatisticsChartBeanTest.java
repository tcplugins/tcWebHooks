package webhook.teamcity.extension.bean;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.extension.bean.StatisticsChartBean.Data;
import webhook.teamcity.extension.bean.StatisticsChartBean.Dataset;
import webhook.teamcity.statistics.StatisticsEntity;
import webhook.teamcity.statistics.StatisticsSnapshot;
import webhook.teamcity.statistics.StatisticsSnapshot.StatisticsItem;
import webhook.teamcity.statistics.StatisticsSnapshot.StatisticsItemStatus;

public class StatisticsChartBeanTest {

	@Test
	public void testAssemble() {
		List<StatisticsEntity> historicalStatistics = new ArrayList<>();
		historicalStatistics.add(createMockedHistory("2021-01-01"));
		
		StatisticsChartBean bean = StatisticsChartBean.assemble(
						LocalDate.parse("2021-01-10").minusDays(10), 
						LocalDate.parse("2021-01-10"), 
						historicalStatistics
					);
		
		System.out.println(bean.toJson());
		assertEquals(Integer.valueOf(70), getDataSetFor("2021-01-01", 200, bean.datasets).getY());
		assertEquals(Integer.valueOf(1010), getDataSetFor("2021-01-01", 500, bean.datasets).getY());
		assertEquals(Integer.valueOf(25), getDataSetFor("2021-01-01", 302, bean.datasets).getY());
	}

	private StatisticsEntity createMockedHistory(String string) {
		
		Map<String, StatisticsItem> urls = new HashMap<>();
		
		List<StatisticsItemStatus> statusesLocalHost = new ArrayList<>();
		statusesLocalHost.add(new StatisticsItemStatus(BuildStateEnum.BUILD_STARTED,200, 20));
		statusesLocalHost.add(new StatisticsItemStatus(BuildStateEnum.BEFORE_BUILD_FINISHED, 500, 10));
		statusesLocalHost.add(new StatisticsItemStatus(BuildStateEnum.BUILD_ADDED_TO_QUEUE, 302, 10));
		urls.put("localhost", new StatisticsItem("localhost", 10, statusesLocalHost));
		
		List<StatisticsItemStatus> statusesSlack = new ArrayList<>();
		statusesSlack.add(new StatisticsItemStatus(BuildStateEnum.BUILD_STARTED, 200, 50));
		statusesSlack.add(new StatisticsItemStatus(BuildStateEnum.BEFORE_BUILD_FINISHED, 500, 1000));
		statusesSlack.add(new StatisticsItemStatus(BuildStateEnum.BUILD_ADDED_TO_QUEUE, 302, 15));
		urls.put("slack.com", new StatisticsItem("slack.com", 10, statusesSlack));
		
		StatisticsSnapshot statisticsSnapshot = new StatisticsSnapshot();
		statisticsSnapshot.setDate(LocalDate.parse(string));
		statisticsSnapshot.setUrls(urls);
		StatisticsEntity e = new StatisticsEntity();
		e.setStatisticsSnapshot(statisticsSnapshot);
		return e;
	}
	
	private Data getDataSetFor(String string, int i, List<Dataset> datasets) {
		return datasets
				.stream()
				.filter(d -> d.getLabel().equals(String.valueOf(i)))
				.findFirst()
				.get()
					.getData()
						.stream()
						.filter(s -> s.getT().equals(LocalDate.parse(string)))
						.findFirst()
						.get();
	}

}
