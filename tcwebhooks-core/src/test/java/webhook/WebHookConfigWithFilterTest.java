package webhook;

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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.TestingWebHookHttpClientFactoryImpl;
import webhook.teamcity.TestingWebHookHttpClientFactoryImpl.TestableHttpClient;
import webhook.teamcity.WebHookHttpClientFactory;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class WebHookConfigWithFilterTest extends WebHookTestServerTestBase {
	
	

	private static final int RESULT__NO_REQUEST_MADE = -1;

	@Mock
	WebHookMainSettings mainSettings;
	
	WebHookHttpClientFactory webHookHttpClientFactory;
	TestableHttpClient httpClient;
	
	WebHookTestServer s;
	
	protected SortedMap<String, String> map = new TreeMap<>();
	protected ExtraParameters  extraParameters  = new ExtraParameters(map); 
	protected WebHookMockingFramework framework;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		s = startWebServer();
		httpClient = new TestableHttpClient();
		webHookHttpClientFactory = new TestingWebHookHttpClientFactoryImpl(httpClient);
	}
	
	@After
	public void tearDown() throws InterruptedException {
		stopWebServer(s);
	}
	
	@Test
	public void testFilterWithUnmatchableMatcherOnRealServer() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-incorrectFilterValue.xml"));
		framework.getWebHookListener().buildFinished(framework.getRunningBuild());
		assertEquals("Post should not have been executed", RESULT__NO_REQUEST_MADE, s.getReponseCode());
		
	}
	
	@Test
	public void testFilterWithBadMatcherOnRealServerFinished() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameFilter.xml"));
		framework.getWebHookListener().buildFinished(framework.getRunningBuild());
		assertEquals("Post should not have been executed", RESULT__NO_REQUEST_MADE, s.getReponseCode());
		
	}
	
	@Test
	public void testDoubleFilterMatcherOnRealServerFinished() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
		framework.getWebHookListener().buildFinished(framework.getRunningBuild());
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		
	}
	
	@Test
	public void testFilterWithBadMatcherOnRealServerStarted() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_STARTED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameFilter.xml"));
		framework.getWebHookListener().buildStarted(framework.getRunningBuild());
		assertEquals("Post should not have been executed", RESULT__NO_REQUEST_MADE, s.getReponseCode());
		
	}
	
	@Test
	public void testDoubleFilterMatcherOnRealServerStarted() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_STARTED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
		framework.getWebHookListener().buildStarted(framework.getRunningBuild());
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		
	}
	
	@Test
	public void testFilterWithBadMatcherOnRealServerChanges() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.CHANGES_LOADED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameFilter.xml"));
		framework.getWebHookListener().changesLoaded(framework.getRunningBuild());
		assertEquals("Post should not have been executed", RESULT__NO_REQUEST_MADE, s.getReponseCode());
		
	}
	
	@Test
	public void testDoubleFilterMatcherOnRealServerChanges() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.CHANGES_LOADED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
		framework.getWebHookListener().changesLoaded(framework.getRunningBuild());
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		
	}
	
	@Test
	public void testFilterWithBadMatcherOnRealServerInterupted() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_INTERRUPTED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameFilter.xml"));
		framework.getWebHookListener().buildInterrupted(framework.getRunningBuild());
		assertEquals("Post should not have been executed", RESULT__NO_REQUEST_MADE, s.getReponseCode());
		
	}
	
	@Test
	public void testDoubleFilterMatcherOnRealServerInterupted() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_INTERRUPTED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
		framework.getWebHookListener().buildInterrupted(framework.getRunningBuild());
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		
	}
	
	@Test
	public void testFilterWithBadMatcherOnRealServerBeforeFinished() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameFilter.xml"));
		framework.getWebHookListener().beforeBuildFinish(framework.getRunningBuild());
		assertEquals("Post should not have been executed", RESULT__NO_REQUEST_MADE, s.getReponseCode());
		
	}
	
	@Test
	public void testDoubleFilterMatcherOnRealServerBeforeFinished() throws IOException, JDOMException, InterruptedException {
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
		framework.getWebHookListener().buildInterrupted(framework.getRunningBuild());
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		
	}

	@Override
	public String getHost() {
		return "localhost";
	}

	@Override
	public Integer getPort() {
		return 58001;
	}

}
