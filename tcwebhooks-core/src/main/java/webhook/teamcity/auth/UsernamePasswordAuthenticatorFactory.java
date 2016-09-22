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
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.USERNAME, true, false, "Username", "The username to authenticate as"));
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.PASSWORD, true, true, "Password", "The password to authenticate with"));
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.REALM, false, false, "Realm", "The Realm the server must present. This is ignored if preemptive is enabled (the default)"));
		
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

