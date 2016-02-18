package webhook.teamcity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookListenerTest {
	SBuildServer sBuildServer = mock(SBuildServer.class);
	BuildHistory buildHistory = mock(BuildHistory.class);
	ProjectManager projectManager = mock(ProjectManager.class);
	ProjectSettingsManager settings = mock(ProjectSettingsManager.class);
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookPayloadManager manager = mock(WebHookPayloadManager.class);
	WebHookAuthenticatorProvider authenticatorProvider = mock(WebHookAuthenticatorProvider.class);
	WebHookPayload payload = new WebHookPayloadJson(manager);
	WebHookProjectSettings projSettings;
	WebHookFactory factory = mock(WebHookFactory.class);
	WebHook webhook = mock (WebHook.class);
	WebHook webHookImpl;
	WebHook spyWebHook;
	SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	SFinishedBuild previousFailedBuild = mock(SFinishedBuild.class);
	List<SFinishedBuild> finishedSuccessfulBuilds = new ArrayList<SFinishedBuild>();
	List<SFinishedBuild> finishedFailedBuilds = new ArrayList<SFinishedBuild>();
	MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
	WebHookListener whl;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		webHookImpl = new WebHookImpl();
		spyWebHook = spy(webHookImpl);   
		whl = new WebHookListener(sBuildServer, settings, configSettings, manager, factory, authenticatorProvider);
		projSettings = new WebHookProjectSettings();
		when(factory.getWebHook()).thenReturn(spyWebHook);
		when(manager.isRegisteredFormat("JSON")).thenReturn(true);
		when(manager.getFormat("JSON")).thenReturn(payload);
		when(manager.getServer()).thenReturn(sBuildServer);
		when(sBuildServer.getProjectManager()).thenReturn(projectManager);
		when(projectManager.findProjectById("project1")).thenReturn(sProject);
		when(sBuildServer.getHistory()).thenReturn(buildHistory);
		when(sBuildServer.getRootUrl()).thenReturn("http://test.server");
		when(previousSuccessfulBuild.getBuildStatus()).thenReturn(Status.NORMAL);
		when(previousSuccessfulBuild.isPersonal()).thenReturn(false);
		when(previousFailedBuild.getBuildStatus()).thenReturn(Status.FAILURE);
		when(previousFailedBuild.isPersonal()).thenReturn(false);
		finishedSuccessfulBuilds.add(previousSuccessfulBuild);
		finishedFailedBuilds.add(previousFailedBuild);
		sBuildType.setProject(sProject);
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		whl.register();
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unused")
	@Test
	public void testWebHookListener() {
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager, factory, authenticatorProvider);
	}

	@Test
	public void testRegister() {
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager, factory, authenticatorProvider);
		whl.register();
		verify(sBuildServer).addListener(whl);
	}

//	@Test
//	public void testGetFromConfig() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testBuildStartedSRunningBuild() throws FileNotFoundException, IOException {
		BuildState state = new BuildState().setAllEnabled();
		projSettings.addNewWebHook("project1", "http://text/test", true, state, "JSON", true, true, new HashSet<String>());
		when(webhook.isEnabled()).thenReturn(state.allEnabled());
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.buildStarted(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}

	@Test
	public void testBuildFinishedSRunningBuild() throws FileNotFoundException, IOException {
		BuildState state = new BuildState().setAllEnabled();
		projSettings.addNewWebHook("1234", "http://text/test", true, state , "JSON", true, true, new HashSet<String>());
		when(webhook.isEnabled()).thenReturn(state.allEnabled());
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.buildFinished(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}
	
	@Test
	public void testBuildFinishedSRunningBuildSuccessAfterFailure() throws FileNotFoundException, IOException {
		BuildState state = new BuildState();
		state.enable(BuildStateEnum.BUILD_FIXED);
		state.enable(BuildStateEnum.BUILD_FINISHED);
		state.enable(BuildStateEnum.BUILD_SUCCESSFUL);
		projSettings.addNewWebHook("1234", "http://text/test", true, state, "JSON", true, true, new HashSet<String>());
		when(webhook.isEnabled()).thenReturn(state.enabled(BuildStateEnum.BUILD_FIXED));
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedFailedBuilds);
		
		whl.buildFinished(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}
	
	@Test
	public void testBuildFinishedSRunningBuildSuccessAfterSuccess() throws FileNotFoundException, IOException {
		BuildState state = new BuildState();
		state.enable(BuildStateEnum.BUILD_FIXED);
		projSettings.addNewWebHook("1234", "http://text/test", true, state, "JSON", true, true, new HashSet<String>());
		when(webhook.isEnabled()).thenReturn(state.enabled(BuildStateEnum.BUILD_FIXED));
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.buildFinished(sRunningBuild);
		verify(factory.getWebHook(), times(0)).post();
	}

	@Test
	public void testBuildInterruptedSRunningBuild() throws FileNotFoundException, IOException {
		BuildState state = new BuildState().setAllEnabled();
		projSettings.addNewWebHook("1234", "http://text/test", true, state, "JSON", true, true, new HashSet<String>());
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.buildInterrupted(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}

	@Test
	public void testBeforeBuildFinishSRunningBuild() throws FileNotFoundException, IOException {
		BuildState state = new BuildState();
		state.enable(BuildStateEnum.BEFORE_BUILD_FINISHED);
		projSettings.addNewWebHook("1234", "http://text/test", true, state, "JSON", true, true, new HashSet<String>());
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		
		whl.beforeBuildFinish(sRunningBuild);
		verify(factory.getWebHook(), times(1)).post();
	}

	@Test
	public void testBuildChangedStatusSRunningBuildStatusStatus() throws FileNotFoundException, IOException {
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		sBuildType.setProject(sProject);
		String triggeredBy = "SubVersion";
		MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
		
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, manager, factory, authenticatorProvider);
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
