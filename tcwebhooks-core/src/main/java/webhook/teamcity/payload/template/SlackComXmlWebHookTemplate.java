package webhook.teamcity.payload.template;

import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class SlackComXmlWebHookTemplate extends AbstractXmlBasedWebHookTemplate implements WebHookPayloadTemplate {

	private static final String CONF_PROPERTIES = "webhook/teamcity/payload/template/SlackComWebHookTemplate.xml";

	public SlackComXmlWebHookTemplate(
			WebHookTemplateManager templateManager,
			WebHookPayloadManager payloadManager,
			WebHookTemplateJaxHelper webHookTemplateJaxHelper,
			ProjectIdResolver projectIdResolver,
			DeferrableServiceManager deferrableServiceManager) {
		super(templateManager, payloadManager, webHookTemplateJaxHelper, projectIdResolver, deferrableServiceManager);
	}

	@Override
	public String getLoggingName() {
		return "SlackComWebHookTemplate";
	}

	@Override
	public String getXmlFileName() {
		return CONF_PROPERTIES;
	}

}
