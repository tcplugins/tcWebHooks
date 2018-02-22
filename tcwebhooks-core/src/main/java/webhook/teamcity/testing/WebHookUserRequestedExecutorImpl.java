package webhook.teamcity.testing;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public class WebHookUserRequestedExecutorImpl {
	
	private final WebHookConfigFactory myWebHookConfigFactory;
	private final WebHookContentBuilder myWebHookContentBuilder;
	private final WebHookPayloadManager myWebHookPayloadManager;
	public WebHookUserRequestedExecutorImpl(
			WebHookConfigFactory webHookConfigFactory,
			WebHookContentBuilder webHookContentBuilder,
			WebHookPayloadManager webHookPayloadManager
			) {
		myWebHookConfigFactory = webHookConfigFactory;
		myWebHookContentBuilder = webHookContentBuilder;
		myWebHookPayloadManager = webHookPayloadManager;
	}
	
	public WebHookHistoryItem requestWebHookExecution(WebHookExecutionRequest webHookExecutionRequest) {
		WebHookConfig webHookConfig = myWebHookConfigFactory.build(webHookExecutionRequest);
		
		// We need an alternative WebHookTemplateManager
		
		
		//WebHook
		
		return null;
	}
	
	/** Method that builds a templete from the webHookTemplateExecutionRequest and then 
	 *  executes the webhook.
	 *  
	 *   Webhook config could be a URL from the user, or a webhook config id.
	 *   
	 * @param webHookTemplateExecutionRequest
	 * @return
	 */
	public WebHookHistoryItem requestWebHookExecution(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest) {
		WebHookConfig webHookConfig = myWebHookConfigFactory.build(webHookTemplateExecutionRequest);
		
		// We need an alternative WebHookTemplateManager. We'll use the injected payload manager, but create our
		// own jaxHelper It's only used to persist the template, which we won't do in this stage.
		WebHookTemplateManager webHookTemplateManager = new WebHookTemplateManager(myWebHookPayloadManager, new NoOpJaxHelper());
		
		
		
		//WebHook
		
		return null;
	}
	
	private static class  NoOpJaxHelper implements WebHookTemplateJaxHelper {

		/**
		 * Not used.
		 */
		@Override
		public WebHookTemplateEntity readTemplate(String configFilePath) throws JAXBException, FileNotFoundException {
			return null;
		}

		/**
		 * Not used
		 */
		@Override
		public WebHookTemplateEntity readTemplate(InputStream stream) throws JAXBException {
			return null;
		}

		/**
		 * Not used
		 */
		@Override
		public void writeTemplate(WebHookTemplateEntity templates, String configFilePath) throws JAXBException {
			// Not used
		}

		/**
		 * Not used
		 */
		@Override
		public WebHookTemplates readTemplates(String configFilePath) throws JAXBException, FileNotFoundException {
			return null;
		}

		/**
		 * Not Used
		 */
		@Override
		public WebHookTemplates readTemplates(InputStream stream) throws JAXBException {
			return null;
		}

		/**
		 * NoOp
		 */
		@Override
		public void writeTemplates(WebHookTemplates templates, String configFilePath) throws JAXBException {
			// No Op
		}
		
	}

}
