package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;

public class LegacyNameValuePairsWebHookTemplate extends LegacyDeprecatedFormatWebHookTemplate {

	public static final String TEMPLATE_ID = "legacy-nvpairs";

	public LegacyNameValuePairsWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);
	}

	@Override
	public String getLegacyFormat() {
		return WebHookPayloadNameValuePairs.FORMAT_SHORT_NAME;
	}

	@Override
	public String getTemplateId() {
		return TEMPLATE_ID;
	}

	@Override
	public String getTemplateToolTip() {
		return "Send the payload as a set of Name/Value Pairs (www-urlencoded as if posted from a form)";
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return WebHookPayloadNameValuePairs.FORMAT_SHORT_NAME.equalsIgnoreCase(payloadFormat);
	}

}
