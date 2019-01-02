package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadEmpty;

public class LegacyEmptyWebHookTemplate extends LegacyDeprecatedFormatWebHookTemplate {

	public static final String TEMPLATE_ID = "legacy-empty";

	public LegacyEmptyWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);
	}

	@Override
	public String getTemplateId() {
		return TEMPLATE_ID;
	}

	@Override
	public String getTemplateToolTip() {
		return "Send a POST request with no content";
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return WebHookPayloadEmpty.FORMAT_SHORT_NAME.equalsIgnoreCase(payloadFormat);
	}

	@Override
	public String getLegacyFormat() {
		return WebHookPayloadEmpty.FORMAT_SHORT_NAME;
	}
}
