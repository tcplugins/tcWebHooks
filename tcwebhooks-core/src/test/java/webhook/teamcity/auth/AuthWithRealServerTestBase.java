package webhook.teamcity.auth;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;

import okhttp3.mockwebserver.MockWebServer;
import webhook.WebHookTestServerTestBase;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.testframework.WebHookMockingFramework;

public class AuthWithRealServerTestBase extends WebHookTestServerTestBase {

	protected SortedMap<String, String> map = new TreeMap<>();
	protected ExtraParameters  extraParameters  = new ExtraParameters(map); 
	protected ExtraParameters  teamcityProperties  = new ExtraParameters(map); 
	protected WebHookMockingFramework framework;
	
	protected MockWebServer server;
	
	@Override
	public String getHost() {
		return "localhost";
	}

	@Override
	public Integer getPort() {
		return 58001;
	}
	
	@Before
	public void setup() throws IOException {
		server = new MockWebServer();
		server.start(getPort());
		
		System.out.println(String.format("MockServer started on http://%s:%s ",
				server.getHostName(),
				server.getPort()
		));

	}

	@After
	public void teardown() throws IOException {
		server.shutdown();
	}
	
}
