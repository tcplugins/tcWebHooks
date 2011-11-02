package webhook.teamcity;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

import com.intellij.javaee.NewAbstractDataHolder;

import webhook.WebHook;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookListenerTest {
	SBuildServer sBuildServer = mock(SBuildServer.class);
	BuildHistory buildHistory = mock(BuildHistory.class);
	ProjectSettingsManager settings = mock(ProjectSettingsManager.class);
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookPayloadManager manager = mock(WebHookPayloadManager.class);
	WebHookPayload payload = new WebHookPayloadJson(manager);
	WebHookProjectSettings projSettings;
	WebHookFactory factory = mock(WebHookFactory.class);
	WebHook webhook = mock(WebHook.class);
	SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	SFinishedBuild previousFailedBuild = mock(SFinishedBuild.class);
	List<SFinishedBuild> finishedSuccessfulBuilds = new ArrayList<SFinishedBuild>();
	List<SFinishedBuild> finishedFailedBuilds = new ArrayList<SFinishedBuild>();
	MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running");
	MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
	WebHookListener whl;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		whl = new WebHookListener(sBuildServer, settings, configSettings, manager, factory);
		projSettings = new WebHookProjectSettings();
		when(factory.getWebHook()).thenReturn(webhook);
		when(manager.isRegisteredFormat("JSON")).thenReturn(true);
		when(manager.getFormat("JSON")).thenReturn(payload);
		when(sBuildServer.getHistory()).thenReturn(buildHistory);
		when(previousSuccessfulBuild.getBuildStatus()).thenReturn(Status.NORMAL);
		when(previousSuccessfulBuild.isPersonal()).thenReturn(false);
		when(previousFailedBuild.getBuildStatus()).thenReturn(Status.FAILURE);
		when(previousFailedBuild.isPersonal()).thenReturn(false);
		finishedSuccessfulBuilds.add(previousSuccessfulBuild);
		finishedFailedBuilds.add(previousFailedBuild);
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		sBuildType.setProject(sProject);
		whl.register();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWebHookListener() {
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager, factory);
	}

	@Test
	public void testRegister() {
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager, factory);
		whl.register();
		verify(sBuildServer).addListener(whl);
	}

//	@Test
//	public void testGetFromConfig() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testBuildStartedSRunningBuild() throws FileNotFoundException, IOException {
		projSettings.addNewWebHook("1234", "http://text/test", true, BuildState.ALL_ENABLED, "JSON");
		when(webhook.getEventListBitMask()).thenReturn(BuildState.ALL_ENABLED);
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.buildStarted(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}

	@Test
	public void testBuildFinishedSRunningBuild() throws FileNotFoundException, IOException {
		projSettings.addNewWebHook("1234", "http://text/test", true, BuildState.ALL_ENABLED, "JSON");
		when(webhook.getEventListBitMask()).thenReturn(BuildState.ALL_ENABLED);
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.buildFinished(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}
	
	@Test
	public void testBuildFinishedSRunningBuildSuccessAfterFailure() throws FileNotFoundException, IOException {
		projSettings.addNewWebHook("1234", "http://text/test", true, BuildState.BUILD_FIXED, "JSON");
		when(webhook.getEventListBitMask()).thenReturn(BuildState.BUILD_FIXED);
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedFailedBuilds);
		
		whl.buildFinished(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}
	
	@Test
	public void testBuildFinishedSRunningBuildSuccessAfterSuccess() throws FileNotFoundException, IOException {
		projSettings.addNewWebHook("1234", "http://text/test", true, BuildState.BUILD_FIXED, "JSON");
		when(webhook.getEventListBitMask()).thenReturn(BuildState.BUILD_FIXED);
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.buildFinished(sRunningBuild);
		verify(factory.getWebHook(), times(0)).post();
	}

	@Test
	public void testBuildInterruptedSRunningBuild() throws FileNotFoundException, IOException {
		projSettings.addNewWebHook("1234", "http://text/test", true, BuildState.ALL_ENABLED, "JSON");
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.buildInterrupted(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}

	@Test
	public void testBeforeBuildFinishSRunningBuild() throws FileNotFoundException, IOException {
		projSettings.addNewWebHook("1234", "http://text/test", true, BuildState.BEFORE_BUILD_FINISHED, "JSON");
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.beforeBuildFinish(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}

	@Test
	public void testBuildChangedStatusSRunningBuildStatusStatus() throws FileNotFoundException, IOException {
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running");
		
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", sBuildType);
		sBuildType.setProject(sProject);
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager, factory);
		Status oldStatus = Status.NORMAL;
		Status newStatus = Status.FAILURE;
		whl.register();
		whl.buildChangedStatus(sRunningBuild, oldStatus, newStatus);
		verify(factory.getWebHook(), times(0)).post();
	}

//	@Test
//	public void testResponsibleChangedSBuildTypeResponsibilityInfoResponsibilityInfoBoolean() {
//		
//	}

}
