package webhook.testframework;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.jdom.JDOMException;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.TestingWebHookFactory;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.WebHookFactoryImpl;
import webhook.teamcity.WebHookHttpClientFactoryImpl;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.auth.AbstractWebHookAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.auth.bearer.BearerAuthenticatorFactory;
import webhook.teamcity.executor.WebHookExecutor;
import webhook.teamcity.executor.WebHookRunnerFactory;
import webhook.teamcity.executor.WebHookSerialExecutorImpl;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebAddressTransformerImpl;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryItemFactoryImpl;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.history.WebHookHistoryRepositoryImpl;
import webhook.teamcity.payload.PayloadTemplateEngineType;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;
import webhook.teamcity.payload.format.WebHookPayloadXml;
import webhook.teamcity.payload.template.LegacyJsonWebHookTemplate;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsLegacyVariableResolverFactory;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.testframework.util.ConfigLoaderUtil;

public class WebHookMockingFrameworkImpl implements WebHookMockingFramework {
	
	WebHookPayloadContent content;
	WebHookConfig webHookConfig;
	SBuildServer sBuildServer = mock(SBuildServer.class);
	BuildHistory buildHistory = mock(BuildHistory.class);
	WebHookSettingsManager settings = mock(WebHookSettingsManager.class);
	ProjectManager projectManager = mock(ProjectManager.class);
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookPayloadManager manager = mock(WebHookPayloadManager.class);
	WebHookTemplateResolver resolver = mock(WebHookTemplateResolver.class);
	WebHookTemplateManager templateManager = mock(WebHookTemplateManager.class);
	WebHookVariableResolverManager webHookVariableResolverManager = mock(WebHookVariableResolverManager.class);
	VariableResolverFactory variableResolverFactory = new WebHooksBeanUtilsVariableResolverFactory();
	VariableResolverFactory legacyVariableResolverFactory = new WebHooksBeanUtilsLegacyVariableResolverFactory();
	 
	WebHookContentBuilder contentBuilder = new WebHookContentBuilder(sBuildServer, resolver, webHookVariableResolverManager);
	WebHookPayloadTemplate template;
	WebHookPayload payloadJson = new WebHookPayloadJson(manager, webHookVariableResolverManager);
	WebHookPayload payloadXml = new WebHookPayloadXml(manager, webHookVariableResolverManager);
	WebHookPayload payloadNvpairs = new WebHookPayloadNameValuePairs(manager, webHookVariableResolverManager);
	WebHookPayload payloadJsonTemplate = new WebHookPayloadJsonTemplate(manager, webHookVariableResolverManager);
	
	WebHookPayloadTemplate templateJson = new LegacyJsonWebHookTemplate(templateManager);
	
	WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
	WebHookPayload payload = new WebHookPayloadJson(manager, webHookVariableResolverManager);
	WebHookProjectSettings projSettings;
	//WebHookFactory factory = mock(WebHookFactory.class);
	WebHookFactory factory = new WebHookFactoryImpl(configSettings, authenticatorProvider, new WebHookHttpClientFactoryImpl());
	WebHook webhook = mock (WebHook.class);
	WebHook webHookImpl;
	WebHook spyWebHook;
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
	
	UsernamePasswordAuthenticatorFactory basicAuthAuthenticatorFactory = new UsernamePasswordAuthenticatorFactory(authenticatorProvider);
	AbstractWebHookAuthenticatorFactory bearerAuthenticatorFactory = new BearerAuthenticatorFactory(authenticatorProvider);
	
	
	SBuildType build2 = mock(SBuildType.class);
	SBuildType build3 = mock(SBuildType.class);
	
	WebHookListener whl;
	SortedMap<String, String> extraParameters;
	SortedMap<String, String> teamcityProperties;
	BuildStateEnum buildstateEnum;
	List<WebHookPayloadTemplate> templateList = new ArrayList<>();
	List<WebHookPayload> formatList = new ArrayList<>();
	private WebHookHistoryRepository historyRepository  = new WebHookHistoryRepositoryImpl();
	private WebAddressTransformer webAddressTransformer = new WebAddressTransformerImpl();
	private WebHookHistoryItemFactory historyItemFactory = new WebHookHistoryItemFactoryImpl(webAddressTransformer, projectManager);
	private WebHookRunnerFactory webHookRunnerFactory = new WebHookRunnerFactory(contentBuilder, historyRepository, historyItemFactory);
	private WebHookExecutor webHookExecutor = new WebHookSerialExecutorImpl(webHookRunnerFactory);

	
	private WebHookMockingFrameworkImpl() {
		webHookImpl = new TestingWebHookFactory().getWebHook();
		spyWebHook = spy(webHookImpl);   
		whl = new WebHookListener(sBuildServer, settings, configSettings, templateManager, factory, resolver, contentBuilder, historyRepository, historyItemFactory, webHookExecutor);
		projSettings = new WebHookProjectSettings();
//		when(factory.getWebHook(webHookConfig,null)).thenReturn(webHookImpl);
//		when(factory.getWebHook()).thenReturn(webHookImpl);
//		when(factory.getWebHook(any(WebHookConfig.class), any(WebHookProxyConfig.class))).thenReturn(webHookImpl);
		when(settings.getTemplateUsageCount((String)any())).thenReturn(0);
		when(webHookVariableResolverManager.getVariableResolverFactory(PayloadTemplateEngineType.STANDARD)).thenReturn(variableResolverFactory);
		when(webHookVariableResolverManager.getVariableResolverFactory(PayloadTemplateEngineType.LEGACY)).thenReturn(legacyVariableResolverFactory);
		when(manager.isRegisteredFormat("nvpairs")).thenReturn(true);
		when(manager.getFormat("nvpairs")).thenReturn(payloadNvpairs);
		when(manager.isRegisteredFormat("json")).thenReturn(true);
		when(manager.getFormat("json")).thenReturn(payloadJson);
		//when(factory.getWebHook()).thenReturn(spyWebHook);
//		when(factory.getWebHook()).thenReturn(webHookImpl);
		when(manager.isRegisteredFormat("JSON")).thenReturn(true);
		when(manager.getFormat("JSON")).thenReturn(payloadJson);
		when(manager.getServer()).thenReturn(sBuildServer);
		
		when(templateManager.isRegisteredTemplate("legacy-json")).thenReturn(true);
		when(templateManager.getTemplate("legacy-json")).thenReturn(templateJson);
		when(resolver.getTemplatePayloadFormat("legacy-json")).thenReturn(payloadJson);
		
		formatList.add(payloadJson);
		formatList.add(payloadXml);
		formatList.add(payloadNvpairs);
		formatList.add(payloadJsonTemplate);
		when(manager.getRegisteredFormats()).thenReturn(formatList);
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
		when(settings.getSettings(sRunningBuild.getProjectId())).thenReturn(projSettings);
		
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
		whl.register();
		basicAuthAuthenticatorFactory.register();
		bearerAuthenticatorFactory.register();
		template = getTestingTemplate(); 
		templateList.add(template);
		when(templateManager.getRegisteredTemplates()).thenReturn(templateList);
		when(resolver.findWebHookTemplatesForProject(sProject)).thenReturn(templateList);
		
		finishedBuildsHistory.addAll(finishedSuccessfulBuilds);
		finishedBuildsHistory.addAll(finishedFailedBuilds);

		((MockSBuildType) sBuildType).setMockingFrameworkInstance(this);
		
	}
	
	@Override
	public List<SFinishedBuild> getMockedBuildHistory(){

		return finishedBuildsHistory;
	}

	private WebHookPayloadTemplate getTestingTemplate() {
		return new WebHookPayloadTemplate() {
			@SuppressWarnings("unused")
			WebHookTemplateManager manager = null;
			BuildStateEnum[] supportedStates = {BuildStateEnum.BUILD_SUCCESSFUL, BuildStateEnum.BUILD_FAILED, BuildStateEnum.BUILD_BROKEN, BuildStateEnum.BUILD_FIXED};
			
			@Override
			public boolean supportsPayloadFormat(String payloadFormat) {
				return true;
			}
			
			@Override
			public void setTemplateManager(WebHookTemplateManager webhookTemplateManager) {
				this.manager = webhookTemplateManager;				
			}
			
			@Override
			public void setRank(Integer rank) {
			}
			
			@Override
			public void register() {
				//this.manager.register();
			}
			
			@Override
			public String getTemplateToolTip() {
				return "Test Tool Tip";
			}
			
			@Override
			public String getTemplateId() {
				return "mockedJsonTemplate";
			}
			
			@Override
			public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
				return WebHookTemplateContent.create(buildState.getShortName(), "Template for " + buildState.getShortName(), true, "");
			}
			
			@Override
			public WebHookTemplateContent getBranchTemplateForState(
					BuildStateEnum buildState) {
				return WebHookTemplateContent.create(buildState.getShortName(), "Branch template for " + buildState.getShortName(), true, "");			}
			
			@Override
			public String getTemplateDescription() {
				return "A long template description";
			}
			
			@Override
			public Set<BuildStateEnum> getSupportedBuildStates() {
				Set<BuildStateEnum> states = new HashSet<>();
				for (BuildStateEnum state: supportedStates){
					states.add(state);
				}
				return states;
			}
			
			@Override
			public Set<BuildStateEnum> getSupportedBranchBuildStates() {
				Set<BuildStateEnum> states = new HashSet<>();
				for (BuildStateEnum state: supportedStates){
					states.add(state);
				}
				return states;
			}
			
			@Override
			public int getRank() {
				return 1;
			}

			@Override
			public String getPreferredDateTimeFormat() {
				return "";
			}

			@Override
			public WebHookTemplateEntity getAsEntity() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public WebHookTemplateConfig getAsConfig() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	public static WebHookMockingFramework create(BuildStateEnum buildState, ExtraParametersMap extraParameters, ExtraParametersMap teamcityProperties) {
		WebHookMockingFrameworkImpl framework = new WebHookMockingFrameworkImpl();
		framework.buildstateEnum = buildState;
		framework.extraParameters = extraParameters;
		framework.teamcityProperties = teamcityProperties;
		framework.content = new WebHookPayloadContent(framework.variableResolverFactory, framework.sBuildServer, framework.sRunningBuild, framework.previousSuccessfulBuild, buildState, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
		return framework;
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
	public WebHookPayloadContent getWebHookContent() {
		return content;
	}

	@Override
	public void loadWebHookConfigXml(File xmlConfigFile) throws JDOMException, IOException {
		webHookConfig = ConfigLoaderUtil.getFirstWebHookInConfig(xmlConfigFile);
		this.content = new WebHookPayloadContent(this.variableResolverFactory, this.sBuildServer, this.sRunningBuild, this.previousSuccessfulBuild, this.buildstateEnum, extraParameters, teamcityProperties, webHookConfig.getEnabledTemplates());
	}
	
	@Override
	public void loadNthWebHookConfigXml(int itemNumber, File xmlConfigFile) throws JDOMException, IOException {
		webHookConfig = ConfigLoaderUtil.getSpecificWebHookInConfig(itemNumber, xmlConfigFile);
		this.content = new WebHookPayloadContent(this.variableResolverFactory, this.sBuildServer, this.sRunningBuild, this.previousSuccessfulBuild, this.buildstateEnum, extraParameters, teamcityProperties, webHookConfig.getEnabledTemplates());
	}
	
	@Override
	public void loadWebHookProjectSettingsFromConfigXml(File xmlConfigFile) throws IOException, JDOMException{
		projSettings.readFrom(ConfigLoaderUtil.getFullConfigElement(xmlConfigFile).getChild("webhooks"));
	}
	
	@Override
	public WebHookConfig getWebHookConfig() {
		return webHookConfig;
	}

	@Override
	public WebHookProjectSettings getWebHookProjectSettings() {
		return projSettings;
	}

	@Override
	public WebHookPayloadManager getWebHookPayloadManager() {
		return manager;
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
	public WebHookTemplateManager getWebHookTemplateManager() {
		return templateManager;
	}

	@Override
	public WebHookTemplateResolver getWebHookTemplateResolver() {
		return resolver;
	}
	
	@Override
	public WebHookListener getWebHookListener() {
		return whl;
	}

	@Override
	public WebHookAuthenticatorProvider getWebHookAuthenticatorProvider() {
		return authenticatorProvider;
	}

	@Override
	public SFinishedBuild getPreviousFailedBuild() {
		return this.previousFailedBuild;
	}

	@Override
	public SFinishedBuild getPreviousSuccessfulBuild() {
		return this.previousSuccessfulBuild;
	}

	@Override
	public WebHookVariableResolverManager getWebHookVariableResolverManager() {
		return this.webHookVariableResolverManager;
	}

	@Override
	public WebHookSettingsManager getWebHookSettingsManager() {
		return this.settings;
	}

}
