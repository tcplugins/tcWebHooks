package webhook.teamcity.payload.util;

import static org.junit.Assert.*;

import org.junit.Test;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.content.WebHookPayloadContent.SimpleSerialiser;

public class VariableMessageBuilderTest extends VariableMessageBuilderTestBase {
	
	@Test
	public void testBuild() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("This is a test ${buildFullName}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		assertEquals("This is a test Test Project :: Test Build", builder.build());
		System.out.println(content.getBuildFullName());
		
	}
	
	@Test
	public void testBuildWithDoubleResultion() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("This is a test ${buildFullName}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		assertEquals("This is a test Test Project :: Test Build", builder.build());
		System.out.println(content.getBuildFullName());
		
	}
	
	@Test
	public void testTeamCityProperties() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("This is a test ${env.isInATest}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
		assertEquals("This is a test Yes, we are in a test", builder.build());
	}
	
	@Test
	public void testDateTemplateProperty() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("The date now is ${now(\"yyyy-MM-dd'T'HH:mm:ss.SSSXXX\")}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		System.out.println(builder.build());
		builder = VariableMessageBuilder.create("The month now is ${now(\"yyyy-MM\")}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		System.out.println(builder.build());
		System.out.println(content.getBuildFullName());
	}
	
	@Test
	public void testSanitiseTemplateProperty() {
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("Sanitising ${sanitise(someTagThing)}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		System.out.println(builder.build());
		builder = VariableMessageBuilder.create("Sanitizing ${sanitize(someTagThing)}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
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
            new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, content.getAllParameters()));
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
            new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, content.getAllParameters()));
		System.out.println(builder.build());
		assertEquals("{ \"myJson\": \" \\\" Some string that shouldn't be used\\\"\" }", builder.build());
	}
	
	@Test
	public void testSubString(){
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("build.vcs.number ${substr(build.vcs.number,0,7,32)}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		assertEquals("build.vcs.number 3b0a11e", builder.build());
	}
	
	@Test
	public void testSubCapitilise(){
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("blah ${capitalise(lowercaseString)}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		assertEquals("blah Yes, We Are All Lowercase", builder.build());
	}
	
	@Test
	public void testSubCapitilize(){
		WebHookPayloadContent content = new WebHookPayloadContent(sBuildServer, sRunningBuild, previousSuccessfulBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		VariableMessageBuilder builder = VariableMessageBuilder.create("blah ${capitalize(lowercaseString)}", new WebHooksBeanUtilsVariableResolver(new SimpleSerialiser(), content, allProperties));
		assertEquals("blah Yes, We Are All Lowercase", builder.build());
	}

}
