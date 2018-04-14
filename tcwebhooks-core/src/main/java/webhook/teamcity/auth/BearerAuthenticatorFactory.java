package webhook.teamcity.auth;

import java.util.List;

public class BearerAuthenticatorFactory extends AbstractWebHookAuthenticatorFactory implements WebHookAuthenticatorFactory {
		public BearerAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			super(provider);
			parameterDefinition.add(
					new WebHookAuthenticationParameter(BearerAuthenticator.KEY_BEARER, REQUIRED, NOT_HIDDEN, "Bearer Token", "The Bearer token that the webhook should send to authenticate with the webserver.")
			);
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
}

