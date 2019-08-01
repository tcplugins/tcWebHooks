package webhook.teamcity.settings.converter;

import java.util.Objects;

import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.payload.format.WebHookPayloadTailoredJson;

public class PayloadToTemplateConverter {
	
	
	public static String transformPayloadToTemplate(String formatId, String templateId) {
		
		if (Objects.isNull(templateId) || "none".equalsIgnoreCase(templateId)) {
		
			if (WebHookPayloadJson.FORMAT_SHORT_NAME.equalsIgnoreCase(formatId)) {
				return "legacy-json"; // TODO: Use constant from Template
			}
			if (WebHookPayloadTailoredJson.FORMAT_SHORT_NAME.equalsIgnoreCase(formatId)) {
				return "legacy-tailored-json"; // TODO: Use constant from Template
			}
			
			return "legacy-nvpairs"; // TODO: Use constant from Template
		}
		
		return templateId;
		
	}

}
