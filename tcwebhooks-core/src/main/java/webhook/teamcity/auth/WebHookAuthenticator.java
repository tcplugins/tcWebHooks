package webhook.teamcity.auth;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

public interface WebHookAuthenticator {
	public WebHookAuthConfig getWebHookAuthConfig();
	public void addAuthentication (PostMethod httppost, HttpClient client, String url);
	public void setWebHookAuthConfig(WebHookAuthConfig authenticationConfig);
}
