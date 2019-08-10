package webhook.teamcity.settings.converter;

import java.util.Objects;

import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.payload.format.WebHookPayloadTailoredJson;
import webhook.teamcity.payload.template.LegacyJsonWebHookTemplate;
import webhook.teamcity.payload.template.LegacyNameValuePairsWebHookTemplate;
import webhook.teamcity.payload.template.LegacyTailoredJsonWebHookTemplate;

public class PayloadToTemplateConverter {

	private PayloadToTemplateConverter() {}

	public static String transformPayloadToTemplate(String formatId, String templateId) {

		if (Objects.isNull(templateId) || "none".equalsIgnoreCase(templateId)) {

			if (WebHookPayloadJson.FORMAT_SHORT_NAME.equalsIgnoreCase(formatId)) {
				return LegacyJsonWebHookTemplate.TEMPLATE_ID;
			}
			if (WebHookPayloadTailoredJson.FORMAT_SHORT_NAME.equalsIgnoreCase(formatId)) {
				return LegacyTailoredJsonWebHookTemplate.TEMPLATE_ID;
			}

			return LegacyNameValuePairsWebHookTemplate.TEMPLATE_ID;
		}

		return templateId;

	}

}
