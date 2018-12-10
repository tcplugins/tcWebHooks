package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class TestNvPairsXmlTemplate extends AbstractXmlBasedWebHookTemplate {
	
	private static final String CONF_PROPERTIES = "webhook/teamcity/payload/template/TestNVPairsTemplate.xml";


	public TestNvPairsXmlTemplate(WebHookTemplateManager templateManager, WebHookPayloadManager payloadManager,
			WebHookTemplateJaxHelper webHookTemplateJaxHelper) {
		super(templateManager, payloadManager, webHookTemplateJaxHelper);
	}

	@Override
	public String getXmlFileName() {
		return CONF_PROPERTIES;
	}

	@Override
	public String getLoggingName() {
		return "TestNvPairsXmlTemplate";
	}

}
