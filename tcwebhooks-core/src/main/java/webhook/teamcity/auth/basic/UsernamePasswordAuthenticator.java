package webhook.teamcity.auth.basic;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;

import webhook.teamcity.auth.AbstractWebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticator;

public class UsernamePasswordAuthenticator extends AbstractWebHookAuthenticator implements WebHookAuthenticator {

		public static final String KEY_REALM = "realm";
		public static final String KEY_PASS = "password";
		public static final String KEY_USERNAME = "username";
		
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

		@Override
		public String getWwwAuthenticateChallengePrefix() {
			return "basic";
		}

}

