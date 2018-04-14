package webhook.teamcity.auth;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;

public interface WebHookAuthenticator {
	public WebHookAuthConfig getWebHookAuthConfig();
	public String getWwwAuthenticateChallengePrefix();
	public void addAuthentication (CredentialsProvider credentialsProvider, HttpClientContext httpClientContext, String url) throws AuthenticationException;
	public void setWebHookAuthConfig(WebHookAuthConfig authenticationConfig);
}
