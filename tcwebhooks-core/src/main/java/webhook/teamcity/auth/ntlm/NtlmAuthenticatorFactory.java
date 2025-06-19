package webhook.teamcity.auth.ntlm;

import webhook.teamcity.auth.AbstractWebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticationParameter;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;

public class NtlmAuthenticatorFactory extends AbstractWebHookAuthenticatorFactory implements WebHookAuthenticatorFactory{

		public NtlmAuthenticatorFactory(WebHookAuthenticatorProvider provider) {
			super(provider);
			parameterDefinition.add(new WebHookAuthenticationParameter(NtlmAuthenticator.KEY_USERNAME, REQUIRED, NOT_HIDDEN, "Username", "ntlmUsername", "The user name.  This should not include the domain to authenticate with. For example: 'user' is correct whereas 'DOMAIN&#x5c;user' is not."));
			parameterDefinition.add(new WebHookAuthenticationParameter(NtlmAuthenticator.KEY_PASS, REQUIRED, HIDDEN, "Password", "ntlmPassword", "The password that the webhook should send to authenticate with the webserver."));
			parameterDefinition.add(new WebHookAuthenticationParameter(NtlmAuthenticator.KEY_WORKSTATION, REQUIRED, NOT_HIDDEN, "Workstation", "ntlmWorkStation", "The workstation the authentication request is originating from. Essentially, the computer name for the TeamCity server."));
			parameterDefinition.add(new WebHookAuthenticationParameter(NtlmAuthenticator.KEY_DOMAIN, REQUIRED, NOT_HIDDEN, "Domain", "ntlmDomain", "The Windows domain to authenticate within."));
		}
		
		@Override
		public String getName() {
			return "ntlm";
		}
		@Override
		public String getKotlinDslName() {
		    return getName();
		}
		
		@Override
		public String getDescription() {
			return "NTLM Authentication";
		}

		@Override
		public WebHookAuthenticator getAuthenticatorInstance() {
			return new NtlmAuthenticator();
		}

        @Override
        public String getProjectFeaturePrefix() {
            return getName();
        }

}

