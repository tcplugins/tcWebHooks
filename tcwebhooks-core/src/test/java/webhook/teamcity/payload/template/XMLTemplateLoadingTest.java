package webhook.teamcity.payload.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;

import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateFileChangeHandler;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;

public class XMLTemplateLoadingTest {

	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm;
	WebHookPayloadManager wpm;
	
	@Test
	public void TestXmlBranchAndNonBranchTemplatesViaChangeListener(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm, new WebHookTemplateJaxHelperImpl());
		
		//File configFile = new File("src/test/resources/webhook-templates_single-entry-called-testXMLtemplate.xml");
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testXmlTemplate"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm, new WebHookTemplateJaxHelperImpl());
		changeListener.register();
		changeListener.handleConfigFileChange();
		
		List<WebHookPayloadTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		WebHookPayloadTemplate template = wtm.getTemplate("testXMLtemplateWithCombinedTemplate");
		assertTrue(template != null);
		assertEquals("{ \"anotherMergedbuildStatus\" : \"${buildStatus}\" }", template.getTemplateForState(BuildStateEnum.BUILD_STARTED).getTemplateText());
		assertEquals("{ \"anotherMergedbuildStatus\" : \"${buildStatus}\" }", template.getBranchTemplateForState(BuildStateEnum.BUILD_STARTED).getTemplateText());
		
		assertEquals("{ \"mergedBuildStatus\" : \"${buildStatus}\" }", template.getTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText());
		assertEquals("{ \"mergedBuildStatus\" : \"${buildStatus}\" }", template.getBranchTemplateForState(BuildStateEnum.BUILD_BROKEN).getTemplateText());
		assertEquals(4, regsiteredTemplates.size());
	}

}
