package webhook.teamcity.auth;

import java.util.ArrayList;
import java.util.List;

public class BearerAuthenticatorFactory implements WebHookAuthenticatorFactory {
		private static final boolean REQUIRED = true;
		private static final boolean NOT_HIDDEN = false;

		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
		
		List<WebHookAuthenticationParameter> parameterDefinition;
		
		public BearerAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			myProvider = provider;
			parameterDefinition = new ArrayList<>();
			parameterDefinition.add(new WebHookAuthenticationParameter(BearerAuthenticator.KEY_BEARER, REQUIRED, NOT_HIDDEN, "Bearer Token", "The Bearer token that the webhook should send to authenticate with the webserver."));
		}
		
		@Override
		public void register(){
			myProvider.registerAuthType(this);
		}
		
		@Override
		public String getName() {
			return "bearer";
		}
		
		@Override
		public String getDescription() {
			return "Bearer Token Authentication (Bearer)";
		}

		@Override
		public WebHookAuthenticator getAuthenticatorInstance() {
			return new BearerAuthenticator();
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

