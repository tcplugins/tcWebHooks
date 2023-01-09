package webhook.teamcity.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractWebHookAuthenticatorFactory implements WebHookAuthenticatorFactory {

	protected static final boolean REQUIRED = true;
	protected static final boolean NOT_REQUIRED = false;
	protected static final boolean HIDDEN = true;
	protected static final boolean NOT_HIDDEN = false;

	protected WebHookAuthenticatorProvider myProvider;
	protected List<WebHookAuthenticationParameter> parameterDefinition;

	protected AbstractWebHookAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
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
			if (parameter.isRequired() &&  ! isPresent(webHookAuthConfig.getParameters(), parameter.getKey())){
				return false;
			}
		}
		return true;
	}

	private boolean isPresent(Map<String, String> parameters, String key) {
		return parameters.containsKey(key) &&
			   parameters.get(key) != null &&
			   parameters.get(key).length() > 0;
	}

}
