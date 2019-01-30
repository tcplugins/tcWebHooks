package webhook.teamcity;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

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
		public abstract int getInvocationCount();
	}


	public static class TestableHttpClient extends DefaultHttpClient implements InvocationCountable {
		
		public int invocationCount = 0;
		
		@Override
		public CloseableHttpResponse execute(
	            final HttpUriRequest request,
	            final HttpContext context) throws IOException, ClientProtocolException {
			invocationCount++;
			return super.execute(request, context);
		}

		@Override
		public int getInvocationCount() {
			return invocationCount;
		}

	}
}
