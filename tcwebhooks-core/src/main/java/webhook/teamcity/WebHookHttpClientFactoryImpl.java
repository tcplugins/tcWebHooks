package webhook.teamcity;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class WebHookHttpClientFactoryImpl implements WebHookHttpClientFactory {
	
	@Override
	public HttpClient getHttpClient(){
		return HttpClients.createDefault();
	}

}
