package webhook.teamcity.statistics;

import java.util.List;

public interface StatisticsReportAssembler {
	
	/**
	 * Assemble all statistics into a report.
	 * Sensitive values can be hashed by passing in an a {@link ValueHasher}
	 * which calls crypt. Alternatively, pass in a non-encrypting ValueHasher.
	 * 
	 * @param hasher - Hasher to pass sensitive values through.
	 * @param statisticsEntities - A List of StatisticsEntity already assembled for the required dates.
	 * @return A {@link StatisticsReport} relevant to the dates requested.
	 */
	public StatisticsReport assembleStatisticsReports(ValueHasher hasher, List<StatisticsEntity> statisticsEntities);

	/**
	 * Assemble info about the version of the webhooks plugin(s).
	 * 
	 * @param hasher - Hasher to pass sensitive values through (if any)
	 * @return A {@link WebHooksPluginInfo} model
	 */
	public WebHooksPluginInfo assembleWebHooksPluginInfo(ValueHasher hasher);
	
	/**
	 * Assemble info about the TeamCiy instance.
	 * 
	 * @param hasher - Hasher to pass sensitive values through (if any)
	 * @return A {@link TeamCityInstanceInfo} model
	 */
	public TeamCityInstanceInfo assembleTeamCityInstanceInfo(ValueHasher hasher);
	
	/**
	 * Assemble statistics about the enabled WebHook configurations.
	 * 
	 * @param hasher - Hasher to pass sensitive values through (if any)
	 * @return A {@link WebHookConfigurationStatistics} model
	 */
	public WebHookConfigurationStatistics assembleWebHookConfigurationStatistics(ValueHasher hasher);
	
	/**
	 * Assemble statistics about the WebHook executions within the date range.
	 * 
	 * @param hasher - Hasher to pass sensitive values through (if any)
	 * @param fromDate - Start date for statistics.
	 * @param toDate - End date (non-inclusive) for statistics.
	 * @return List of {@link StatisticsSnapshot}
	 */
	public List<StatisticsSnapshot>  assembleWebHookStatisticsReports(ValueHasher hasher, List<StatisticsEntity> statisticsEntities);

	public enum ReportFormatType { PLAIN, HASHED }

}
