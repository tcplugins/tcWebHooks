package webhook.teamcity.payload.variableresolver;

import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.secure.WebHookSecretResolverFactory;

public interface VariableResolverFactory {
	
	public abstract void register();
	public abstract void setWebHookVariableResolverManager(WebHookVariableResolverManager variableResolverManager);
	public abstract void setWebHookSecretResolverFactory(WebHookSecretResolverFactory webHookSecretResolverFactory);
	public abstract PayloadTemplateEngineType getPayloadTemplateType();
	public abstract String getVariableResolverFactoryName();
	public abstract VariableMessageBuilder createVariableMessageBuilder(VariableResolver resolver);
	public abstract VariableResolver buildVariableResolver(SProject sProject, WebHookContentObjectSerialiser webhookPayload, Object javaBean, ExtraParameters extraAndTeamCityProperties);
	

}
