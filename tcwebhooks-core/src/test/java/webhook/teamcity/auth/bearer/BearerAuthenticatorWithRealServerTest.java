package webhook.teamcity.auth.bearer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.auth.AuthWithRealServerTestBase;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class BearerAuthenticatorWithRealServerTest extends AuthWithRealServerTestBase {

	@Test
	public void testRealWebHookWithAuth() throws JDOMException, IOException, InterruptedException {
		server.enqueue(new MockResponse().setResponseCode(401).setHeader("www-authenticate", "Bearer realm=\"http://example1.com\""));
		server.enqueue(new MockResponse().setResponseCode(201));
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-bearer-auth.xml"));
		framework.getWebHookListener().buildFinished(framework.getRunningBuild());
		RecordedRequest req1 = server.takeRequest();
		RecordedRequest req2 = server.takeRequest();
		assertNull(req1.getHeader("Authorization"));
		assertEquals("Bearer my_token", req2.getHeader("Authorization"));
	}
	
	@Test
	public void testRealWebHookWithPremptiveAuth() throws JDOMException, IOException, InterruptedException {
		server.enqueue(new MockResponse().setResponseCode(201));
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-preemptive-bearer-auth.xml"));
		framework.getWebHookListener().buildFinished(framework.getRunningBuild());
		
		RecordedRequest req1 = server.takeRequest();
		assertEquals("Bearer my_preemptive_token", req1.getHeader("Authorization"));
	}
	
	@Test
	public void testRealWebHookWithPremptiveAuthFromToken() throws JDOMException, IOException, InterruptedException {
		server.enqueue(new MockResponse().setResponseCode(201));
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-preemptive-bearer-auth-from-token.xml"));
		framework.getWebHookListener().buildFinished(framework.getRunningBuild());
		
		RecordedRequest req1 = server.takeRequest();
		assertEquals("Bearer thing1", req1.getHeader("Authorization"));
	}

}
