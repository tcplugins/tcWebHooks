package webhook.teamcity.auth;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWebHookAuthenticatorFactory implements WebHookAuthenticatorFactory {

	protected static final boolean REQUIRED = true;
	protected static final boolean NOT_REQUIRED = false;
	protected static final boolean HIDDEN = true;
	protected static final boolean NOT_HIDDEN = false;	
	
	protected WebHookAuthenticatorProvider myProvider;
	protected List<WebHookAuthenticationParameter> parameterDefinition;
	
	public AbstractWebHookAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
		myProvider = provider;
		parameterDefinition = new ArrayList<>();
	}

	@Override
	public void register() {
		myProvider.registerAuthType(this);
	}

	@Override
	public List<WebHookAuthenticationParameter> getParameterList() {
		return parameterDefinition;
	}

	@Override
	public boolean areAllRequiredParametersPresent(WebHookAuthConfig webHookAuthConfig) {
		for (WebHookAuthenticationParameter parameter : this.parameterDefinition) {
			if (parameter.isRequired()){
				if (
					webHookAuthConfig.getParameters().containsKey(parameter.getKey()) &&
					webHookAuthConfig.getParameters().get(parameter.getKey()) != null &&
					webHookAuthConfig.getParameters().get(parameter.getKey()).length() > 0) {
					
						// We have a value greater than 0 chars. 
						continue;
				} else {
					return false;
				}
			}
		}
		return true;
	}

}
