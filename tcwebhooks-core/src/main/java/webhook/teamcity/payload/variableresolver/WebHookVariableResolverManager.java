package webhook.teamcity.payload.variableresolver;

import webhook.teamcity.payload.PayloadTemplateEngineType;

public interface WebHookVariableResolverManager {

	void registerVariableResolverFactory(VariableResolverFactory factory);

	VariableResolverFactory getVariableResolverFactory(PayloadTemplateEngineType type);

}