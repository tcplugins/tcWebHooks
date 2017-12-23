package webhook.teamcity.reporting;

import java.util.List;

import webhook.WebHookExecutionStats;

public interface ReportPayloadFactory {
	
	public ReportPayload buildReportPayload(List<WebHookExecutionStats> webHookExecutionStats);

}
