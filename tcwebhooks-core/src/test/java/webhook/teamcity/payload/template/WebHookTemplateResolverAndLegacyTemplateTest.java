package webhook.teamcity.payload.template;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;

public class WebHookTemplateResolverAndLegacyTemplateTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testTemplateSupportsFormatAndState() {
		
		SBuildServer server = mock(SBuildServer.class);
		SBuildType build = new MockSBuildType("test", "something else", "build01");
		SProject project = new MockSProject("test", "something", "project01", "MyProject", build);
		WebHookPayloadManager payloadManager = new WebHookPayloadManager(server);
		WebHookTemplateManager templateManager = new WebHookTemplateManager(payloadManager, new WebHookTemplateJaxHelperImpl());
		WebHookTemplateResolver resolver = new WebHookTemplateResolver(templateManager);
		
		LegacyDeprecatedFormatWebHookTemplate template = new LegacyDeprecatedFormatWebHookTemplate(templateManager);
		template.register();
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_STARTED, project, "json", "none"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_INTERRUPTED, project, "json", "none"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BEFORE_BUILD_FINISHED, project, "json", "none"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_BROKEN, project, "json", "none"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FIXED, project, "json", "none"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_FAILED, project, "json", "none"));
		assertTrue(resolver.templateSupportsFormatAndState(BuildStateEnum.BUILD_SUCCESSFUL, project, "json", "none"));
		
	}

}
