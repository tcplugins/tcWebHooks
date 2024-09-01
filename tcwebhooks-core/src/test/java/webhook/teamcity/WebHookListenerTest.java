package webhook.teamcity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import webhook.TestingWebHookFactory;
import webhook.WebHook;
import webhook.WebHookProxyConfig;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.executor.WebHookExecutor;
import webhook.teamcity.executor.WebHookRunnerFactory;
import webhook.teamcity.executor.WebHookSerialExecutorImpl;
import webhook.teamcity.executor.WebHookStatisticsExecutor;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebAddressTransformerImpl;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryItemFactoryImpl;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.history.WebHookHistoryRepositoryImpl;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.project.WebHookParameterStore;

public class WebHookListenerTest {
	SBuildServer sBuildServer = mock(SBuildServer.class);
	BuildHistory buildHistory = mock(BuildHistory.class);
	ProjectManager projectManager = mock(ProjectManager.class);
	WebHookSettingsManager settings = mock(WebHookSettingsManager.class);
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookPayloadManager manager = mock(WebHookPayloadManager.class);
	WebHookTemplateManager templateManager = mock(WebHookTemplateManager.class);
	WebHookTemplateResolver templateResolver = mock(WebHookTemplateResolver.class);
	WebHookHistoryRepository historyRepository = new WebHookHistoryRepositoryImpl();
	WebAddressTransformer webAddressTransformer = new WebAddressTransformerImpl();
	WebHookHistoryItemFactory historyItemFactory = new WebHookHistoryItemFactoryImpl(webAddressTransformer, projectManager);
	WebHookContentBuilder contentBuilder;
	WebHookRunnerFactory webHookRunnerFactory = new WebHookRunnerFactory(contentBuilder, historyRepository, historyItemFactory);
	WebHookExecutor webHookExecutor = new WebHookSerialExecutorImpl(webHookRunnerFactory);
	WebHookStatisticsExecutor webHookStatisticsExecutor = new WebHookSerialExecutorImpl(webHookRunnerFactory);
	WebHookAuthenticatorProvider authenticatorProvider = mock(WebHookAuthenticatorProvider.class);
	WebHookVariableResolverManager resolverManager = new WebHookVariableResolverManagerImpl();
	VariableResolverFactory variableResolverFactory = new WebHooksBeanUtilsVariableResolverFactory();
	WebHookPayload payload = new WebHookPayloadJson(manager, resolverManager);
	WebHookProjectSettings projSettings;
	WebHookFactory factory = mock(WebHookFactory.class);
	WebHook webhook = mock (WebHook.class);
	WebHook webHookImpl;
	WebHook spyWebHook;
	WebHookConfig webHookConfig;
	SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	SFinishedBuild previousFailedBuild = mock(SFinishedBuild.class);
	List<SFinishedBuild> finishedSuccessfulBuilds = new ArrayList<>();
	List<SFinishedBuild> finishedFailedBuilds = new ArrayList<>();
	MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
	WebHookListener whl;
	BuildState allBuildStates = new BuildState();
	
	WebHookParameterStore webHookParameterStore = mock(WebHookParameterStore.class);
	WebHookSettingsEventHandler settingsEventHandler = mock(WebHookSettingsEventHandler.class);

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		resolverManager.registerVariableResolverFactory(variableResolverFactory);
		webHookImpl = new TestingWebHookFactory().getWebHook();
		allBuildStates.allEnabled();
		webHookImpl.setBuildStates(allBuildStates);
		spyWebHook = spy(webHookImpl);   
		webHookConfig = mock(WebHookConfig.class);
		when(webHookParameterStore.getAllWebHookParameters(sProject)).thenReturn(Collections.emptyList());
		contentBuilder = new WebHookContentBuilder(sBuildServer, templateResolver, resolverManager, webHookParameterStore);
		whl = new WebHookListener(sBuildServer, settings, configSettings, templateManager, factory, webHookExecutor, webHookStatisticsExecutor, settingsEventHandler);
		projSettings = new WebHookProjectSettings();
		when(factory.getWebHook(any(WebHookConfig.class), any(WebHookProxyConfig.class))).thenReturn(webHookImpl);
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
		when(configSettings.getProxyConfigForUrl("http://text/test")).thenReturn(null);
		finishedSuccessfulBuilds.add(previousSuccessfulBuild);
		finishedFailedBuilds.add(previousFailedBuild);
		sBuildType.setProject(sProject);
		when(settings.getSettings(sRunningBuild.getProjectId())).thenReturn(projSettings);
		whl.register();
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unused")
	@Test
	public void testWebHookListener() {
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, templateManager, factory, webHookExecutor, webHookStatisticsExecutor, settingsEventHandler);
	}

	@Test
	public void testRegister() {
		WebHookListener whl = new WebHookListener(sBuildServer, settings,configSettings, templateManager, factory, webHookExecutor, webHookStatisticsExecutor, settingsEventHandler);
		whl.register();
		verify(sBuildServer).addListener(whl);
	}

}
