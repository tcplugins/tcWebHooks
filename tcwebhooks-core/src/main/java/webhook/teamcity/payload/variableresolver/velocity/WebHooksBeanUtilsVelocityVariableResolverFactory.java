package webhook.teamcity.payload.variableresolver.velocity;

import java.util.Map;

import org.apache.velocity.context.Context;

import jetbrains.buildServer.log.Loggers;
import webhook.teamcity.WebHookContentResolutionException;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.VariableResolver;

public class WebHooksBeanUtilsVelocityVariableResolverFactory implements VariableResolverFactory {
	
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
		return PayloadTemplateEngineType.VELOCITY;
	}

	@Override
	public String getVariableResolverFactoryName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public VariableResolver buildVariableResolver(WebHookContentObjectSerialiser webhookPayload, Object javaBean,
			Map<String, ExtraParametersMap> extraAndTeamCityProperties) {
		return new WebHooksBeanUtilsVelocityVariableResolver(javaBean, extraAndTeamCityProperties);
	}

	@Override
	public VariableMessageBuilder createVariableMessageBuilder(String template, VariableResolver resolver) {
		if (resolver instanceof WebHooksBeanUtilsVelocityVariableResolver) {
			return WebHookVelocityVariableMessageBuilder.create(template, (Context)resolver);
		} 
		throw new WebHookContentResolutionException("Incompatible VariableResolver. It must implement Velocity Context");
	}

}
