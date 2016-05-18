package webhook.teamcity.auth;

import java.net.URI;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;

public class NTLMAuthenticator implements WebHookAuthenticator {

		private static final String DOMAIN = "domain";
		private static final String PASSWORD = "password";
		private static final String USERNAME = "username";
		private static final String ORIGINATOR = "originator";
		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
	

		@Override
		public void addAuthentication(PostMethod method, HttpClient client, String url) {
			if (	config.parameters.containsKey(USERNAME) && 
					config.parameters.containsKey(PASSWORD) &&
					config.parameters.containsKey(ORIGINATOR) &&
					config.parameters.containsKey(DOMAIN)
				){
					URI uri = URI.create(url);
					AuthScope scope;
					scope = new AuthScope(uri.getHost(), uri.getPort(), config.parameters.get(DOMAIN));
					
					Credentials creds = new NTCredentials(
										config.parameters.get(USERNAME), 
										config.parameters.get(PASSWORD),
										config.parameters.get(ORIGINATOR), 
										config.parameters.get(DOMAIN)
							);
					client.getState().setCredentials(scope, creds);
					client.getParams().setAuthenticationPreemptive(config.preemptive);
			}
		}

		@Override @NotNull
		public WebHookAuthConfig getWebHookAuthConfig() {
			return config;
		}

		@Override
		public void setWebHookAuthConfig(WebHookAuthConfig authenticationConfig) {
			this.config = authenticationConfig;
			
		}
}

