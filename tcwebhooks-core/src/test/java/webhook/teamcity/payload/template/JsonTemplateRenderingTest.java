package webhook.teamcity.payload.template;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.payload.template.render.WebHookStringRenderer.WebHookHtmlRendererException;

public class JsonTemplateRenderingTest {

	SBuildServer mockServer = mock(SBuildServer.class);
	WebHookTemplateManager wtm;
	WebHookPayloadManager wpm;
	
	@Test
	public void TestJsonTemplatesWithHtmlRenderer() throws WebHookHtmlRendererException, WebHookPayloadContentAssemblyException {
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wtm = new WebHookTemplateManager(null, null);
		AbstractPropertiesBasedWebHookTemplate wht = new SlackComWebHookTemplate(wtm);
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

		
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadJsonTemplate whp = new WebHookPayloadJsonTemplate(wpm);
		whp.register();
		SortedMap<String, String> extraParameters = new TreeMap<>();
		
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
