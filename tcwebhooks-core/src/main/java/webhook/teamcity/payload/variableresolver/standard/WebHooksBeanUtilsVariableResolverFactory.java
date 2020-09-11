package webhook.teamcity.payload.variableresolver.standard;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.settings.secure.WebHookSecretResolver;
import webhook.teamcity.settings.secure.WebHookSecretResolverFactory;
import webhook.teamcity.payload.variableresolver.VariableResolver;

public class WebHooksBeanUtilsVariableResolverFactory implements VariableResolverFactory {
	
	WebHookVariableResolverManager variableResolverManager;
	WebHookSecretResolver webHookSecretResolver;
	
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
		return PayloadTemplateEngineType.STANDARD;
	}

	@Override
	public String getVariableResolverFactoryName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public VariableResolver buildVariableResolver(SProject sProject, WebHookContentObjectSerialiser webhookPayload, Object javaBean,
			ExtraParameters extraAndTeamCityProperties) {
		return new WebHooksBeanUtilsVariableResolver(sProject, webhookPayload, javaBean, extraAndTeamCityProperties, webHookSecretResolver);
	}

	@Override
	public VariableMessageBuilder createVariableMessageBuilder(VariableResolver resolver) {
		return WebHookVariableMessageBuilder.create(resolver);
	}

}
