package webhook.teamcity.auth;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;

public interface WebHookAuthenticator {
	public WebHookAuthConfig getWebHookAuthConfig();
	public void addAuthentication (CredentialsProvider credentialsProvider, HttpClientContext httpClientContext, String url);
	public void setWebHookAuthConfig(WebHookAuthConfig authenticationConfig);
}
