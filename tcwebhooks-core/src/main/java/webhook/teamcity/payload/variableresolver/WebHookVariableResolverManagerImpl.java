package webhook.teamcity.payload.variableresolver;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.intellij.openapi.diagnostic.Logger;
import webhook.teamcity.WebHookContentResolutionException;
import webhook.teamcity.WebHookExecutionException;
import webhook.teamcity.payload.PayloadTemplateEngineType;

public class WebHookVariableResolverManagerImpl implements WebHookVariableResolverManager {
	private static final Logger LOG = Logger.getInstance(WebHookVariableResolverManagerImpl.class.getName());

	Map<PayloadTemplateEngineType, VariableResolverFactory> variableResolvers = new TreeMap<>();
	Map<String, VariableResolverFactory> variableResolversWithStringKeys = new TreeMap<>();
	
	@Override
	public void registerVariableResolverFactory(VariableResolverFactory factory) {
		if (variableResolvers.containsKey(factory.getPayloadTemplateType())) {
			LOG.warn("WebHookVariableResolverManagerImpl :: Previously registered factory '" 
				+ variableResolvers.get(factory.getPayloadTemplateType()).getVariableResolverFactoryName()
				 + "' of type '"
				+ factory.getPayloadTemplateType().toString() + "' was overridden.");
		}
		variableResolvers.put(factory.getPayloadTemplateType(), factory);
		LOG.info("WebHookVariableResolverManagerImpl :: Registered new VariableResolverFactory '" 
				+ factory.getVariableResolverFactoryName()
				+ "' as type '" 
				+ factory.getPayloadTemplateType().toString() + "'");
	}
	
	@Override
	public VariableResolverFactory getVariableResolverFactory(PayloadTemplateEngineType type) {
		if (! variableResolvers.containsKey(type)) {
			throw new WebHookContentResolutionException("No VariableResolverFactory is registered for type '" + type.toString() + "'", WebHookExecutionException.WEBHOOK_VARIABLE_RESOLVER_NOT_FOUND_EXCEPTION_ERROR_CODE);
		}
		return variableResolvers.get(type);
	}
	
	@Override
	public Map<String, VariableResolverFactory> getAllVariableResolverFactoriesMap() {
		return ImmutableMap.copyOf(this.variableResolversWithStringKeys);
	}
	
	@Override
	public List<VariableResolverFactory> getAllVariableResolverFactories() {
		return ImmutableList.copyOf(variableResolvers.values());
	}

}
