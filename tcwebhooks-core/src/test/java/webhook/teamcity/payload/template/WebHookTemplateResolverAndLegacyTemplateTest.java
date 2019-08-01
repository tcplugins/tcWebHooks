package webhook.teamcity.payload.template;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;

public class WebHookTemplateResolverAndLegacyTemplateTest {

	@Test
	public void testEmptyTemplateSupportsFormatAndState() {
		
		SBuildServer server = mock(SBuildServer.class);
		SBuildType build = new MockSBuildType("test", "something else", "build01");
		SProject project = new MockSProject("test", "something", "project01", "MyProject", build);
		WebHookPayloadManager payloadManager = new WebHookPayloadManager(server);
		WebHookTemplateManager templateManager = new WebHookTemplateManager(payloadManager, new WebHookTemplateJaxHelperImpl());
		WebHookTemplateResolver resolver = new WebHookTemplateResolver(templateManager, payloadManager);
		
		WebHookPayloadTemplate template = new LegacyEmptyWebHookTemplate(templateManager);
		template.register();
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_STARTED, project, "legacy-empty"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_INTERRUPTED, project, "legacy-empty"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BEFORE_BUILD_FINISHED, project, "legacy-empty"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_BROKEN, project, "legacy-empty"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FIXED, project, "legacy-empty"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FAILED, project, "legacy-empty"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_SUCCESSFUL, project, "legacy-empty"));
		
	}
	
	@Test
	public void testJsonTemplateSupportsFormatAndState() {
		
		SBuildServer server = mock(SBuildServer.class);
		SBuildType build = new MockSBuildType("test", "something else", "build01");
		SProject project = new MockSProject("test", "something", "project01", "MyProject", build);
		WebHookPayloadManager payloadManager = new WebHookPayloadManager(server);
		WebHookTemplateManager templateManager = new WebHookTemplateManager(payloadManager, new WebHookTemplateJaxHelperImpl());
		WebHookTemplateResolver resolver = new WebHookTemplateResolver(templateManager, payloadManager);
		
		WebHookPayloadTemplate template = new LegacyJsonWebHookTemplate(templateManager);
		template.register();
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_STARTED, project, "legacy-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_INTERRUPTED, project, "legacy-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BEFORE_BUILD_FINISHED, project, "legacy-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_BROKEN, project, "legacy-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FIXED, project, "legacy-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FAILED, project, "legacy-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_SUCCESSFUL, project, "legacy-json"));
		
	}
	
	@Test
	public void testNameValuePairsTemplateSupportsFormatAndState() {
		
		SBuildServer server = mock(SBuildServer.class);
		SBuildType build = new MockSBuildType("test", "something else", "build01");
		SProject project = new MockSProject("test", "something", "project01", "MyProject", build);
		WebHookPayloadManager payloadManager = new WebHookPayloadManager(server);
		WebHookTemplateManager templateManager = new WebHookTemplateManager(payloadManager, new WebHookTemplateJaxHelperImpl());
		WebHookTemplateResolver resolver = new WebHookTemplateResolver(templateManager, payloadManager);
		
		WebHookPayloadTemplate template = new LegacyNameValuePairsWebHookTemplate(templateManager);
		template.register();
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_STARTED, project, "legacy-nvpairs"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_INTERRUPTED, project, "legacy-nvpairs"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BEFORE_BUILD_FINISHED, project, "legacy-nvpairs"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_BROKEN, project, "legacy-nvpairs"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FIXED, project, "legacy-nvpairs"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FAILED, project, "legacy-nvpairs"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_SUCCESSFUL, project, "legacy-nvpairs"));
		
	}
	
	@Test
	public void testTailoredJsonTemplateSupportsFormatAndState() {
		
		SBuildServer server = mock(SBuildServer.class);
		SBuildType build = new MockSBuildType("test", "something else", "build01");
		SProject project = new MockSProject("test", "something", "project01", "MyProject", build);
		WebHookPayloadManager payloadManager = new WebHookPayloadManager(server);
		WebHookTemplateManager templateManager = new WebHookTemplateManager(payloadManager, new WebHookTemplateJaxHelperImpl());
		WebHookTemplateResolver resolver = new WebHookTemplateResolver(templateManager, payloadManager);
		
		WebHookPayloadTemplate template = new LegacyTailoredJsonWebHookTemplate(templateManager);
		template.register();
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_STARTED, project, "legacy-tailored-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_INTERRUPTED, project, "legacy-tailored-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BEFORE_BUILD_FINISHED, project, "legacy-tailored-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_BROKEN, project, "legacy-tailored-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FIXED, project, "legacy-tailored-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FAILED, project, "legacy-tailored-json"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_SUCCESSFUL, project, "legacy-tailored-json"));
		
	}
	
	@Test
	public void testXmlTemplateSupportsFormatAndState() {
		
		SBuildServer server = mock(SBuildServer.class);
		SBuildType build = new MockSBuildType("test", "something else", "build01");
		SProject project = new MockSProject("test", "something", "project01", "MyProject", build);
		WebHookPayloadManager payloadManager = new WebHookPayloadManager(server);
		WebHookTemplateManager templateManager = new WebHookTemplateManager(payloadManager, new WebHookTemplateJaxHelperImpl());
		WebHookTemplateResolver resolver = new WebHookTemplateResolver(templateManager, payloadManager);
		
		WebHookPayloadTemplate template = new LegacyXmlWebHookTemplate(templateManager);
		template.register();
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_STARTED, project, "legacy-xml"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_INTERRUPTED, project, "legacy-xml"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BEFORE_BUILD_FINISHED, project, "legacy-xml"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_BROKEN, project, "legacy-xml"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FIXED, project, "legacy-xml"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FAILED, project, "legacy-xml"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_SUCCESSFUL, project, "legacy-xml"));
		
	}

}
