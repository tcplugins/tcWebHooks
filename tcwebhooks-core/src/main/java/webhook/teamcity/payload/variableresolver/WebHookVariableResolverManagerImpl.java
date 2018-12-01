package webhook.teamcity.payload.variableresolver;

import java.util.Map;
import java.util.TreeMap;

import webhook.teamcity.payload.PayloadTemplateEngineType;

public class WebHookVariableResolverManagerImpl implements WebHookVariableResolverManager {
	
	Map<PayloadTemplateEngineType, VariableResolverFactory> variableResolvers = new TreeMap<>();
	
	@Override
	public void registerVariableResolverFactory(VariableResolverFactory factory) {
		variableResolvers.put(factory.getPayloadTemplateType(), factory);
		
	}
	
	@Override
	public VariableResolverFactory getVariableResolverFactory(PayloadTemplateEngineType type) {
		return variableResolvers.get(type);
	}

}
