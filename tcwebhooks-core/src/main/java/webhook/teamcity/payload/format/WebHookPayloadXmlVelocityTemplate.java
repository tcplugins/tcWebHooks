package webhook.teamcity.payload.format;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;

public class WebHookPayloadXmlVelocityTemplate extends WebHookPayloadXmlTemplate implements WebHookPayload, WebHookContentObjectSerialiser {

	public static final String FORMAT_SHORT_NAME = "xmlVelocityTemplate";

	public WebHookPayloadXmlVelocityTemplate(WebHookPayloadManager manager, WebHookVariableResolverManager variableResolverManager) {
		super(manager, variableResolverManager);
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}

	@Override
	public String getFormatDescription() {
		return "XML Velocity template";
	}

	@Override
	public String getFormatToolTipText() {
		return "Send an XML payload with content from a Velocity template";
	}

	@Override
	public Object serialiseObject(Object object) {
		return object;
	}

	@Override
	public PayloadTemplateEngineType getTemplateEngineType() {
		return PayloadTemplateEngineType.VELOCITY;
	}

}
