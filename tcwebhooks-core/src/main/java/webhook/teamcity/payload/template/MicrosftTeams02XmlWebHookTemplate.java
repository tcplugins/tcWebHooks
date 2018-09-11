package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class MicrosftTeams02XmlWebHookTemplate extends AbstractXmlBasedWebHookTemplate implements WebHookPayloadTemplate {
	
	private static final String CONF_PROPERTIES = "webhook/teamcity/payload/template/MicrosoftTeams01WebHookTemplate.xml";
	
	public MicrosftTeams02XmlWebHookTemplate(
			WebHookTemplateManager templateManager,
			WebHookPayloadManager payloadManager,
			WebHookTemplateJaxHelper webHookTemplateJaxHelper) {
		super(templateManager, payloadManager, webHookTemplateJaxHelper);
	}

	@Override
	public String getLoggingName() {
		return "MicrosoftTeams01XmlWebHookTemplate";
	}

	@Override
	public String getXmlFileName() {
		return CONF_PROPERTIES;
	}

}
