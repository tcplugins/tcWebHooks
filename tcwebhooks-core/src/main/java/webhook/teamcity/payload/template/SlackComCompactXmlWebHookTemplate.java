package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class SlackComCompactXmlWebHookTemplate extends AbstractXmlBasedWebHookTemplate implements WebHookPayloadTemplate {
	
	
	public SlackComCompactXmlWebHookTemplate(
			WebHookTemplateManager templateManager,
			WebHookPayloadManager payloadManager,
			WebHookTemplateJaxHelper webHookTemplateJaxHelper) {
		super(templateManager, payloadManager, webHookTemplateJaxHelper);
	}

	String CONF_PROPERTIES = "webhook/teamcity/payload/template/SlackComCompactWebHookTemplate.xml";


	@Override
	public String getLoggingName() {
		return "SlackComCompactXmlWebHookTemplate";
	}

	@Override
	public String getXmlFileName() {
		return CONF_PROPERTIES;
	}

}
