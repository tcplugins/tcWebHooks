package webhook.teamcity.payload.template;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateContent;
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
		wtm = new WebHookTemplateManager(mockServer, null, null);
		SlackComWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
		wht.register();
		assertTrue(wtm.getRegisteredTemplates().contains(wht.getTemplateShortName()));
	}
	
	@Test
	public void TestFindMatchingTemplates(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wtm = new WebHookTemplateManager(mockServer, null, null);
		SlackComWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
		wht.register();
		TestWebHookTemplate wht2 = new TestWebHookTemplate(wtm);
		wht2.register();
		AaaTestWebHookTemplate wht3 = new AaaTestWebHookTemplate(wtm);
		wht3.register();
		BbbTestWebHookTemplate wht4 = new BbbTestWebHookTemplate(wtm);
		wht4.register();
		assertTrue(wtm.findAllTemplatesForFormat(WebHookPayloadTailoredJson.FORMAT_SHORT_NAME).contains(wht));
		assertTrue(wtm.findAllTemplatesForFormat(WebHookPayloadTailoredJson.FORMAT_SHORT_NAME).contains(wht2));
		System.out.println(wht.getTemplateForState(BuildStateEnum.BUILD_SUCCESSFUL).getTemplateText());
	}
	
	
	class TestWebHookTemplate extends AbstractWebHookTemplate {
		
		public TestWebHookTemplate(WebHookTemplateManager webhookTemplateManager) {
			setTemplateManager(webhookTemplateManager);
		}

		@Override
		public void register() {
			super.register(this);
		}

		@Override
		public String getTemplateDescription() {
			return "TestWebHookTemplate for testing";
		}

		@Override
		public String getTemplateToolTipText() {
			return "Tooltip - TestWebHookTemplate for testing";
		}

		@Override
		public String getTemplateShortName() {
			return "testWebHookTemplate";
		}

		@Override
		public boolean supportsPayloadFormat(String payloadFormat) {
			if (payloadFormat.equals("tailoredjson"))
				return true;
			return false;
		}

		@Override
		public WebHookTemplateContent getTemplateForState(
				BuildStateEnum buildState) {
			return WebHookTemplateContent.create(BuildStateEnum.BUILD_SUCCESSFUL.getShortName(), "{ \fallback\": \"${buildName}", true);
		}
		
	}
	
	class AaaTestWebHookTemplate extends TestWebHookTemplate{

		public AaaTestWebHookTemplate(
				WebHookTemplateManager webhookTemplateManager) {
			super(webhookTemplateManager);
		}
		
		@Override
		public String getTemplateShortName() {
			return "AaaTestWebHookTemplate";
		}
		
	}
	
	class BbbTestWebHookTemplate extends TestWebHookTemplate{
		public BbbTestWebHookTemplate(
				WebHookTemplateManager webhookTemplateManager) {
			super(webhookTemplateManager);
		}
		
		@Override
		public String getTemplateShortName() {
			return "BbbTestWebHookTemplate";
		}
		
		@Override
		public Integer getRank() {
			return 100;
		}
	}
}

