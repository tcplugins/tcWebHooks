package webhook.teamcity.payload.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;

public class VariableMessageBuilderTestBase {

	MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	protected MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "123");
	protected SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
	protected SBuildServer sBuildServer;
	protected ExtraParameters extraParameters;
	protected SortedMap<String, String> webhookProperties;
	protected SortedMap<String, String> teamcityProperties;
	protected ExtraParameters allProperties;
	protected VariableResolverFactory variableResolverFactory = new WebHooksBeanUtilsVariableResolverFactory();

	@Before
	public void setup() {
		extraParameters = new ExtraParameters();
		sBuildType.setProject(sProject);
		webhookProperties = new TreeMap<>();
		//extraParameters.put("build.vcs.number", "${build.vcs.number}");
		webhookProperties.put("body.passed", "Yey, this build has passed for ${buildType}.");
		webhookProperties.put("body", "${body.passed}");
		webhookProperties.put("body2", "${body.failed}");
		webhookProperties.put("sha", "${build.vcs.number}");
		extraParameters.addAll("webhook", webhookProperties, true);
		teamcityProperties = new TreeMap<>();
		teamcityProperties.put("lowercaseString", "yes, we are all lowercase");
		teamcityProperties.put("env.isInATest", "Yes, we are in a test");
		teamcityProperties.put("buildFullName", "Hopefully will never see this.");
		teamcityProperties.put("buildFullName", "Hopefully will never see this.");
		teamcityProperties.put("someTagThing", "A ~peice of text! with <> s<<<tuff in it%.");
		teamcityProperties.put("build.vcs.number", "3b0a11eda029aaeb349993cb070a1c2e5987906c");
		teamcityProperties.put("body.failed", "Boo, this build has failed for ${buildType}.");
		teamcityProperties.put("config", "This is some config thing");
		teamcityProperties.put("builder.appVersion", "This is the appVersion");
		extraParameters.addAll("teamcity", teamcityProperties, false);
		sBuildServer = mock(SBuildServer.class);
		when(sBuildServer.getRootUrl()).thenReturn("http://test.url");
		allProperties = extraParameters;
	}

}
