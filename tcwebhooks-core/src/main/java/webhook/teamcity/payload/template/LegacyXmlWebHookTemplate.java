package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadXml;

public class LegacyXmlWebHookTemplate extends LegacyDeprecatedFormatWebHookTemplate {

	public static final String TEMPLATE_ID = "legacy-xml";

	public LegacyXmlWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);
	}

	@Override
	public String getTemplateId() {
		return TEMPLATE_ID;
	}

	@Override
	public String getTemplateToolTip() {
		return "Send the payload formatted in XML";
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return WebHookPayloadXml.FORMAT_SHORT_NAME.equalsIgnoreCase(payloadFormat);
	}

	@Override
	public String getLegacyFormat() {
		return WebHookPayloadXml.FORMAT_SHORT_NAME;
	}


}
