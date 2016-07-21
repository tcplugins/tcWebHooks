package webhook.teamcity;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

public class TestingWebHookHttpClientFactoryImpl implements WebHookHttpClientFactory {
	
	
	TestableHttpClient httpClient;
	
	public TestingWebHookHttpClientFactoryImpl(TestableHttpClient client) {
		this.httpClient = client;
	}
	
	@Override
	public HttpClient getHttpClient(){
		return httpClient;
	}
	
	public interface InvocationCountable {
		public abstract int getIncovationCount();
	}


	public static class TestableHttpClient extends HttpClient implements InvocationCountable {
		
		public int invocationCount = 0;
		
		@Override
		public int executeMethod(HttpMethod method) throws IOException,	HttpException {
			invocationCount++;
			return super.executeMethod(method);
		}

		@Override
		public int getIncovationCount() {
			return invocationCount;
		}

	}
}
