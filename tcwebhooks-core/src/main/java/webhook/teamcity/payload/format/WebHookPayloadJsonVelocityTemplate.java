package webhook.teamcity.payload.format;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;

public class WebHookPayloadJsonVelocityTemplate extends WebHookPayloadJsonTemplate implements WebHookPayload, WebHookContentObjectSerialiser {

	public static final String FORMAT_SHORT_NAME = "jsonVelocityTemplate";

	public WebHookPayloadJsonVelocityTemplate(WebHookPayloadManager manager, WebHookVariableResolverManager variableResolverManager) {
		super(manager, variableResolverManager);
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	@Override
	public String getFormatDescription() {
		return "JSON Velocity template";
	}

	@Override
	public String getFormatToolTipText() {
		return "Send a JSON payload with content from a Velocity template";
	}

	@Override
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.VELOCITY;
	}

}
