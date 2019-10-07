package webhook.teamcity.payload.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
import webhook.Constants;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateFileChangeHandler;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;
import webhook.teamcity.settings.entity.WebHookTemplateJaxTestHelper;

public class WebHookTemplateManagerTest {
	
	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm;
	WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
	WebHookTemplateJaxHelperImpl webHookTemplateJaxHelper = new WebHookTemplateJaxTestHelper();
	ProjectIdResolver projectIdResolver = mock(ProjectIdResolver.class); 
	
	@Before
	public void setup(){
		
	}
	
	@Test
	public void TestSlackComTemplateRegistration(){
		wtm = mock(WebHookTemplateManager.class);
		AbstractXmlBasedWebHookTemplate wht = new SlackComXmlWebHookTemplate(wtm, wpm, webHookTemplateJaxHelper, projectIdResolver, null);
		wht.register();
		verify(wtm).registerTemplateFormatFromSpring(any(WebHookTemplateFromXml.class));
	}
	
	@Test
	public void TestSlackComTemplate(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wtm = new WebHookTemplateManager(null, new WebHookTemplateJaxHelperImpl(), projectIdResolver);
		AbstractXmlBasedWebHookTemplate wht = new SlackComXmlWebHookTemplate(wtm, wpm, webHookTemplateJaxHelper, projectIdResolver, null);
		wht.register();
		assertEquals(wht.getTemplateId(), wtm.getTemplate(wht.getTemplateId()).getTemplateId());
	}
	
	@Test
	public void TestXmlTemplatesViaChangeListener(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm, new WebHookTemplateJaxHelperImpl(), projectIdResolver);
		
		//File configFile = new File("src/test/resources/webhook-templates_single-entry-called-testXMLtemplate.xml");
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testXmlTemplate"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm, webHookTemplateJaxHelper, null);
		changeListener.register();
		changeListener.handleConfigFileChange();

		List<WebHookPayloadTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		assertEquals(4, regsiteredTemplates.size());
		assertEquals("testXMLtemplate", wtm.getTemplate("testXMLtemplate").getTemplateId());
	}
	
	@Test
	public void TestXmlTemplatesWithTemplateIdsViaChangeListener(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm, null, projectIdResolver);
		
		//File configFile = new File("src/test/resources/webhook-templates_single-entry-called-testXMLtemplate.xml");
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testXmlTemplateWithTemplateIds"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm, webHookTemplateJaxHelper, null);
		changeListener.register();
		changeListener.handleConfigFileChange();
		
		List<WebHookPayloadTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		assertEquals(2, regsiteredTemplates.size());
		assertEquals("testXMLtemplateWithId", wtm.getTemplate("testXMLtemplateWithId").getTemplateId());
	}
	
	@Test
	public void TestCDataTemplatesViaChangeListener(){
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm, null, projectIdResolver);
		
		//File configFile = new File("src/test/resources/webhook-templates_single-entry-called-testXMLtemplate.xml");
		ServerPaths serverPaths = new ServerPaths(new File("src/test/resources/testCDataTemplate"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm, webHookTemplateJaxHelper, null);
		changeListener.register();
		changeListener.handleConfigFileChange();
		
		List<WebHookPayloadTemplate> regsiteredTemplates = wtm.getRegisteredTemplates();
		assertEquals(1, regsiteredTemplates.size());
		assertTrue(regsiteredTemplates.get(0).getTemplateId().equals("testXMLtemplate"));
		System.out.println("###########################");
		System.out.println(regsiteredTemplates.get(0).getTemplateForState(BuildStateEnum.BUILD_SUCCESSFUL).getTemplateText());
		System.out.println("###########################");
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
		public String getTemplateId() {
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

		@Override
		public String getProjectId() {
			return Constants.ROOT_PROJECT_ID;
		}

		
	}
	
	class AaaTestWebHookTemplate extends TestWebHookTemplate{

		public AaaTestWebHookTemplate(
				WebHookTemplateManager webhookTemplateManager) {
			super(webhookTemplateManager);
		}
		
		@Override
		public String getTemplateId() {
			return "AaaTestWebHookTemplate";
		}
		
	}
	
	class BbbTestWebHookTemplate extends TestWebHookTemplate{
		public BbbTestWebHookTemplate(
				WebHookTemplateManager webhookTemplateManager) {
			super(webhookTemplateManager);
		}
		
		@Override
		public String getTemplateId() {
			return "BbbTestWebHookTemplate";
		}
		
		@Override
		public int getRank() {
			return 100;
		}
	}
}

