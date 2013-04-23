package webhook.teamcity.payload.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.content.WebHookPayloadContent;

public class VariableMessageBuilderTest {
	
	MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
	SBuildServer sBuildServer;
	SortedMap<String, String> extraParameters;

	@Before
	public void setup(){
		sBuildType.setProject(sProject);
		extraParameters = new TreeMap<String, String>();
		sBuildServer = mock(SBuildServer.class);
	}
	
	@Test
	public void testBuild() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters,WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("This is a test ${buildFullName}", new WebHooksBeanUtilsVariableResolver(content));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
	}

}
