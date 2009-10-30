package webhook.teamcity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.WebHookMainSettings;

public class WebHookListenerTest {
	SBuildServer sBuildServer = mock(SBuildServer.class);
	ProjectSettingsManager settings = mock(ProjectSettingsManager.class);
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookPayloadManager manager = mock(WebHookPayloadManager.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWebHookListener() {
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager);
	}

	@Test
	public void testRegister() {
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager);
		whl.register();
	}

	@Test
	public void testGetFromConfig() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildStartedSRunningBuild() {
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager);
		whl.register();
		whl.buildStarted(sRunningBuild);
	}

	@Test
	public void testBuildFinishedSRunningBuild() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildInterruptedSRunningBuild() {
		fail("Not yet implemented");
	}

	@Test
	public void testBeforeBuildFinishSRunningBuild() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildChangedStatusSRunningBuildStatusStatus() {
		fail("Not yet implemented");
	}

	@Test
	public void testResponsibleChangedSBuildTypeResponsibilityInfoResponsibilityInfoBoolean() {
		fail("Not yet implemented");
	}

}
