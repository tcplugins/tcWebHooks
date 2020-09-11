package webhook.teamcity.payload.template;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.payload.template.render.WebHookStringRenderer.WebHookHtmlRendererException;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;
import webhook.teamcity.settings.entity.WebHookTemplateJaxTestHelper;

public class JsonTemplateRenderingTest {

	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm;
	WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
	WebHookTemplateJaxHelperImpl webHookTemplateJaxHelper = new WebHookTemplateJaxTestHelper();
	ProjectIdResolver projectIdResolver = mock(ProjectIdResolver.class);

	
	@Test
	public void TestJsonTemplatesWithHtmlRenderer() throws WebHookHtmlRendererException, WebHookPayloadContentAssemblyException {
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		
		when(projectIdResolver.getExternalProjectId(Mockito.eq("project1"))).thenReturn("ATestProject");
		when(projectIdResolver.getInternalProjectId(Mockito.eq("ATestProject"))).thenReturn("project1");
		
		when(projectIdResolver.getExternalProjectId(Mockito.eq("project0"))).thenReturn("_Root");
		when(projectIdResolver.getInternalProjectId(Mockito.eq("_Root"))).thenReturn("project0");
		
		wtm = new WebHookTemplateManager(null, null, null);
		AbstractXmlBasedWebHookTemplate wht = new SlackComXmlWebHookTemplate(wtm, wpm, webHookTemplateJaxHelper, projectIdResolver, null);
		wht.register();

		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		when (previousBuild.getFinishDate()).thenReturn(new Date());
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");

		WebHookVariableResolverManager variableResolverManager = new WebHookVariableResolverManagerImpl();
		variableResolverManager.registerVariableResolverFactory(new WebHooksBeanUtilsVariableResolverFactory());
		
		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadJsonTemplate whp = new WebHookPayloadJsonTemplate(wpm, variableResolverManager);
		whp.register();
		ExtraParameters extraParameters = new ExtraParameters();
		
		extraParameters.put("item1", "content1");
		extraParameters.put("item2", "content2");
		extraParameters.put("item3", "content3");
		extraParameters.put("item4", "content4");
		extraParameters.put("item5", "content5");
		
		
		//WebHookPayloadContent content = new WebHookPayloadContent(mockServer, sRunningBuild, previousBuild, BuildStateEnum.BUILD_SUCCESSFUL, extraParameters, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		String result = whp.buildFinished(sRunningBuild, previousBuild, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates(), wht.getBranchTemplateForState(BuildStateEnum.BUILD_SUCCESSFUL));
		System.out.println(wht.getBranchTemplateForState(BuildStateEnum.BUILD_SUCCESSFUL));
		System.out.println(whp.getWebHookStringRenderer().render(wht.getBranchTemplateForState(BuildStateEnum.BUILD_SUCCESSFUL).getTemplateText()));
		System.out.println(result);
		System.out.println(whp.getWebHookStringRenderer().render(result));
		
	}

}
