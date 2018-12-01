package webhook.teamcity.payload.variableresolver.standard;

import jetbrains.buildServer.log.Loggers;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;

public class WebHooksBeanUtilsLegacyVariableResolverFactory extends WebHooksBeanUtilsVariableResolverFactory implements VariableResolverFactory {
	
	@Override
	public void register() {
		Loggers.SERVER.info("WebHooksBeanUtilsLegacyVariableResolverFactory :: Registering for type: " + getPayloadTemplateType().toString());
		this.variableResolverManager.registerVariableResolverFactory(this);
	}
	
	@Override
	public PayloadTemplateEngineType getPayloadTemplateType() {
		return PayloadTemplateEngineType.LEGACY;
	}

	@Override
	public String getVariableResolverFactoryName() {
		return this.getClass().getSimpleName();
	}

}
