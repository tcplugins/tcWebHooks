package webhook.teamcity.payload.variableresolver;

import java.util.Map;
import java.util.TreeMap;

import jetbrains.buildServer.log.Loggers;
import webhook.teamcity.WebHookContentResolutionException;
import webhook.teamcity.payload.PayloadTemplateEngineType;

public class WebHookVariableResolverManagerImpl implements WebHookVariableResolverManager {
	
	Map<PayloadTemplateEngineType, VariableResolverFactory> variableResolvers = new TreeMap<>();
	
	@Override
	public void registerVariableResolverFactory(VariableResolverFactory factory) {
		if (variableResolvers.containsKey(factory.getPayloadTemplateType())) {
			Loggers.SERVER.warn("WebHookVariableResolverManagerImpl :: Previously registered factory '" 
				+ variableResolvers.get(factory.getPayloadTemplateType()).getVariableResolverFactoryName()
				 + "' of type '"
				+ factory.getPayloadTemplateType().toString() + "' was overridden.");
		}
		variableResolvers.put(factory.getPayloadTemplateType(), factory);
		Loggers.SERVER.info("WebHookVariableResolverManagerImpl :: Registered new VariableResolverFactory '" 
				+ factory.getVariableResolverFactoryName()
				+ "' as type '" 
				+ factory.getPayloadTemplateType().toString() + "'");
	}
	
	@Override
	public VariableResolverFactory getVariableResolverFactory(PayloadTemplateEngineType type) {
		if (! variableResolvers.containsKey(type)) {
			throw new WebHookContentResolutionException("No VariableResolverFactory is registered for type '" + type.toString() + "'", WebHookContentResolutionException.WEBHOOK_VARIABLE_RESOLVER_NOT_FOUND_EXCEPTION_ERROR_CODE);
		}
		return variableResolvers.get(type);
	}

}
