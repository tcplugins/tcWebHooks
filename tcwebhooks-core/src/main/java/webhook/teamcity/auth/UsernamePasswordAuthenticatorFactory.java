package webhook.teamcity.auth;

import java.util.ArrayList;
import java.util.List;

public class UsernamePasswordAuthenticatorFactory implements WebHookAuthenticatorFactory{

		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
		
		List<WebHookAuthenticationParameter> parameterDefinition;
		
		public UsernamePasswordAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			myProvider = provider;
			parameterDefinition = new ArrayList<>();
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.USERNAME, true, false, "Username", "The username that the webhook should send to authenticate with the webserver."));
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.PASSWORD, true, true, "Password", "The password that the webhook should send to authenticate with the webserver."));
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.REALM, false, false, "Realm", "The Realm the server must present before this webhook will send credentials. This is ignored if preemptive is enabled, because the webhook does not make a first request to expect a 401 repsonse and to retreive the realm."));
		
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
}

