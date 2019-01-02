package webhook.teamcity.payload.format;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;

public class WebHookPayloadNameValuePairsVelocityTemplate extends WebHookPayloadNameValuePairsTemplate implements WebHookPayload, WebHookContentObjectSerialiser {

	public static final String FORMAT_SHORT_NAME = "nvpairsVelocityTemplate";

	public WebHookPayloadNameValuePairsVelocityTemplate(WebHookPayloadManager manager, WebHookVariableResolverManager variableResolverManager) {
		super(manager, variableResolverManager);
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	@Override
	public String getFormatDescription() {
		return "Name Value Pairs - urlencoded Velocity template";
	}

	@Override
	public String getFormatToolTipText() {
		return "Send a x-www-form-urlencoded payload with content from a Velocity template";
	}

	@Override
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.VELOCITY;
	}

}
