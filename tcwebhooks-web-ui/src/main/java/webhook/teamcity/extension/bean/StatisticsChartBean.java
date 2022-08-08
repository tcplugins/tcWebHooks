package webhook.teamcity.extension.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import webhook.teamcity.WebHookExecutionException;
import webhook.teamcity.statistics.LocalDateTypeAdaptor;
import webhook.teamcity.statistics.StatisticsEntity;
import webhook.teamcity.statistics.StatisticsSnapshot.StatisticsItem;

public class StatisticsChartBean {

	private static final Gson GSON = new GsonBuilder()
			 .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdaptor())
			 .serializeNulls()
			 .setPrettyPrinting()
			 .create();
	
	private static final Map<Integer, String> statusColours = new ImmutableMap.Builder<Integer, String>()
			.put(200, "rgba(56, 231, 106, 0.9)")
			.put(201, "rgba(56, 231, 106, 0.8)")
			.put(202, "rgba(56, 231, 106, 0.7)")
			.put(203, "rgba(56, 231, 106, 0.6)")
			.put(204, "rgba(56, 231, 106, 0.5)")
			.put(300, "rgba(12, 176, 242, 0.5)")
			.put(301, "rgba(12, 176, 242, 0.4)")
			.put(302, "rgba(12, 176, 242, 0.3)")
			.put(303, "rgba(12, 176, 242, 0.2)")
			.put(304, "rgba(12, 176, 242, 0.1)")
			.put(400, "rgba(132, 99, 250, 0.5)")
			.put(401, "rgba(132, 99, 250, 0.4)")
			.put(402, "rgba(132, 99, 250, 0.3)")
			.put(403, "rgba(132, 99, 250, 0.2)")
			.put(404, "rgba(132, 99, 250, 0.1)")
			.put(500, "rgba(255, 49, 140, 0.5)")
			.put(501, "rgba(255, 49, 140, 0.4)")
			.put(502, "rgba(255, 49, 140, 0.3)")
			.put(503, "rgba(255, 49, 140, 0.2)")
			.put(504, "rgba(255, 49, 140, 0.1)")
			.put(WebHookExecutionException.WEBHOOK_DISABLED_INFO_CODE, "rgba(55, 55, 55, 0.5)")
			.put(WebHookExecutionException.WEBHOOK_DISABLED_BY_FILTER_INFO_CODE, "rgba(55, 55, 55, 0.2)")
			
			.put(WebHookExecutionException.WEBHOOK_EXECUTION_ERROR_CODE, "rgba(255, 49, 140, 0.5)")
			
			.put(WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE, "rgba(255, 131, 49, 0.8)")
			.put(WebHookExecutionException.WEBHOOK_PAYLOAD_CONTENT_ASSEMBLY_EXCEPTION_ERROR_CODE, "rgba(255, 131, 49, 0.7)")
			.put(WebHookExecutionException.WEBHOOK_VARIABLE_RESOLVER_NOT_FOUND_EXCEPTION_ERROR_CODE, "rgba(255, 131, 49, 0.6)")
			.put(WebHookExecutionException.WEBHOOK_CONTENT_RESOLUTION_ERROR_CODE, "rgba(255, 131, 49, 0.5)")
			.put(WebHookExecutionException.TEMPLATE_NOT_FOUND_ERROR_CODE, "rgba(255, 131, 49, 0.4)")
			.put(WebHookExecutionException.UNSUPPORTED_BUILDSTATE_EXCEPTION_ERROR_CODE, "rgba(255, 131, 49, 0.3)")
			.put(WebHookExecutionException.UNSUPPORTED_WEBHOOK_FORMAT_EXCEPTION_ERROR_CODE, "rgba(255, 131, 49, 0.2)")
			.put(WebHookExecutionException.WEBHOOK_CONFIGURATION_NOT_FOUND_EXCEPTION_ERROR_CODE, "rgba(255, 131, 49, 0.1)")
			.build();

	protected List<Dataset> datasets = new ArrayList<>();

	public static StatisticsChartBean assemble(LocalDate startDate, LocalDate endDate, List<StatisticsEntity> historicalStatistics) {

		StatisticsChartBean bean = new StatisticsChartBean();

		int days = Days.daysBetween(startDate, endDate).getDays();
		//Arrays.asList(200, 201, 202, 203, 204, 300, 301, 302, 303, 304, 400, 401, 402, 403, 404, 405, 406, 500, 501, 502, 503, 504, 600, 601, 602, 603, 604, 605, 700, 701, 702, 703, 704, 705, 800, 801, 802, 803, 804, 805, 900, 901, 903)
		statusColours.keySet()
		.stream()
		.forEach(statusCode -> {
			Dataset dataset = new Dataset();
			dataset.setBackgroundColor(statusColours.get(statusCode));
			dataset.setLabel(statusCode.toString());
			for(int i = 0; i < days; i++) {
				LocalDate date = startDate.plusDays(i);
				Data data = new Data(date, 0);
				List<Collection<StatisticsItem>> items = historicalStatistics
					.stream()
					.filter(entity -> entity.getStatisticsSnapshot().getDate().equals(date))
					.map(entity -> entity.getStatisticsSnapshot().getUrls().values())
					.collect(Collectors.toList());

				items.forEach(l -> l.forEach(item -> {
					int sum = item.getStatuses().entrySet()
							.stream()
							.filter(e -> e.getKey().equals(statusCode))
							.mapToInt(Map.Entry::getValue)
							.sum();
					data.setY(data.getY() + sum);
				}));
				dataset.addDataItem(data);
			}
			bean.datasets.add(dataset);
		});

		return bean;
	}

	public String toJson() {
		return GSON.toJson(this);
	}

	@Getter @Setter
	public static final class Dataset {
		private String label;
		private List<Data> data = new ArrayList<>();
		private String backgroundColor = "rgba(132, 99, 250, 0.5)";
		//private String backgroundColor = "rgba(56, 231, 106, 0.5)";
		private String borderColor = "rgba(132, 99, 250, 1)";
		//private String borderColor = "rgba(56, 231, 106, 1)";
		private int borderWidth = 1;

		public Dataset addDataItem(Data item) {
			this.data.add(item);
			return this;
		}
	}

	@Getter @Setter @AllArgsConstructor
	public static final class Data {
		private LocalDate t;
		private Integer y;
	}

}
