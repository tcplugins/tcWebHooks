package webhook.teamcity.auth;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.BasicAuthCache;
import org.jetbrains.annotations.NotNull;

import webhook.teamcity.auth.bearer.BearerAuthSchemeFactory;
import webhook.teamcity.auth.bearer.TokenCredentials;

public class BearerAuthenticator implements WebHookAuthenticator {

		public static final String BEARER_AUTH_SCHEME_TYPE = "Bearer";
		public static final String KEY_BEARER = "bearer";
		public static final String KEY_REALM = "realm";
		WebHookAuthenticatorProvider myProvider;
		WebHookAuthConfig config;
		
		@Override
		public void addAuthentication(CredentialsProvider credentialsProvider, HttpClientContext httpClientContext, String url) throws AuthenticationException {
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
			return BEARER_AUTH_SCHEME_TYPE;
		}

}

