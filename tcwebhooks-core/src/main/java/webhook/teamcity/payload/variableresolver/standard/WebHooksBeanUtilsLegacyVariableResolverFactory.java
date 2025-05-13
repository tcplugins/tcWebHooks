package webhook.teamcity.payload.variableresolver.standard;

import com.intellij.openapi.diagnostic.Logger;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;

public class WebHooksBeanUtilsLegacyVariableResolverFactory extends WebHooksBeanUtilsVariableResolverFactory implements VariableResolverFactory {
	private static final Logger LOG = Logger.getInstance(WebHooksBeanUtilsLegacyVariableResolverFactory.class.getName());

	@Override
	public void register() {
		LOG.info("WebHooksBeanUtilsLegacyVariableResolverFactory :: Registering for type: " + getPayloadTemplateType().toString());
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
