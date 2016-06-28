package webhook.teamcity.payload.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
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
	Map<String, ExtraParametersMap> allProperties;
	

	@Before
	public void setup(){
		sBuildType.setProject(sProject);
		extraParameters = new TreeMap<>();
		//extraParameters.put("build.vcs.number", "${build.vcs.number}");
		extraParameters.put("body.passed", "Yey, this build has passed for ${buildType}.");
		extraParameters.put("body", "${body.passed}");
		extraParameters.put("body2", "${body.failed}");
		extraParameters.put("sha", "${build.vcs.number}");
		teamcityProperties = new TreeMap<>();
		teamcityProperties.put("env.isInATest", "Yes, we are in a test");
		teamcityProperties.put("buildFullName", "Hopefully will never see this.");
		teamcityProperties.put("buildFullName", "Hopefully will never see this.");
		teamcityProperties.put("someTagThing", "A ~peice of text! with <> s<<<tuff in it%.");
		teamcityProperties.put("build.vcs.number", "3b0a11eda029aaeb349993cb070a1c2e5987906c");
		teamcityProperties.put("body.failed", "Boo, this build has failed for ${buildType}.");
		teamcityProperties.put("config", "This is some config thing");
		teamcityProperties.put("builder.appVersion", "This is the appVersion");
		sBuildServer = mock(SBuildServer.class);
		allProperties = new LinkedHashMap<>();
		allProperties.put("teamcity", new ExtraParametersMap(teamcityProperties));
		allProperties.put("webhook", new ExtraParametersMap(extraParameters));
	}
	
	@Test
	public void testBuild() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("This is a test ${buildFullName}", new WebHooksBeanUtilsVariableResolver(content, allProperties));
		assertEquals("This is a test Test Project :: Test Build", builder.build());
		System.out.println(content.getBuildFullName());
		
	}
	
	@Test
	public void testBuildWithDoubleResultion() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("This is a test ${buildFullName}", new WebHooksBeanUtilsVariableResolver(content, allProperties));
		assertEquals("This is a test Test Project :: Test Build", builder.build());
		System.out.println(content.getBuildFullName());
		
	}
	
	@Test
	public void testTeamCityProperties() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("This is a test ${env.isInATest}", new WebHooksBeanUtilsVariableResolver(content, allProperties));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
		assertEquals("This is a test Yes, we are in a test", builder.build());
	}
	
	@Test
	public void testDateTemplateProperty() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("The date now is ${now(\"yyyy-MM-dd'T'HH:mm:ss.SSSXXX\")}", new WebHooksBeanUtilsVariableResolver(content, allProperties));
		System.out.println(builder.build());
		builder = VariableMessageBuilder.create("The month now is ${now(\"yyyy-MM\")}", new WebHooksBeanUtilsVariableResolver(content, allProperties));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
	}
	
	@Test
	public void testSanitiseTemplateProperty() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("Sanitising ${sanitise(someTagThing)}", new WebHooksBeanUtilsVariableResolver(content, allProperties));
		System.out.println(builder.build());
		builder = VariableMessageBuilder.create("Sanitizing ${sanitize(someTagThing)}", new WebHooksBeanUtilsVariableResolver(content, allProperties));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
	}
	
	@Test
	public void TestResolvingParamtersFromTeamCityAndExtras(){
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("{ \"notifyType\": \"${notifyType}\",  "
				+ "\"buildId\": \"${buildId}\", "
				+ "\"buildTypeId\": \"${buildTypeId}\", "
				+ "\"buildStatus\": \"${buildStatus}\", "
				+ "\"config\": \"${config}\","
				+ "\"appVersion\": \"${builder.appVersion}\", "
				+ "\"sha\": \"${build.vcs.number}\" }", 
            new WebHooksBeanUtilsVariableResolver(content, content.getAllParameters()));
		System.out.println(builder.build());
		assertEquals("{ \"notifyType\": \"beforeBuildFinish\",  \"buildId\": \"123456\", \"buildTypeId\": \"TestBuild\", \"buildStatus\": \"Running\", \"config\": \"This is some config thing\",\"appVersion\": \"This is the appVersion\", \"sha\": \"3b0a11eda029aaeb349993cb070a1c2e5987906c\" }", builder.build());
	}
	
	@Test
	public void TestResolvingParamtersFromTeamCityAndExtrasAndEscapeJson(){
		extraParameters.put("jsonString", " \" Some string that shouldn't be used\"");
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("{ "
				+ "\"myJson\": \"${escapejson(jsonString)}\" "
				+ "}", 
            new WebHooksBeanUtilsVariableResolver(content, content.getAllParameters()));
		System.out.println(builder.build());
		assertEquals("{ \"myJson\": \" \\\" Some string that shouldn't be used\\\"\" }", builder.build());
	}

}
