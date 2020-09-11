package webhook.teamcity.auth.ntlm;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.client.BasicAuthCache;

import webhook.teamcity.auth.AbstractWebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticator;

public class NtlmAuthenticator extends AbstractWebHookAuthenticator implements WebHookAuthenticator {

		public static final String KEY_PASS = "password";
		public static final String KEY_USERNAME = "username";
		public static final String KEY_WORKSTATION = "workstation";
		public static final String KEY_DOMAIN = "domain";
		
		@Override
		public void addAuthentication(CredentialsProvider credentialsProvider, HttpClientContext httpClientContext, String url) {
			if (config.getParameters().containsKey(KEY_USERNAME) && config.getParameters().containsKey(KEY_PASS)){
					URI uri = URI.create(url);
					AuthScope scope = new AuthScope(uri.getHost(), uri.getPort());
					Credentials creds = new NTCredentials(config.getParameters().get(KEY_USERNAME), config.getParameters().get(KEY_PASS), config.getParameters().get(KEY_WORKSTATION), config.getParameters().get(KEY_DOMAIN));
					credentialsProvider.setCredentials(scope, creds);
					if (Boolean.TRUE.equals(config.getPreemptive())) {
				           // Create AuthCache instance
			            AuthCache authCache = new BasicAuthCache();
			            // Generate BASIC scheme object and add it to the local
			            // auth cache
			            NTLMScheme ntlmAuth = new NTLMScheme();
			            authCache.put(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), ntlmAuth);

			            // Add AuthCache to the execution context
			            httpClientContext.setAuthCache(authCache);
					}
			}
		}

		@Override
		public String getWwwAuthenticateChallengePrefix() {
			return "ntlm";
		}

}

