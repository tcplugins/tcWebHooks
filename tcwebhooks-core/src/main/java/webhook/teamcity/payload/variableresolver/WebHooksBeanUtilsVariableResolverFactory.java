package webhook.teamcity.payload.variableresolver;

import java.util.Map;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;
import webhook.teamcity.payload.util.VariableMessageBuilder;
import webhook.teamcity.payload.util.WebHookVariableMessageBuilder;

public class WebHooksBeanUtilsVariableResolverFactory implements VariableResolverFactory {

	@Override
	public PayloadTemplateEngineType getPayloadTemplateType() {
		return PayloadTemplateEngineType.STANDARD;
	}

	@Override
	public String getVariableResolverFactoryName() {
		return "WebHooksBeanUtilsVariableResolver";
	}

	@Override
	public VariableResolver buildVariableResolver(WebHookContentObjectSerialiser webhookPayload, Object javaBean,
			Map<String, ExtraParametersMap> extraAndTeamCityProperties) {
		return new WebHooksBeanUtilsVariableResolver(webhookPayload, javaBean, extraAndTeamCityProperties);
	}

	@Override
	public VariableMessageBuilder createVariableMessageBuilder(String template, VariableResolver resolver) {
		return WebHookVariableMessageBuilder.create(template, resolver);
	}

}
