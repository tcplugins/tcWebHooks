package webhook.teamcity.testing;

import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public interface WebHookUserRequestedExecutor {

	WebHookHistoryItem requestWebHookExecution(WebHookExecutionRequest webHookExecutionRequest);

	/** Method that builds a template from the webHookTemplateExecutionRequest and then 
	 *  executes the webhook.
	 *  
	 *   Webhook config could be a URL from the user, or a webhook config id.
	 *   
	 * @param webHookTemplateExecutionRequest
	 * @return
	 */
	WebHookHistoryItem requestWebHookExecution(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest);

}