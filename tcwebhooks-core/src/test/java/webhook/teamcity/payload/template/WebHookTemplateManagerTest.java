package webhook.teamcity.payload.template;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadTailoredJson;

public class WebHookTemplateManagerTest {
	
	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm;
	
	@Before
	public void setup(){
		
	}
	
	@Test
	public void TestSlackComTemplateRegistration(){
		wtm = mock(WebHookTemplateManager.class);
		SlackComWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
		wht.register();
		verify(wtm).registerTemplateFormatFromSpring(wht);
	}
	
	@Test
	public void TestSlackComTemplate(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wtm = new WebHookTemplateManager(mockServer);
		SlackComWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
		wht.register();
		assertTrue(wtm.getRegisteredTemplates().contains(wht.getTemplateShortName()));
	}
	
	@Test
	public void TestFindMatchingTemplates(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wtm = new WebHookTemplateManager(mockServer);
		SlackComWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
		wht.register();
		assertTrue(wtm.findAllTemplatesForFormat(WebHookPayloadTailoredJson.FORMAT_SHORT_NAME).contains(wht));
		System.out.println(wht.getTemplateForState(BuildStateEnum.BUILD_SUCCESSFUL).getTemplateText());
	}
}

