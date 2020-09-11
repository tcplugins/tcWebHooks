package webhook.testframework;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import org.jdom.JDOMException;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.executor.WebHookExecutor;
import webhook.teamcity.executor.WebHookRunnerFactory;
import webhook.teamcity.executor.WebHookSerialExecutorImpl;
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
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;
import webhook.teamcity.payload.format.WebHookPayloadTailoredJson;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplateJaxTestHelper;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.testframework.util.ConfigLoaderUtil;

public class WebHookSemiMockingFrameworkImpl implements WebHookMockingFramework {
	
	private WebHookSemiMockingFrameworkImpl() {}
	
	WebHookPayloadContent content;
	WebHookConfig webHookConfig;
	SBuildServer sBuildServer = mock(SBuildServer.class);
	BuildHistory buildHistory = mock(BuildHistory.class);
	WebHookSettingsManager projectSettingsManager = mock(WebHookSettingsManager.class);
	ProjectManager projectManager = mock(ProjectManager.class);
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookProjectSettings webHookProjectSettings = new WebHookProjectSettings();
	WebHookFactory webHookFactory = new MockingWebHookFactory();
	
	SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	SFinishedBuild previousFailedBuild = mock(SFinishedBuild.class);
	List<SFinishedBuild> finishedSuccessfulBuilds = new ArrayList<>();
	List<SFinishedBuild> finishedFailedBuilds = new ArrayList<>();
	List<SFinishedBuild> finishedBuildsHistory = new ArrayList<>();
	
	SBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	SBuildType sBuildType02 = new MockSBuildType("Test Build-2", "A Test Build 02", "bt2");
	SBuildType sBuildType03 = new MockSBuildType("Test Build-2", "A Test Build 03", "bt3");
	SRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	SProject sProject = new MockSProject("Test Project", "A test project", "project01", "ATestProject", sBuildType);
	SProject sProject02 = new MockSProject("Test Project 02", "A test project 02", "project2", "TestProjectNumber02", sBuildType);
	SProject sProject03 = new MockSProject("Test Project 03", "A test sub project 03", "project3", "TestProjectNumber02_TestProjectNumber03", sBuildType);
	
	
	SBuildType build2 = mock(SBuildType.class);
	SBuildType build3 = mock(SBuildType.class);
	
	ExtraParameters extraParameters;
	BuildStateEnum buildstateEnum;
	
	private WebHookPayloadManager webHookPayloadManager;
	private WebHookTemplateManager webHookTemplateManager;
	private WebHookContentBuilder webHookContentBuilder;
	private WebHookTemplateResolver webHookTemplateResolver;
	private WebHookListener webHookListener;
	private WebHookAuthenticatorProvider authenticatorProvider;
	private WebHookTemplateJaxHelper webHookTemplateJaxHelper;
	private WebHookHistoryRepository historyRepository;
	private WebAddressTransformer webAddressTransformer;
	private WebHookHistoryItemFactory historyItemFactory;
	private WebHookVariableResolverManager webHookVariableResolverManager;
	private WebHookRunnerFactory webHookRunnerFactory;
	private WebHookExecutor webHookExecutor;
	private ProjectIdResolver projectIdResolver = mock(ProjectIdResolver.class);
	
	private WebHookParameterStore webHookParameterStore = mock(WebHookParameterStore.class); 


	
	public static WebHookSemiMockingFrameworkImpl create(BuildStateEnum buildState, ExtraParameters extraParameters) {
		WebHookSemiMockingFrameworkImpl framework = new WebHookSemiMockingFrameworkImpl();
		framework.setup();
		framework.buildstateEnum = buildState;
		framework.extraParameters = extraParameters;
		
		return framework;
	}
	
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		when(projectIdResolver.getExternalProjectId(Mockito.eq("project1"))).thenReturn("ATestProject");
		when(projectIdResolver.getInternalProjectId(Mockito.eq("ATestProject"))).thenReturn("project1");
		
		when(projectIdResolver.getExternalProjectId(Mockito.eq("_Root"))).thenReturn("_Root");
		when(projectIdResolver.getInternalProjectId(Mockito.eq("_Root"))).thenReturn("_Root");
		
		webHookTemplateJaxHelper = new WebHookTemplateJaxTestHelper();
		webHookVariableResolverManager = new WebHookVariableResolverManagerImpl();
		VariableResolverFactory variableResolverFactory =  new WebHooksBeanUtilsVariableResolverFactory();
		webHookVariableResolverManager.registerVariableResolverFactory(variableResolverFactory);
		variableResolverFactory.setWebHookVariableResolverManager(webHookVariableResolverManager);
		webHookPayloadManager = setupPayloadManagerAndRegisterPayloadFormats();
		
		webHookTemplateManager  = new WebHookTemplateManager(webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver);
		webHookTemplateResolver = new WebHookTemplateResolver(webHookTemplateManager, webHookPayloadManager);
		webHookContentBuilder =  new WebHookContentBuilder(sBuildServer, webHookTemplateResolver, webHookVariableResolverManager, webHookParameterStore);
		
		authenticatorProvider = setupAuthenticatorProviderAndRegisterFactories();
		webAddressTransformer = new WebAddressTransformerImpl();
		historyItemFactory = new WebHookHistoryItemFactoryImpl(webAddressTransformer, projectManager);
		historyRepository = new WebHookHistoryRepositoryImpl();
		webHookRunnerFactory = new WebHookRunnerFactory(webHookContentBuilder, historyRepository, historyItemFactory);
		webHookExecutor = new WebHookSerialExecutorImpl(webHookRunnerFactory);
		
		webHookListener = new WebHookListener(sBuildServer, projectSettingsManager, configSettings, webHookTemplateManager, webHookFactory, webHookTemplateResolver, webHookContentBuilder, historyRepository, historyItemFactory, webHookExecutor);
		when(projectSettingsManager.getTemplateUsageCount((String)any())).thenReturn(0);
		when(projectManager.findProjectById("project01")).thenReturn(sProject);
		when(projectManager.findBuildTypeById("bt1")).thenReturn(sBuildType);
		when(sBuildServer.getHistory()).thenReturn(buildHistory);
		when(sBuildServer.getRootUrl()).thenReturn("http://test.server");
		when(sBuildServer.getProjectManager()).thenReturn(projectManager);
		when(previousSuccessfulBuild.getBuildStatus()).thenReturn(Status.NORMAL);
		when(previousSuccessfulBuild.isPersonal()).thenReturn(false);
		when(previousSuccessfulBuild.getFinishDate()).thenReturn(new Date());
		when(previousFailedBuild.getBuildStatus()).thenReturn(Status.FAILURE);
		when(previousFailedBuild.isPersonal()).thenReturn(false);
		when(previousFailedBuild.getFinishDate()).thenReturn(new Date());
		finishedSuccessfulBuilds.add(previousSuccessfulBuild);
		finishedFailedBuilds.add(previousFailedBuild);
		((MockSBuildType) sBuildType).setProject(sProject);
		when(projectSettingsManager.getSettings(sRunningBuild.getProjectId())).thenReturn(webHookProjectSettings);
		when(projectSettingsManager.getSettings(Mockito.eq("_Root"))).thenReturn(new WebHookProjectSettings());

		
		when(build2.getBuildTypeId()).thenReturn("bt2");
		when(build2.getInternalId()).thenReturn("bt2");
		when(build2.getName()).thenReturn("This is Build 2");
		when(build3.getBuildTypeId()).thenReturn("bt3");
		when(build3.getInternalId()).thenReturn("bt3");
		when(build3.getName()).thenReturn("This is Build 3");
		((MockSProject) sProject).addANewBuildTypeToTheMock(build2);
		((MockSProject) sProject).addANewBuildTypeToTheMock(build3);
		((MockSProject) sProject02).addANewBuildTypeToTheMock(sBuildType02);
		((MockSProject) sProject03).addANewBuildTypeToTheMock(sBuildType03);
		((MockSProject) sProject03).setParentProject(sProject02);
		((MockSProject) sProject02).addChildProjectToMock(sProject03);
		
		when(webHookParameterStore.getAllWebHookParameters(any())).thenReturn(Collections.emptyList());
	}
	
	private WebHookAuthenticatorProvider setupAuthenticatorProviderAndRegisterFactories() {
		WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
		UsernamePasswordAuthenticatorFactory authenticatorFactory =  new UsernamePasswordAuthenticatorFactory(authenticatorProvider);
		authenticatorFactory.register();
		return authenticatorProvider;
	}

	private WebHookPayloadManager setupPayloadManagerAndRegisterPayloadFormats() {
		WebHookPayloadManager webHookPayloadManager = new WebHookPayloadManager(sBuildServer);
		WebHookPayload nvPairsPayloadFormat = new WebHookPayloadNameValuePairs(webHookPayloadManager, webHookVariableResolverManager);
		nvPairsPayloadFormat.register();
		
		WebHookPayload jsonPayloadFormat = new WebHookPayloadJsonTemplate(webHookPayloadManager, webHookVariableResolverManager);
		
		jsonPayloadFormat.register();
		WebHookPayload tailoredJsonPayloadFormat = new WebHookPayloadTailoredJson(webHookPayloadManager, webHookVariableResolverManager);
		tailoredJsonPayloadFormat.register();
		
		WebHookPayload jsonTemplatePayloadFormat = new WebHookPayloadJsonTemplate(webHookPayloadManager, webHookVariableResolverManager);
		jsonTemplatePayloadFormat.register();
		return webHookPayloadManager;
	}

	@Override
	public SBuildServer getServer() {
		return sBuildServer;
	}

	@Override
	public SRunningBuild getRunningBuild() {
		return sRunningBuild;
	}

	@Override
	public SBuildType getSBuildType() {
		return sBuildType;
	}

	@Override
	public SBuildType getSBuildTypeFromSubProject() {
		return sBuildType03;
	}

	@Override
	public WebHookConfig getWebHookConfig() {
		return webHookConfig;
	}

	@Override
	public WebHookPayloadContent getWebHookContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebHookPayloadManager getWebHookPayloadManager() {
		return webHookPayloadManager;
	}

	@Override
	public WebHookProjectSettings getWebHookProjectSettings() {
		return webHookProjectSettings;
	}

	@Override
	public WebHookTemplateManager getWebHookTemplateManager() {
		return webHookTemplateManager;
	}

	@Override
	public WebHookTemplateResolver getWebHookTemplateResolver() {
		return webHookTemplateResolver;
	}

	@Override
	public WebHookAuthenticatorProvider getWebHookAuthenticatorProvider() {
		return authenticatorProvider;
	}

	@Override
	public WebHookListener getWebHookListener() {
		return webHookListener;
	}

	@Override
	public void loadWebHookConfigXml(File xmlConfigFile) throws JDOMException, IOException {
		webHookConfig = ConfigLoaderUtil.getFirstWebHookInConfig(xmlConfigFile);
	}

	@Override
	public void loadNthWebHookConfigXml(int itemNumber, File xmlConfigFile) throws JDOMException, IOException {
		webHookConfig = ConfigLoaderUtil.getSpecificWebHookInConfig(itemNumber, xmlConfigFile);
	}

	@Override
	public void loadWebHookProjectSettingsFromConfigXml(File xmlConfigFile) throws IOException, JDOMException {
		webHookProjectSettings.readFrom(ConfigLoaderUtil.getFullConfigElement(xmlConfigFile).getChild("webhooks"));
	}

	@Override
	public List<SFinishedBuild> getMockedBuildHistory() {
		return finishedBuildsHistory;
	}

	@Override
	public SFinishedBuild getPreviousFailedBuild() {
		return previousFailedBuild;
	}

	@Override
	public SFinishedBuild getPreviousSuccessfulBuild() {
		return previousSuccessfulBuild;
	}
	
	public MockingWebHookFactory getWebHookFactory() {
		return (MockingWebHookFactory) webHookFactory;
	}

	public WebHookTemplateJaxHelper getWebHookTemplateJaxHelper() {
		return webHookTemplateJaxHelper;
	}

	@Override
	public WebHookVariableResolverManager getWebHookVariableResolverManager() {
		return this.webHookVariableResolverManager;
	}

	@Override
	public WebHookSettingsManager getWebHookSettingsManager() {
		return this.projectSettingsManager;
	}

	public ProjectIdResolver getProjectIdResolver() {
		return this.projectIdResolver;
	}

}
