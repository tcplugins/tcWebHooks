package webhook.teamcity.payload.variableresolver;

import java.util.Map;

import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;
import webhook.teamcity.payload.util.VariableMessageBuilder;

public interface VariableResolverFactory {
	
	public abstract PayloadTemplateEngineType getPayloadTemplateType();
	public abstract String getVariableResolverFactoryName();
	public abstract VariableMessageBuilder createVariableMessageBuilder(final String template, VariableResolver resolver);
	public abstract VariableResolver buildVariableResolver(WebHookContentObjectSerialiser webhookPayload, Object javaBean, Map<String, ExtraParametersMap> extraAndTeamCityProperties);
	

}
