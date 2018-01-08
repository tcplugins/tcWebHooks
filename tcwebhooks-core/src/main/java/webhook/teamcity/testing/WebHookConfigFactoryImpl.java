package webhook.teamcity.testing;

import webhook.teamcity.settings.WebHookConfig;

public class WebHookConfigFactoryImpl implements WebHookConfigFactory {
	
	@Override
	public WebHookConfig build(WebHookExecutionRequest webHookExecutionRequest) {
		return WebHookConfig.builder()
					 .authEnabled(webHookExecutionRequest.isAuthEnabled())
					 //.authParameters(we)
					 .build();
	}

}
