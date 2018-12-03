package webhook.teamcity.payload.variableresolver;

import java.util.Map;

import jetbrains.buildServer.log.Loggers;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;
import webhook.teamcity.payload.util.VariableMessageBuilder;
import webhook.teamcity.payload.util.WebHookVariableMessageBuilder;

public class WebHooksBeanUtilsVariableResolverFactory implements VariableResolverFactory {
	
	WebHookVariableResolverManager variableResolverManager;
	
	@Override
	public void register() {
		Loggers.SERVER.info("WebHooksBeanUtilsVariableResolverFactory :: Registering for type: " + getPayloadTemplateType().toString());
		this.variableResolverManager.registerVariableResolverFactory(this);
	}
	
	@Override
	public void setWebHookVariableResolverManager(WebHookVariableResolverManager variableResolverManager) {
		this.variableResolverManager = variableResolverManager;
	}

	@Override
	public PayloadTemplateEngineType getPayloadTemplateType() {
		return PayloadTemplateEngineType.STANDARD;
	}

	@Override
	public String getVariableResolverFactoryName() {
		return this.getClass().getSimpleName();
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
