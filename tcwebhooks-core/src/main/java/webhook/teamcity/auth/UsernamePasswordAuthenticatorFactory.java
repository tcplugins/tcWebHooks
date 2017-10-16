package webhook.teamcity.auth;

import java.util.ArrayList;
import java.util.List;

public class UsernamePasswordAuthenticatorFactory implements WebHookAuthenticatorFactory{
		private static final boolean REQUIRED = true;
		private static final boolean NOT_REQUIRED = false;
		private static final boolean HIDDEN = true;
		private static final boolean NOT_HIDDEN = false;

		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
		
		List<WebHookAuthenticationParameter> parameterDefinition;
		
		public UsernamePasswordAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			myProvider = provider;
			parameterDefinition = new ArrayList<>();
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.KEY_USERNAME, REQUIRED, NOT_HIDDEN, "Username", "The username that the webhook should send to authenticate with the webserver."));
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.KEY_PASS, REQUIRED, HIDDEN, "Password", "The password that the webhook should send to authenticate with the webserver."));
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.KEY_REALM, NOT_REQUIRED, NOT_HIDDEN, "Realm", "The Realm the server must present before this webhook will send credentials. This is ignored if preemptive is enabled, because the webhook does not make a first request to expect a 401 repsonse and to retreive the realm."));
		
		}
		
		@Override
		public void register(){
			myProvider.registerAuthType(this);
		}
		
		@Override
		public String getName() {
			return "userpass";
		}
		
		@Override
		public String getDescription() {
			return "Username/Password Authentication (Basic Auth)";
		}

		@Override
		public WebHookAuthenticator getAuthenticatorInstance() {
			return new UsernamePasswordAuthenticator();
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

