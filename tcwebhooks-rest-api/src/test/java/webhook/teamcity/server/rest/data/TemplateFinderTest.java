package webhook.teamcity.server.rest.data;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.AbstractPropertiesBasedWebHookTemplate;
import webhook.teamcity.payload.template.AbstractXmlBasedWebHookTemplate;
import webhook.teamcity.payload.template.SlackComCompactXmlWebHookTemplate;
import webhook.teamcity.payload.template.SlackComWebHookTemplate;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;

public class TemplateFinderTest {
	
	@Mock	SBuildServer server;
	
	private WebHookTemplateManager webHookTemplateManager;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		when(server.getRootUrl()).thenReturn("http://test.url");
		
		WebHookPayloadManager webHookPayloadManager = new WebHookPayloadManager(server);
		webHookTemplateManager = new WebHookTemplateManager(webHookPayloadManager, new WebHookTemplateJaxHelperImpl());
		
		AbstractXmlBasedWebHookTemplate wht = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, new WebHookTemplateJaxHelperImpl());
		wht.register();
	}

	@Test
	public void testFindTemplateWithNoDimention() {
		TemplateFinder templateFinder = new TemplateFinder(webHookTemplateManager);
		WebHookTemplateEntity e = templateFinder.findTemplateById("slack.com-compact");
		assertEquals("slack.com-compact", e.getName());
	}
	
	@Test
	public void testFindTemplateByIdDimension() {
		TemplateFinder templateFinder = new TemplateFinder(webHookTemplateManager);
		WebHookTemplateEntity e = templateFinder.findTemplateById("id:slack.com-compact");
		assertEquals("slack.com-compact", e.getName());
	}
	
	@Test
	public void testFindTemplateByNameDimension() {
		TemplateFinder templateFinder = new TemplateFinder(webHookTemplateManager);
		WebHookTemplateEntity e = templateFinder.findTemplateById("name:slack.com-compact");
		assertEquals("slack.com-compact", e.getName());
		System.out.println(e.getTemplateDescription());
	}

}
