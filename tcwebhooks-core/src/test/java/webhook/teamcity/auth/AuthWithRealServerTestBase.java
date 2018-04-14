package webhook.teamcity.auth;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import okhttp3.mockwebserver.MockWebServer;
import webhook.WebHookTestServer;
import webhook.WebHookTestServerTestBase;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class AuthWithRealServerTestBase extends WebHookTestServerTestBase {

	protected SortedMap<String, String> map = new TreeMap<>();
	protected ExtraParametersMap  extraParameters  = new ExtraParametersMap(map); 
	protected ExtraParametersMap  teamcityProperties  = new ExtraParametersMap(map); 
	protected WebHookMockingFramework framework;
	
	MockWebServer server;
	private String urlPrefix;
	
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

		urlPrefix = String.format("http://%s:%s", server.getHostName(), server.getPort());
	}

	@After
	public void teardown() throws IOException {
		server.shutdown();
	}
	
}
