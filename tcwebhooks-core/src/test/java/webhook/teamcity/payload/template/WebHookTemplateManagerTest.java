package webhook.teamcity.payload.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateFileChangeHandler;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateBranchText;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateFormat;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItems;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateText;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;

public class WebHookTemplateManagerTest {
	
	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm;
	WebHookPayloadManager wpm;
	
	@Before
	public void setup(){
		
	}
	
	@Test
	public void TestSlackComTemplateRegistration(){
		wtm = mock(WebHookTemplateManager.class);
		AbstractPropertiesBasedWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
		wht.register();
		verify(wtm).registerTemplateFormatFromSpring(wht);
	}
	
	@Test
	public void TestSlackComTemplate(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wtm = new WebHookTemplateManager(null, new WebHookTemplateJaxHelperImpl());
		AbstractPropertiesBasedWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
		wht.register();
		assertTrue(wtm.getRegisteredTemplates().contains(wht));
	}
	
	@Test
	public void TestXmlTemplatesViaChangeListener(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm, new WebHookTemplateJaxHelperImpl());
		
		//File configFile = new File("src/test/resources/webhook-templates_single-entry-called-testXMLtemplate.xml");
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testXmlTemplate"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm, new WebHookTemplateJaxHelperImpl());
		changeListener.register();
		changeListener.handleConfigFileChange();

		List<WebHookPayloadTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		assertEquals(4, regsiteredTemplates.size());
		assertEquals("testXMLtemplate", wtm.getTemplate("testXMLtemplate").getTemplateShortName());
	}
	
	@Test
	public void TestXmlTemplatesWithTemplateIdsViaChangeListener(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm, null);
		
		//File configFile = new File("src/test/resources/webhook-templates_single-entry-called-testXMLtemplate.xml");
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testXmlTemplateWithTemplateIds"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm, new WebHookTemplateJaxHelperImpl());
		changeListener.register();
		changeListener.handleConfigFileChange();
		
		List<WebHookPayloadTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		assertEquals(2, regsiteredTemplates.size());
		assertEquals("testXMLtemplateWithId", wtm.getTemplate("testXMLtemplateWithId").getTemplateShortName());
	}
	
	@Test
	public void TestCDataTemplatesViaChangeListener(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm, null);
		
		//File configFile = new File("src/test/resources/webhook-templates_single-entry-called-testXMLtemplate.xml");
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testCDataTemplate"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm, new WebHookTemplateJaxHelperImpl());
		changeListener.register();
		changeListener.handleConfigFileChange();
		
		List<WebHookPayloadTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		assertTrue(regsiteredTemplates.size() == 1);
		assertTrue(regsiteredTemplates.get(0).getTemplateShortName().equals("testXMLtemplate"));
		System.out.println("###########################");
		System.out.println(regsiteredTemplates.get(0).getTemplateForState(BuildStateEnum.BUILD_SUCCESSFUL).getTemplateText());
		System.out.println("###########################");
	}
	
	@Test
	public void TestFindMatchingTemplates(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wtm = new WebHookTemplateManager(null, new WebHookTemplateJaxHelperImpl());
		AbstractPropertiesBasedWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
		wht.register();
		TestWebHookTemplate wht2 = new TestWebHookTemplate(wtm);
		wht2.register();
		AaaTestWebHookTemplate wht3 = new AaaTestWebHookTemplate(wtm);
		wht3.register();
		BbbTestWebHookTemplate wht4 = new BbbTestWebHookTemplate(wtm);
		wht4.register();
		assertTrue(wtm.findAllTemplatesForFormat(WebHookPayloadJsonTemplate.FORMAT_SHORT_NAME).contains(wht));
		assertTrue(wtm.findAllTemplatesForFormat(WebHookPayloadJsonTemplate.FORMAT_SHORT_NAME).contains(wht2));
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
		public String getTemplateToolTip() {
			return "Tooltip - TestWebHookTemplate for testing";
		}

		@Override
		public String getTemplateShortName() {
			return "testWebHookTemplate";
		}

		@Override
		public boolean supportsPayloadFormat(String payloadFormat) {
			if (payloadFormat.equalsIgnoreCase("jsonTemplate"))
				return true;
			return false;
		}

		@Override
		public WebHookTemplateContent getTemplateForState(
				BuildStateEnum buildState) {
			return WebHookTemplateContent.create(BuildStateEnum.BUILD_SUCCESSFUL.getShortName(), "{ \fallback\": \"${buildName}", true, "");
		}

		@Override
		public WebHookTemplateContent getBranchTemplateForState(
				BuildStateEnum buildState) {
			return WebHookTemplateContent.create(BuildStateEnum.BUILD_SUCCESSFUL.getShortName(), "{ \fallback\": \"${buildName} - ${branch}", true, "");		}

		@Override
		public Set<BuildStateEnum> getSupportedBuildStates() {
			return new HashSet<>(Arrays.asList(BuildStateEnum.BUILD_SUCCESSFUL));
		}

		@Override
		public Set<BuildStateEnum> getSupportedBranchBuildStates() {
			return new HashSet<>(Arrays.asList(BuildStateEnum.BUILD_SUCCESSFUL));
		}

		@Override
		public String getPreferredDateTimeFormat() {
			return "";
		}

		@Override
		public WebHookTemplateEntity getAsEntity() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public WebHookTemplateConfig getAsConfig() {
			// TODO Auto-generated method stub
			return null;
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
		public int getRank() {
			return 100;
		}
	}
}

