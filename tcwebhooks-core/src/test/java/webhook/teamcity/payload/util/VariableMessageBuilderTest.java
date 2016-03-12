package webhook.teamcity.payload.util;

import static org.mockito.Mockito.mock;

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
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.content.WebHookPayloadContent;

public class VariableMessageBuilderTest {
	
	MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
	SBuildServer sBuildServer;
	SortedMap<String, String> extraParameters;
	SortedMap<String, String> teamcityProperties;

	@Before
	public void setup(){
		sBuildType.setProject(sProject);
		extraParameters = new TreeMap<String, String>();
		teamcityProperties = new TreeMap<String, String>();
		teamcityProperties.put("env.isInATest", "Yes, we are in a test");
		teamcityProperties.put("buildFullName", "Hopefully will never see this.");
		teamcityProperties.put("buildFullName", "Hopefully will never see this.");
		teamcityProperties.put("someTagThing", "A ~peice of text! with <> s<<<tuff in it%.");
		sBuildServer = mock(SBuildServer.class);
	}
	
	@Test
	public void testBuild() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilderWithVelocity builder = VariableMessageBuilderWithVelocity.create("This is a test ${buildFullName}", new WebHooksBeanUtilsVariableResolver(content, new ExtraParametersMap(teamcityProperties)));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
	}
	
	@Test
	public void testTeamCityProperties() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("This is a test ${env.isInATest}", new WebHooksBeanUtilsVariableResolver(content, new ExtraParametersMap(teamcityProperties)));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
	}
	
	@Test
	public void testDateTemplateProperty() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("The date now is ${now(\"yyyy-MM-dd'T'HH:mm:ss.SSSXXX\")}", new WebHooksBeanUtilsVariableResolver(content, new ExtraParametersMap(teamcityProperties)));
		System.out.println(builder.build());
		builder = VariableMessageBuilder.create("The month now is ${now(\"yyyy-MM\")}", new WebHooksBeanUtilsVariableResolver(content, new ExtraParametersMap(teamcityProperties)));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
	}
	
	@Test
	public void testSanitiseTemplateProperty() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("Sanitising ${sanitise(someTagThing)}", new WebHooksBeanUtilsVariableResolver(content, new ExtraParametersMap(teamcityProperties)));
		System.out.println(builder.build());
		builder = VariableMessageBuilder.create("Sanitizing ${sanitize(someTagThing)}", new WebHooksBeanUtilsVariableResolver(content, new ExtraParametersMap(teamcityProperties)));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
	}

}
