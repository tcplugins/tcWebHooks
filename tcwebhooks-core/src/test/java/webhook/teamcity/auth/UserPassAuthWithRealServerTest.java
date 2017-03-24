package webhook.teamcity.auth;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.JDOMException;
import org.junit.Test;

import webhook.WebHookTestServer;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class UserPassAuthWithRealServerTest {

	SortedMap<String, String> map = new TreeMap<String, String>();
	ExtraParametersMap  extraParameters  = new ExtraParametersMap(map); 
	ExtraParametersMap  teamcityProperties  = new ExtraParametersMap(map); 
	WebHookMockingFramework framework;

	@Test
	public void testRealWebHookWithAuth() throws JDOMException, IOException, InterruptedException {
			WebHookTestServer server = startWebServer();
			framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);
			framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));
			framework.getWebHookListener().buildFinished(framework.getRunningBuild());
			stopWebServer(server);
	}
	
	@Test
	public void testRealWebHookWithWrongAuth() throws JDOMException, IOException, InterruptedException {
		WebHookTestServer server = startWebServer();
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-wrong-auth.xml"));
		framework.getWebHookListener().buildFinished(framework.getRunningBuild());
		stopWebServer(server);
	}
	
	public WebHookTestServer startWebServer(){
		try {
			WebHookTestServer s = new WebHookTestServer("localhost", 58001);
			s.getServer().start();
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void stopWebServer(WebHookTestServer s) throws InterruptedException {
		try {
			s.getServer().stop();
			// Sleep to let the server shutdown cleanly.
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Thread.sleep(1000);
		}
	}

	
	
}
