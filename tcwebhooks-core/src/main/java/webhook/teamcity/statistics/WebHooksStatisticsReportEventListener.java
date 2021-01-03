package webhook.teamcity.statistics;

import webhook.teamcity.settings.WebHookConfig;

public interface WebHooksStatisticsReportEventListener {
	
	/**
	 * Send the {@link StatisticsReport} using the {@link WebHookConfig}
	 * @param webHookConfig
	 * @param statisticsReport
	 */
	public void reportStatistics(WebHookConfig whc, StatisticsReport statisticsReport);
	
	/**
	 * Send the {@link StatisticsReport}
	 * @param statisticsReport
	 */
	public void reportStatistics(StatisticsReport statisticsReport);

}
