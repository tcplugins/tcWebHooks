package webhook.teamcity.auth.bearer;

import java.util.List;

import webhook.teamcity.auth.AbstractWebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticationParameter;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;

public class BearerAuthenticatorFactory extends AbstractWebHookAuthenticatorFactory implements WebHookAuthenticatorFactory {
		public BearerAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			super(provider);
			parameterDefinition.add(
					new WebHookAuthenticationParameter(BearerAuthenticator.KEY_BEARER, REQUIRED, NOT_HIDDEN, "Bearer Token","bearerToken", "The Bearer token that the webhook should send to authenticate with the webserver.")
			);
		}
		
		@Override
		public String getName() {
			return "bearer";
		}
		
		@Override
		public String getKotlinDslName() {
		    return getName();
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
        public String getProjectFeaturePrefix() {
            return getName();
        }
}

