package webhook.teamcity.auth.basic;

import webhook.teamcity.auth.AbstractWebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticationParameter;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;

public class UsernamePasswordAuthenticatorFactory extends AbstractWebHookAuthenticatorFactory implements WebHookAuthenticatorFactory{

		public UsernamePasswordAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			super(provider);
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.KEY_USERNAME, REQUIRED, NOT_HIDDEN, "Username", "The username that the webhook should send to authenticate with the webserver."));
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.KEY_PASS, REQUIRED, HIDDEN, "Password", "The password that the webhook should send to authenticate with the webserver."));
			parameterDefinition.add(new WebHookAuthenticationParameter(UsernamePasswordAuthenticator.KEY_REALM, NOT_REQUIRED, NOT_HIDDEN, "Realm", "The Realm the server must present before this webhook will send credentials. This is ignored if preemptive is enabled, because the webhook does not make a first request to expect a 401 repsonse and to retreive the realm."));
		
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

}

