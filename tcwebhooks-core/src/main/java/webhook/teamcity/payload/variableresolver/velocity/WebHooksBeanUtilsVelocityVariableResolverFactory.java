package webhook.teamcity.payload.variableresolver.velocity;

import org.apache.velocity.context.Context;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.WebHookContentResolutionException;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.settings.secure.WebHookSecretResolver;
import webhook.teamcity.settings.secure.WebHookSecretResolverFactory;
import webhook.teamcity.payload.variableresolver.VariableResolver;

public class WebHooksBeanUtilsVelocityVariableResolverFactory implements VariableResolverFactory {
	
	private WebHookVariableResolverManager variableResolverManager;
	private WebHookSecretResolver webHookSecretResolver;
	
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
	public void setWebHookSecretResolverFactory(WebHookSecretResolverFactory webHookSecretResolverFactory) {
		this.webHookSecretResolver = webHookSecretResolverFactory.getWebHookSecretResolver();
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
	public VariableResolver buildVariableResolver(SProject sProject, WebHookContentObjectSerialiser webhookPayload, Object javaBean,
			ExtraParameters extraAndTeamCityProperties) {
		return new WebHooksBeanUtilsVelocityVariableResolver(sProject, webhookPayload, javaBean, extraAndTeamCityProperties, webHookSecretResolver);
	}

	@Override
	public VariableMessageBuilder createVariableMessageBuilder(VariableResolver resolver) {
		if (resolver instanceof WebHooksBeanUtilsVelocityVariableResolver) {
			return WebHookVelocityVariableMessageBuilder.create((Context)resolver, webHookSecretResolver);
		} 
		throw new WebHookContentResolutionException("Incompatible VariableResolver. It must implement Velocity Context");
	}

}
