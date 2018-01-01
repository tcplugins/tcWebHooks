package webhook.teamcity;

import org.apache.http.client.HttpClient;

public interface WebHookHttpClientFactory {
	public abstract HttpClient getHttpClient();
}