package webhook.teamcity.testing;

import java.util.ArrayList;
import java.util.TreeMap;

import webhook.teamcity.settings.CustomMessageTemplate;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public class WebHookConfigFactoryImpl implements WebHookConfigFactory {
	
	@Override
	public WebHookConfig build(WebHookExecutionRequest webHookExecutionRequest) {
		return WebHookConfig.builder()
					 .url(webHookExecutionRequest.getUrl())
					 .templates(new TreeMap<String,CustomMessageTemplate>())
					 .authEnabled(webHookExecutionRequest.isAuthEnabled())
					 .authType(webHookExecutionRequest.getAuthType())
					 .authParameters(webHookExecutionRequest.getAuthParameters())
					 .filters(new ArrayList<WebHookFilterConfig>())
					 .build();
	}

	@Override
	public WebHookConfig build(WebHookTemplateExecutionRequest webHookExecutionRequest) {
		// TODO Auto-generated method stub
		return null;
	}

}
