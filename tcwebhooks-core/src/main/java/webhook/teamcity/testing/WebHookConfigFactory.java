package webhook.teamcity.testing;

import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public interface WebHookConfigFactory {

	WebHookConfig build(WebHookExecutionRequest webHookExecutionRequest);
	WebHookConfig build(WebHookTemplateExecutionRequest webHookExecutionRequest);

}