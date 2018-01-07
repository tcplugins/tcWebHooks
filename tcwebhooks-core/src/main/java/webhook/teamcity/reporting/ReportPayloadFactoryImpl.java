package webhook.teamcity.reporting;

import java.util.List;

import webhook.WebHookExecutionStats;
import webhook.teamcity.history.WebAddressTransformer;

public class ReportPayloadFactoryImpl implements ReportPayloadFactory {
	
	private final WebAddressTransformer myWebAddressTransformer;
	
	public ReportPayloadFactoryImpl(WebAddressTransformer webAddressTransformer) {
		myWebAddressTransformer = webAddressTransformer;
	}

	@Override
	public ReportPayload buildReportPayload(List<WebHookExecutionStats> webHookExecutionStats) {
		// TODO Auto-generated method stub
		return null;
	}

}
