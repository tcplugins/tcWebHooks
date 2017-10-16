package webhook.teamcity.auth;

import java.net.URI;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;

public class UsernamePasswordAuthenticator implements WebHookAuthenticator {

		public static final String KEY_REALM = "realm";
		public static final String KEY_PASS = "password";
		public static final String KEY_USERNAME = "username";
		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
		
		@Override
		public void addAuthentication(PostMethod method, HttpClient client, String url) {
			if (config.getParameters().containsKey(KEY_USERNAME) && config.getParameters().containsKey(KEY_PASS)){
					URI uri = URI.create(url);
					AuthScope scope;
					if (config.getParameters().containsKey(KEY_REALM)){
						scope = new AuthScope(uri.getHost(), uri.getPort(), config.getParameters().get(KEY_REALM));
					} else {
						scope = new AuthScope(uri.getHost(), uri.getPort());
					}
					Credentials creds = new UsernamePasswordCredentials(config.getParameters().get(KEY_USERNAME), config.getParameters().get(KEY_PASS));
					client.getState().setCredentials(scope, creds);
					client.getParams().setAuthenticationPreemptive(config.getPreemptive());
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

