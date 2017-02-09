package webhook.teamcity;

import org.apache.commons.httpclient.HttpClient;

public interface WebHookHttpClientFactory {
	public abstract HttpClient getHttpClient();
}