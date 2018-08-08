package webhook.teamcity.testing;

import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookRenderResult;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public interface WebHookUserRequestedExecutor {
	/** Method that builds a {@link WebHookConfig} from the {@link WebHookExecutionRequest} and then 
	 *  returns a preview ({@link WebHookRenderResult}) of the webhook payload but does not actually 
	 *  execute the webhook.
	 *  
	 *   WebHookConfig is built from the fields on the webhook edit page.
	 *   WebHook does not need to be persisted.
	 *   
	 * @param webHookTemplateExecutionRequest
	 * @return htmlString
	 */
	WebHookRenderResult requestWebHookPreview(WebHookExecutionRequest webHookExecutionRequest);
	
	/** Method that builds a template from the webHookTemplateExecutionRequest and then 
	 *  returns a preview ({@link WebHookRenderResult}) of the webhook payload but does not actually 
	 *  execute the webhook.
	 *  
	 *   Webhook config could be a URL from the user, or a webhook config id.
	 *   
	 * @param webHookTemplateExecutionRequest
	 * @return htmlString
	 */
	WebHookRenderResult requestWebHookPreview(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest);
	
	/** Method that builds a webhook config from the {@link WebHookExecutionRequest} and executes the webhook.
	 * Intended to be used from the WebHook edit screen (not the template edit screen).
	 * 
	 * @param webHookExecutionRequest
	 * @return
	 */
	WebHookHistoryItem requestWebHookExecution(WebHookExecutionRequest webHookExecutionRequest);

	/** Method that builds a template from the webHookTemplateExecutionRequest and then 
	 *  executes the webhook.
	 *  
	 *   Webhook config could be a URL from the user, or a webhook config id.
	 *   
	 * @param webHookTemplateExecutionRequest
	 * @return WebHookHistoryItem
	 */
	WebHookHistoryItem requestWebHookExecution(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest);


}