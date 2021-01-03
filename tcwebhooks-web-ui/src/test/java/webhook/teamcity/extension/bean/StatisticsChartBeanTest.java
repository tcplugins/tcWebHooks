package webhook.teamcity.extension.bean;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;

import webhook.teamcity.statistics.StatisticsEntity;

public class StatisticsChartBeanTest {

	@Test
	public void testAssemble() {
		List<StatisticsEntity> historicalStatistics = new ArrayList<>();
		System.out.println(
				StatisticsChartBean.assemble(
						LocalDate.now().minusDays(10), 
						LocalDate.now(), 
						historicalStatistics
					).toJson());
	}

}
