package webhook.teamcity.payload.variableresolver;

import java.util.List;
import java.util.Map;

import webhook.teamcity.payload.PayloadTemplateEngineType;

public interface WebHookVariableResolverManager {

	void registerVariableResolverFactory(VariableResolverFactory factory);

	VariableResolverFactory getVariableResolverFactory(PayloadTemplateEngineType type);
	
	Map<String, VariableResolverFactory> getAllVariableResolverFactoriesMap();
	List<VariableResolverFactory> getAllVariableResolverFactories();

}