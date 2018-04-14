package webhook.teamcity.auth;

import java.net.URI;

import javax.annotation.Resource.AuthenticationType;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.jetbrains.annotations.NotNull;

public class UsernamePasswordAuthenticator implements WebHookAuthenticator {

		public static final String KEY_REALM = "realm";
		public static final String KEY_PASS = "password";
		public static final String KEY_USERNAME = "username";
		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
		
		@Override
		public void addAuthentication(CredentialsProvider credentialsProvider, HttpClientContext httpClientContext, String url) {
			if (config.getParameters().containsKey(KEY_USERNAME) && config.getParameters().containsKey(KEY_PASS)){
					URI uri = URI.create(url);
					AuthScope scope;
					if (config.getParameters().containsKey(KEY_REALM)){
						scope = new AuthScope(uri.getHost(), uri.getPort(), config.getParameters().get(KEY_REALM));
					} else {
						scope = new AuthScope(uri.getHost(), uri.getPort());
					}
					Credentials creds = new UsernamePasswordCredentials(config.getParameters().get(KEY_USERNAME), config.getParameters().get(KEY_PASS));
					credentialsProvider.setCredentials(scope, creds);
					if (config.getPreemptive()) {
				           // Create AuthCache instance
			            AuthCache authCache = new BasicAuthCache();
			            // Generate BASIC scheme object and add it to the local
			            // auth cache
			            BasicScheme basicAuth = new BasicScheme();
			            authCache.put(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), basicAuth);

			            // Add AuthCache to the execution context
			            httpClientContext.setAuthCache(authCache);
					}
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

		@Override
		public String getWwwAuthenticateChallengePrefix() {
			return "basic";
		}

}

