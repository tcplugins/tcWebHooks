package webhook.teamcity.payload.content;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class WebHookPayloadContentTest {
	
	SortedMap<String, String> map = new TreeMap<>();
	ExtraParameters  extraParameters  = new ExtraParameters(map); 
	WebHookMockingFramework framework;

	@Before 
	public void setup() throws JDOMException, IOException{
	}
	
	@Test
	public void testGetBuildStatusHtml() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled.xml"));
		
		final String htmlStatus = "<span class=\"tcWebHooksMessage\"><a href=\"http://test.server/project.html?projectId=ATestProject\">Test Project</a> :: <a href=\"http://test.server/viewType.html?buildTypeId=ATestProject_TestBuild\">Test Build</a> # <a href=\"http://test.server/viewLog.html?buildTypeId=ATestProject_TestBuild&buildId=123456\"><strong>TestBuild01</strong></a> has <strong>finished</strong> with a status of <a href=\"http://test.server/viewLog.html?buildTypeId=ATestProject_TestBuild&buildId=123456\"> <strong>success</strong></a> and was triggered by <strong>SubVersion</strong></span>";
		//						   <span class="tcWebHooksMessage"><a href="http://test.server/project.html?projectId=project1">Test Project</a> :: <a href="http://test.server/viewType.html?buildTypeId=bt1">Test Build</a> # <a href="http://test.server/viewLog.html?buildTypeId=bt1&buildId=123456"><strong>TestBuild01</strong></a> has <strong>finished</strong> with a status of <a href="http://test.server/viewLog.html?buildTypeId=bt1&buildId=123456"> <strong>success</strong></a> and was triggered by <strong>SubVersion</strong></span>

		WebHookPayloadContent content = framework.getWebHookContent();
		System.out.println(content.getBuildStatusHtml());
		assertEquals(htmlStatus, content.getBuildStatusHtml());
	}

	@Test
	public void testCustomBuildStatusHtml() throws JDOMException, IOException {
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-custom-templates.xml"));
		WebHookPayloadContent content = framework.getWebHookContent();
		System.out.println(content.getBuildStatusHtml());
		assertTrue(content.getBuildStatusHtml().equals("master ATestProject"));
	}
}
