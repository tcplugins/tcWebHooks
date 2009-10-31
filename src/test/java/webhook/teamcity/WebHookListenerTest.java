package webhook.teamcity;

import static org.mockito.Mockito.*;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookListenerTest {
	SBuildServer sBuildServer = mock(SBuildServer.class);
	ProjectSettingsManager settings = mock(ProjectSettingsManager.class);
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookPayloadManager manager = mock(WebHookPayloadManager.class);
	WebHookProjectSettings projSettings = mock(WebHookProjectSettings.class);
	
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

//	@Test
//	public void testGetFromConfig() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testBuildStartedSRunningBuild() {
		
		
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		when(projSettings.isEnabled()).thenReturn(true);
		
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager);
		whl.register();
		whl.buildStarted(sRunningBuild);
	}

	@Test
	public void testBuildFinishedSRunningBuild() {
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		when(projSettings.isEnabled()).thenReturn(true);
		
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager);
		whl.register();
		whl.buildFinished(sRunningBuild);
	}

	@Test
	public void testBuildInterruptedSRunningBuild() {
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		when(projSettings.isEnabled()).thenReturn(true);
		
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager);
		whl.register();
		whl.buildInterrupted(sRunningBuild);
	}

	@Test
	public void testBeforeBuildFinishSRunningBuild() {
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		when(projSettings.isEnabled()).thenReturn(true);
		
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager);
		whl.register();
		whl.beforeBuildFinish(sRunningBuild);
	}

	@Test
	public void testBuildChangedStatusSRunningBuildStatusStatus() {
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		when(projSettings.isEnabled()).thenReturn(true);
		
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager);
		Status oldStatus = Status.NORMAL;
		Status newStatus = Status.FAILURE;
		whl.register();
		whl.buildChangedStatus(sRunningBuild, oldStatus, newStatus);
	}

//	@Test
//	public void testResponsibleChangedSBuildTypeResponsibilityInfoResponsibilityInfoBoolean() {
//		
//	}

}
