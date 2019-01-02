package webhook.teamcity.payload.template;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadTailoredJson;

import java.util.Arrays;

public class LegacyTailoredJsonWebHookTemplate extends LegacyDeprecatedFormatWebHookTemplate {

	public static final String TEMPLATE_ID = "legacy-tailored-json";

	public LegacyTailoredJsonWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);

		// We can't handle these states because we don't have access to the running or finished build.
		getSupportedBuildStates().removeAll(Arrays.asList(
													BuildStateEnum.BUILD_ADDED_TO_QUEUE,
													BuildStateEnum.BUILD_REMOVED_FROM_QUEUE,
													BuildStateEnum.RESPONSIBILITY_CHANGED)
											);
	}

	@Override
	public String getTemplateId() {
		return TEMPLATE_ID;
	}

	@Override
	public String getTemplateToolTip() {
		return "Send a JSON payload with content specified by parameter named 'body'";
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return WebHookPayloadTailoredJson.FORMAT_SHORT_NAME.equalsIgnoreCase(payloadFormat);
	}

	@Override
	public String getLegacyFormat() {
		return WebHookPayloadTailoredJson.FORMAT_SHORT_NAME;
	}

}
