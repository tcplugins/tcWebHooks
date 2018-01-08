package webhook.teamcity.testing;

import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookUserRequestedExecutorImpl {
	
	private final WebHookConfigFactory myWebHookConfigFactory;
	public WebHookUserRequestedExecutorImpl(WebHookConfigFactory webHookConfigFactory) {
		myWebHookConfigFactory = webHookConfigFactory;
	}
	
	public WebHookHistoryItem requestWebHookExectuion(WebHookExecutionRequest webHookExecutionRequest) {
		WebHookConfig webHookConfig = myWebHookConfigFactory.build(webHookExecutionRequest);
		
		return null;
	}

}
