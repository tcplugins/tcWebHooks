package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJson;

public class LegacyJsonWebHookTemplate extends LegacyDeprecatedFormatWebHookTemplate {

	public static final String TEMPLATE_ID = "legacy-json";

	public LegacyJsonWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);
	}

	@Override
	public String getTemplateId() {
		return TEMPLATE_ID;
	}

	@Override
	public String getTemplateToolTip() {
		return "Send the payload formatted in JSON";
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return WebHookPayloadJson.FORMAT_SHORT_NAME.equalsIgnoreCase(payloadFormat);
	}

	@Override
	public String getLegacyFormat() {
		return WebHookPayloadJson.FORMAT_SHORT_NAME;
	}

}
