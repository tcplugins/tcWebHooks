package webhook.teamcity;

import org.apache.commons.httpclient.HttpClient;

public class WebHookHttpClientFactoryImpl implements WebHookHttpClientFactory {
	
	@Override
	public HttpClient getHttpClient(){
		return new HttpClient();
	}

}
