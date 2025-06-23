package webhook.teamcity.auth.bearer;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.BasicAuthCache;

import webhook.teamcity.auth.AbstractWebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticator;

public class BearerAuthenticator extends AbstractWebHookAuthenticator implements WebHookAuthenticator {

		public static final String BEARER_AUTH_SCHEME_TYPE = "Bearer";
		public static final String KEY_BEARER = "bearer";
		
		@Override
		public void addAuthentication(CredentialsProvider credentialsProvider, HttpClientContext httpClientContext, String url) {
			if (config.getParameters().containsKey(KEY_BEARER)){
					URI uri = URI.create(url);
					RegistryBuilder<AuthSchemeProvider> registryBuilder = RegistryBuilder.create();
					BearerAuthSchemeFactory bearerAuthSchemeFactory = new BearerAuthSchemeFactory();
					Registry<AuthSchemeProvider> authSchemeRegistry = registryBuilder.register(BEARER_AUTH_SCHEME_TYPE, bearerAuthSchemeFactory).build();
				    httpClientContext.setAttribute(HttpClientContext.AUTHSCHEME_REGISTRY, authSchemeRegistry);
					
					AuthScope scope = new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_REALM, BEARER_AUTH_SCHEME_TYPE);
					Credentials creds = new TokenCredentials(config.getParameters().get(KEY_BEARER));
					credentialsProvider.setCredentials(scope, creds);
					httpClientContext.setAttribute(HttpClientContext.CREDS_PROVIDER, credentialsProvider);
					
					if (config.getPreemptive()) {
						// Create AuthCache instance
						AuthCache authCache = new BasicAuthCache();
						// Generate Bearer scheme object and add it to the local auth cache
						AuthScheme bearerAuth = bearerAuthSchemeFactory.create(httpClientContext);
						authCache.put(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), bearerAuth);
						
						// Add AuthCache to the execution context
						httpClientContext.setAuthCache(authCache);
					}
			}
		}

		@Override
		public String getWwwAuthenticateChallengePrefix() {
			return BEARER_AUTH_SCHEME_TYPE;
		}

}

