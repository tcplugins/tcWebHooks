package webhook.teamcity.payload.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.serverSide.Branch;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplateJaxTestHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class SlackComCompactXmlWebHookTemplateTest {
	
	@Mock
	private SBuild sRunningBuild;

	@Mock
	private SBuildServer server;

	private WebHookTemplateManager webHookTemplateManager;
	private WebHookPayloadManager webHookPayloadManager;
	private WebHookTemplateJaxHelper webHookTemplateJaxHelper;
	private WebHookTemplateResolver webHookTemplateResolver;
	private WebHookVariableResolverManager webHookVariableResolverManager; 

	private WebHookPayload payloadFormat;
	
	@Test
	public void testLoadDefaultCompactSlackTemplateAndVerifyThatThereIsATemplateForChangesLoaded() {
		
		setup();
		
		WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
		slackCompact.register();
		webHookTemplateManager.registerTemplateFormatFromSpring(slackCompact);
		
		WebHookContentBuilder webHookContentBuilder = new WebHookContentBuilder(server, webHookTemplateResolver, webHookVariableResolverManager);
		
		WebHookTemplateContent webHookTemplateContentChangesLoaded = webHookContentBuilder.findTemplateForState(sRunningBuild, BuildStateEnum.CHANGES_LOADED, slackCompact.getTemplateId(), payloadFormat);
		assertNotNull(webHookTemplateContentChangesLoaded);
		
		WebHookTemplateContent webHookTemplateContentSuccessful = webHookContentBuilder.findTemplateForState(sRunningBuild, BuildStateEnum.BUILD_SUCCESSFUL, slackCompact.getTemplateId(), payloadFormat);
		assertNotNull(webHookTemplateContentSuccessful);
	}
	
	@Test(expected=UnSupportedBuildStateException.class)
	public void testLoadDefaultCompactSlackTemplateAndThenOverideItAndVerifyThatThereIsNoTemplateForChangesLoaded() throws FileNotFoundException, JAXBException {
		
		setup();
		
		WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
		slackCompact.register();
		webHookTemplateManager.registerTemplateFormatFromSpring(slackCompact);
		
    	WebHookTemplates templatesList =  webHookTemplateJaxHelper.readTemplates("src/test/resources/testSlackCompactOverriden/webhook-templates.xml");
		webHookTemplateManager.registerAllXmlTemplates(templatesList);
		
		WebHookContentBuilder webHookContentBuilder = new WebHookContentBuilder(server, webHookTemplateResolver, webHookVariableResolverManager);
		
		WebHookTemplateContent webHookTemplateContentChangesLoaded = webHookContentBuilder.findTemplateForState(sRunningBuild, BuildStateEnum.CHANGES_LOADED, slackCompact.getTemplateId(), payloadFormat);
		assertNull(webHookTemplateContentChangesLoaded);
		
		WebHookTemplateContent webHookTemplateContentSuccessful = webHookContentBuilder.findTemplateForState(sRunningBuild, BuildStateEnum.BUILD_SUCCESSFUL, slackCompact.getTemplateId(), payloadFormat);
		assertNotNull(webHookTemplateContentSuccessful);
	}
	
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		setupPayloadManagerAndRegisterJsonTemplate();
		webHookTemplateJaxHelper = new WebHookTemplateJaxTestHelper();
		webHookTemplateManager  = new WebHookTemplateManager(webHookPayloadManager, webHookTemplateJaxHelper);
		webHookTemplateResolver = new WebHookTemplateResolver(webHookTemplateManager, webHookPayloadManager);
		webHookVariableResolverManager  = new WebHookVariableResolverManagerImpl();
		webHookVariableResolverManager.registerVariableResolverFactory(new WebHooksBeanUtilsVariableResolverFactory());
		setupSbuildMock();
		
	}

	private void setupSbuildMock() {
		when(sRunningBuild.getBranch()).thenReturn(new Branch() {
			
			@Override
			public boolean isDefaultBranch() {
				return false;
			}
			
			@Override
			public String getName() {
				return "release-branch";
			}
			
			@Override
			public String getDisplayName() {
				return "Release Branch";
			}
		});
	}

	private void setupPayloadManagerAndRegisterJsonTemplate() {
		webHookPayloadManager = new WebHookPayloadManager(server);
		payloadFormat = new WebHookPayloadJsonTemplate(webHookPayloadManager, webHookVariableResolverManager);
		payloadFormat.register();
	}
	

}
