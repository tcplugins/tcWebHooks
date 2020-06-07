package webhook.teamcity.payload.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;

import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateFileChangeHandler;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;

public class XMLTemplatesFrom1_2LoadingTest {

	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm;
	WebHookPayloadManager wpm;
	
	@Test
	public void TestXmlTemplatesCanBeLoadedFrom1_2_ConfigFile(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm, new WebHookTemplateJaxHelperImpl());
		
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testRollbackDataFrom1.2"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm, new WebHookTemplateJaxHelperImpl());
		changeListener.register();
		changeListener.handleConfigFileChange();
		
		List<WebHookPayloadTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		WebHookPayloadTemplate template = wtm.getTemplate("test-01");
		assertNotNull(template);
		assertEquals("jsonTemplate", template.getAsConfig().getFormat());
		assertEquals(4, regsiteredTemplates.size());
	}

}
