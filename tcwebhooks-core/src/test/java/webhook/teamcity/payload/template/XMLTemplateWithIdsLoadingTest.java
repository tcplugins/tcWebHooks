package webhook.teamcity.payload.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;

import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateFileChangeHandler;
import webhook.teamcity.payload.WebHookTemplateManager;

public class XMLTemplateWithIdsLoadingTest {

	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm;
	WebHookPayloadManager wpm;
	
	@Test
	public void TestXmlTemplatesWithIdsViaChangeListener(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm);
		
		//File configFile = new File("src/test/resources/webhook-templates_single-entry-called-testXMLtemplate.xml");
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testXmlTemplateWithTemplateIds"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm);
		changeListener.register();
		changeListener.handleConfigFileChange();
		
		List<WebHookTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		WebHookTemplate template = wtm.getTemplate("testXMLtemplateWithId");
		assertTrue(template != null);
		assertEquals(2, regsiteredTemplates.size());
	}

}
