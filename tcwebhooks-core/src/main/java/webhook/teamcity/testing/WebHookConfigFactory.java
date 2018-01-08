package webhook.teamcity.testing;

import webhook.teamcity.settings.WebHookConfig;

public interface WebHookConfigFactory {

	WebHookConfig build(WebHookExecutionRequest webHookExecutionRequest);

}