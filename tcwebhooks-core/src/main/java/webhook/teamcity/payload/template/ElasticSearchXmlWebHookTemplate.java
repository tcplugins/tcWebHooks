package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class ElasticSearchXmlWebHookTemplate extends AbstractXmlBasedWebHookTemplate implements WebHookPayloadTemplate {
	
	
	public ElasticSearchXmlWebHookTemplate(
			WebHookTemplateManager templateManager,
			WebHookPayloadManager payloadManager,
			WebHookTemplateJaxHelper webHookTemplateJaxHelper) {
		super(templateManager, payloadManager, webHookTemplateJaxHelper);
	}

	String CONF_PROPERTIES = "webhook/teamcity/payload/template/ElasticSearchWebHookTemplate.xml";


	@Override
	public String getLoggingName() {
		return "ElasticSearchXmlWebHookTemplate";
	}

	@Override
	public String getXmlFileName() {
		return CONF_PROPERTIES;
	}

}
