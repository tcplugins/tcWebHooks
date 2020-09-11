package webhook.teamcity.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.jdom.JDOMException;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.WebHookExecutionStats;
import webhook.WebHookTestServer;
import webhook.WebHookTestServerTestBase;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.TestingWebHookHttpClientFactoryImpl;
import webhook.teamcity.TestingWebHookHttpClientFactoryImpl.TestableHttpClient;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.WebHookFactoryImpl;
import webhook.teamcity.WebHookHttpClientFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.basic.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebAddressTransformerImpl;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.payload.template.SlackComCompactXmlWebHookTemplate;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateBranchText;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateText;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxTestHelper;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookExecutionRequestGsonBuilder;
import webhook.teamcity.testing.model.WebHookRenderResult;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class WebHookUserRequestedExecutorImplTest extends WebHookTestServerTestBase {
	
	
	private SBuildServer server = mock(SBuildServer.class);
	private WebHookParameterStore webHookParameterStore = mock(WebHookParameterStore.class);
	private WebHookParameterStoreFactory webHookParameterStoreFactory = mock(WebHookParameterStoreFactory.class);
	private ProjectIdResolver projectIdResolver = mock(ProjectIdResolver.class);
	private WebHookSettingsManager projectSettingsManager = mock(WebHookSettingsManager.class);
	
	private WebHookPayloadManager webHookPayloadManager = new WebHookPayloadManager(server);
	private WebHookTemplateJaxTestHelper webHookTemplateJaxTestHelper = new WebHookTemplateJaxTestHelper();
	private WebHookTemplateManager webHookTemplateManager  = new WebHookTemplateManager(webHookPayloadManager, webHookTemplateJaxTestHelper, projectIdResolver);
	private WebHookTemplateResolver webHookTemplateResolver = new WebHookTemplateResolver(webHookTemplateManager, webHookPayloadManager);
	private WebHookConfigFactory webHookConfigFactory = new WebHookConfigFactoryImpl(server, projectSettingsManager, webHookTemplateManager);
	
	private WebHookVariableResolverManager variableResolverManager = new WebHookVariableResolverManagerImpl();
	
	
	private WebHookPayload jsonTemplate = new WebHookPayloadJsonTemplate(webHookPayloadManager, variableResolverManager);

	private WebHookMainSettings mainSettings = new WebHookMainSettings(server);
	private WebHookProjectSettings webHookProjectSettings;
	
	private TestableHttpClient httpClient = new TestableHttpClient();
	private WebHookHttpClientFactory webHookHttpClientFactory = new TestingWebHookHttpClientFactoryImpl(httpClient);
	
	private WebHookAuthenticatorProvider webHookAuthenticatorProvider = new WebHookAuthenticatorProvider();

	private WebHookFactory webHookFactory = new WebHookFactoryImpl(mainSettings, webHookAuthenticatorProvider, webHookHttpClientFactory);
	private WebHookContentBuilder webHookContentBuilder = new WebHookContentBuilder(server, webHookTemplateResolver, variableResolverManager, webHookParameterStore);
	
	private MockSBuildType buildType = new MockSBuildType("name", "description", "buildTypeId");
	private SProject sproject = new MockSProject("My Project", "description", "project01", "MyProject", buildType);
	private SRunningBuild runningBuild = new MockSRunningBuild(buildType, "triggeredBy", Status.NORMAL, "statusText", "buildNumber"); 
	
	private WebHookHistoryItemFactory webHookHistoryItemFactory;
	
	@Mock
	private WebHookHistoryRepository webHookHistoryRepository;
	
	@Mock
	private ParametersProvider parametersProvider;
	
	private final WebAddressTransformer webAddressTransformer = new WebAddressTransformerImpl();
	
	@Mock
	private ProjectManager projectManager;
	
	WebHookMockingFramework framework;
	
	Map<BuildStateEnum, Boolean> finishedBuildState = Collections.singletonMap(BuildStateEnum.BUILD_SUCCESSFUL, true);
	
	@Before
	public void setup() throws JAXBException, IOException, JDOMException {
		MockitoAnnotations.initMocks(this);
		variableResolverManager.registerVariableResolverFactory(new WebHooksBeanUtilsVariableResolverFactory());
		jsonTemplate.register();
		buildType.setProject(sproject);
		when(server.findBuildInstanceById(1)).thenReturn(runningBuild);
		when(server.findBuildInstanceById(2)).thenReturn(runningBuild);
		when(server.getProjectManager()).thenReturn(projectManager);
		when(server.getRootUrl()).thenReturn("http://teamcity");
		when(projectManager.findProjectByExternalId(eq("MyProject"))).thenReturn(sproject);
		when(projectManager.findProjectById(eq("project01"))).thenReturn(sproject);
		when(projectIdResolver.getExternalProjectId("_Root")).thenReturn("_Root");
		when(projectIdResolver.getInternalProjectId("_Root")).thenReturn("_Root");

		UsernamePasswordAuthenticatorFactory usernamePasswordAuthenticatorFactory = new UsernamePasswordAuthenticatorFactory(webHookAuthenticatorProvider);
		usernamePasswordAuthenticatorFactory.register();
		
		
		framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, new ExtraParameters(new HashMap<String,String>()));
		framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-slackcompact-jsonTemplate-AllEnabled.xml"));
		webHookProjectSettings = framework.getWebHookProjectSettings(); 

		when(projectSettingsManager.getSettings(anyString())).thenReturn(webHookProjectSettings);
		when(parametersProvider.getAll()).thenReturn(new TreeMap<String,String>());
		when(webHookParameterStore.getAllWebHookParameters(any())).thenReturn(Collections.emptyList());
		when(webHookParameterStoreFactory.getWebHookParameterStore()).thenReturn(webHookParameterStore);
		
		buildType.setParametersProvider(parametersProvider);
		
		WebHookTemplateEntity templateEntity = webHookTemplateJaxTestHelper.readTemplate("src/main/resources/webhook/teamcity/payload/template/SlackComCompactWebHookTemplate.xml");
		webHookTemplateManager.registerTemplateFormatFromXmlEntity(templateEntity);
		webHookHistoryItemFactory = new MockWebHookHistoryItemFactory(sproject);
	}

	@Test
	public void testRequestWebHookPreviewWebHookExecutionRequest() {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager,
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				webHookContentBuilder, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
			);
		
		WebHookExecutionRequest webHookExecutionRequest = WebHookExecutionRequest.builder()
				.buildId(1L)
				.uniqueKey("new")
				.projectExternalId("MyProject")
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				
				.url("http://localhost:12345/webhook")
				.templateId("slack.com-compact")
				.authEnabled(false)
				.configBuildStates(finishedBuildState)
				.build();
		
		WebHookRenderResult payload = executorImpl.requestWebHookPreview(webHookExecutionRequest);
		
		Loggers.SERVER.debug("################# " + payload);
		assertEquals(true, payload.getHtml().contains("http://teamcity/viewLog.html?buildTypeId=name"));
		
		Loggers.SERVER.debug(WebHookExecutionRequestGsonBuilder.gsonBuilder().toJson(webHookExecutionRequest));
		
	}
	
	@Test
	public void testRequestWebHookPreviewWebHookTemplateExecutionRequest() {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				null, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
			);
		
		BuildState finishedBuildState = new BuildState();
		finishedBuildState.setEnabled(BuildStateEnum.BUILD_SUCCESSFUL, true);
		WebHookConfig loadedConfig = webHookProjectSettings.getWebHooksConfigs().get(0);
		
		WebHookTemplateExecutionRequest webHookTemplateExecutionRequest = WebHookTemplateExecutionRequest.builder()
				.buildId(2L)
				.projectExternalId(sproject.getExternalId())
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				.uniqueKey(loadedConfig.getUniqueKey())
				.format("jsontemplate")
				.url("http://localhost:12345/webhook")
				.defaultBranchTemplate(new WebHookTemplateBranchText("{\n \"branch_buildId\" : \"${buildId}\" \n}"))
				.defaultTemplate(new WebHookTemplateText(false, "{ \"nonBranch_buildId\" : \"${buildId}\" }"))
				.build();
		WebHookRenderResult payload = executorImpl.requestWebHookPreview(webHookTemplateExecutionRequest);
		
		Loggers.SERVER.debug("################# " + payload);
		assertEquals(true, payload.getHtml().contains("branch_buildId"));

	}	
	
	@Test
	public void testRequestWebHookExecutionWebHookExecutionRequest() {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				webHookContentBuilder, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
			);
		
		WebHookExecutionRequest webHookExecutionRequest = WebHookExecutionRequest.builder()
				.buildId(1L)
				.uniqueKey("new")
				.projectExternalId("MyProject")
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				
				.url("http://localhost:12345/webhook")
				.templateId("slack.com-compact")
				.authEnabled(false)
				.configBuildStates(finishedBuildState)
				.build();
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookExecutionRequest);
		
		assertEquals("HttpClient should be invoked exactly once", 1, httpClient.getInvocationCount());
		assertEquals("Expect 801 since there is no server running on port 12345", 801, historyItem.getWebhookErrorStatus().getErrorCode());
		assertEquals(true, historyItem.getWebhookErrorStatus().getMessage().contains("Connection refused"));
	}
	
	@Test
	public void testRequestWebHookExecutionWebHookExecutionRequestReturns200() throws InterruptedException {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				webHookContentBuilder, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
				);
		
		WebHookExecutionRequest webHookExecutionRequest = WebHookExecutionRequest.builder()
				.buildId(1L)
				.uniqueKey("new")
				.projectExternalId("MyProject")
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				
				.url("http://localhost:58001/200")
				.templateId("slack.com-compact")
				.authEnabled(false)
				.configBuildStates(finishedBuildState)
				.build();
		
		WebHookTestServer s = startWebServer();
		
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookExecutionRequest);

		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		assertEquals("HttpClient should be invoked exactly once", 1, httpClient.getInvocationCount());
		assertEquals(false, historyItem.getWebHookExecutionStats().isErrored());
		
		stopWebServer(s);
	}
	
	@Test
	public void testRequestWebHookExecutionWebHookExecutionRequestForAddedToQueue() throws InterruptedException {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				webHookContentBuilder, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
				);
		
		WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxTestHelper, framework.getProjectIdResolver(), null);
		slackCompact.register();
		
		WebHookExecutionRequest webHookExecutionRequest = WebHookExecutionRequest.builder()
				.buildId(1L)
				.uniqueKey("new")
				.projectExternalId("MyProject")
				.testBuildState(BuildStateEnum.BUILD_ADDED_TO_QUEUE)
				
				.url("http://localhost:58001/200")
				.templateId("slack.com-compact")
				.authEnabled(false)
				.configBuildStates(finishedBuildState)
				.build();
		
		WebHookTestServer s = startWebServer();
		
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookExecutionRequest);
		
		assertTrue(s.getRequestBody().contains("Been Added To The Build Queue"));
		assertEquals("HttpClient should be invoked exactly once", 1, httpClient.getInvocationCount());
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		assertEquals(false, historyItem.getWebHookExecutionStats().isErrored());
		
		stopWebServer(s);
	}
	
	@Test
	public void testRequestWebHookExecutionWithOverridenConfigWebHookExecutionRequestReturns200() throws InterruptedException {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				webHookContentBuilder, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
				);
		
		WebHookConfig loadedConfig = webHookProjectSettings.getWebHooksConfigs().get(0);
		
		WebHookExecutionRequest webHookExecutionRequest = WebHookExecutionRequest.builder()
				.buildId(1L)
				.uniqueKey(loadedConfig.getUniqueKey())
				.projectExternalId("MyProject")
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				
				.url("http://localhost:58001/200")
				.templateId("slack.com-compact")
				.authEnabled(false)
				.configBuildStates(finishedBuildState)
				.build();
		
		WebHookTestServer s = startWebServer();
		
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookExecutionRequest);
		
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		assertEquals("HttpClient should be invoked exactly once", 1, httpClient.getInvocationCount());
		assertEquals(false, historyItem.getWebHookExecutionStats().isErrored());
		
		stopWebServer(s);
	}

	@SuppressWarnings("serial")
	@Test
	public void testRequestWebHookExecutionWebHookExecutionRequestWithAuthReturns200() throws InterruptedException {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				webHookContentBuilder, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
				);
		
		WebHookExecutionRequest webHookExecutionRequest = WebHookExecutionRequest.builder()
				.buildId(1L)
				.uniqueKey("new")
				.projectExternalId("MyProject")
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				
				.url("http://localhost:58001/auth/200")
				.templateId("slack.com-compact")
				.authEnabled(true)
				.authType("userpass")
				.authParameters(new HashMap<String,String>() {
					 {
						    put("username", "user1");
						    put("password", "user1pass");
						    put("realm",    "TestRealm");
						 }
						})
				.configBuildStates(finishedBuildState)
				.build();
		
		WebHookTestServer s = startWebServer();
		
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookExecutionRequest);
		
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		assertEquals("HttpClient should be invoked exactly once", 1, httpClient.getInvocationCount());
		assertEquals(false, historyItem.getWebHookExecutionStats().isErrored());
		
		stopWebServer(s);
	}
	
	@Test
	public void testRequestWebHookExecutionWebHookTemplateExecutionRequest() {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				null, 
				variableResolverManager, 
				projectIdResolver,
				webHookParameterStoreFactory
			);
		
		BuildState finishedBuildState = new BuildState();
		finishedBuildState.setEnabled(BuildStateEnum.BUILD_SUCCESSFUL, true);
		WebHookConfig loadedConfig = webHookProjectSettings.getWebHooksConfigs().get(0);
		
		WebHookTemplateExecutionRequest webHookTemplateExecutionRequest = WebHookTemplateExecutionRequest.builder()
				.buildId(2L)
				.projectExternalId(sproject.getExternalId())
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				.uniqueKey(loadedConfig.getUniqueKey())
				.format("jsontemplate")
				.url("http://localhost:12345/webhook")
				.defaultBranchTemplate(new WebHookTemplateBranchText("branch Text for build: ${buildId}"))
				.defaultTemplate(new WebHookTemplateText(false, "non-Branch text for build: ${buildId}"))
				.build();
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookTemplateExecutionRequest);
		
		assertEquals("HttpClient should be invoked exactly once", 1, httpClient.getInvocationCount());
		assertEquals("Expect 801 since there is no server running on port 12345", 801, historyItem.getWebhookErrorStatus().getErrorCode());
		Loggers.SERVER.debug("################# " + historyItem.getWebhookErrorStatus().getMessage());
		assertEquals(true, historyItem.getWebhookErrorStatus().getMessage().contains("Connection refused"));

	}
	
	@Test
	public void testRequestWebHookExecutionWebHookTemplateExecutionRequestForAddedToQueue() throws InterruptedException {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				null, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
				);
		
		BuildState addedToQueueBuildState = new BuildState();
		addedToQueueBuildState.setEnabled(BuildStateEnum.BUILD_ADDED_TO_QUEUE, true);
		WebHookConfig loadedConfig = webHookProjectSettings.getWebHooksConfigs().get(0);
		
		WebHookTemplateExecutionRequest webHookTemplateExecutionRequest = WebHookTemplateExecutionRequest.builder()
				.buildId(2L)
				.projectExternalId(sproject.getExternalId())
				.testBuildState(BuildStateEnum.BUILD_ADDED_TO_QUEUE)
				.uniqueKey(loadedConfig.getUniqueKey())
				.format("jsontemplate")
				.url("http://localhost:58001/200")
				.defaultBranchTemplate(new WebHookTemplateBranchText("branch Text for build: ${buildTypeId}"))
				.defaultTemplate(new WebHookTemplateText(false, "non-Branch text for build: ${buildTypeId}"))
				.build();
		
		WebHookTestServer s = startWebServer();
		
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookTemplateExecutionRequest);
		
		assertEquals("non-Branch text for build: name", s.getRequestBody());
		assertEquals("HttpClient should be invoked exactly once", 1, httpClient.getInvocationCount());
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		assertEquals(false, historyItem.getWebHookExecutionStats().isErrored());
		
		stopWebServer(s);
		
	}
	
	@Test
	public void testRequestWebHookExecutionWebHookTemplateExecutionRequestReturns200() throws InterruptedException {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				webHookContentBuilder, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
				);
		
		BuildState finishedBuildState = new BuildState();
		finishedBuildState.setEnabled(BuildStateEnum.BUILD_SUCCESSFUL, true);
		WebHookConfig loadedConfig = webHookProjectSettings.getWebHooksConfigs().get(0);
		
		WebHookTemplateExecutionRequest webHookTemplateExecutionRequest = WebHookTemplateExecutionRequest.builder()
				.buildId(2L)
				.projectExternalId(sproject.getExternalId())
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				.uniqueKey(loadedConfig.getUniqueKey())
				.format("jsontemplate")
				.url("http://localhost:58001/200")
				.defaultBranchTemplate(new WebHookTemplateBranchText("branch Text for build: ${buildId}"))
				.defaultTemplate(new WebHookTemplateText(false, "non-Branch text for build: ${buildId}"))
				.build();

		WebHookTestServer s = startWebServer();
		
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookTemplateExecutionRequest);

		assertEquals("branch Text for build: 123456", s.getRequestBody());
		assertEquals("Post should have returned 200 OK", HttpServletResponse.SC_OK, s.getReponseCode());
		assertEquals("HttpClient should be invoked exactly once", 1, httpClient.getInvocationCount());
		assertEquals(false, historyItem.getWebHookExecutionStats().isErrored());
		
		stopWebServer(s);
		
	}
	
	@Test
	public void testRequestWebHookExecutionWebHookTemplateExecutionRequestWithInvalidWebHook() {
		WebHookUserRequestedExecutor executorImpl = new WebHookUserRequestedExecutorImpl(
				server, mainSettings,
				webHookConfigFactory, 
				webHookFactory,
				webHookTemplateResolver, 
				webHookPayloadManager, 
				webHookHistoryItemFactory,
				webHookHistoryRepository,
				webAddressTransformer,
				null, 
				variableResolverManager,
				projectIdResolver,
				webHookParameterStoreFactory
				);
		
		BuildState finishedBuildState = new BuildState();
		finishedBuildState.setEnabled(BuildStateEnum.BUILD_SUCCESSFUL, true);
		
		WebHookTemplateExecutionRequest webHookTemplateExecutionRequest = WebHookTemplateExecutionRequest.builder()
				.buildId(2L)
				.projectExternalId(sproject.getExternalId())
				.testBuildState(BuildStateEnum.BUILD_SUCCESSFUL)
				.uniqueKey("12345")
				.format("jsontemplate")
				.defaultBranchTemplate(new WebHookTemplateBranchText("branch Text for build: ${buildId}"))
				.defaultTemplate(new WebHookTemplateText(false, "non-Branch text for build: ${buildId}"))
				.build();
		
		WebHookHistoryItem historyItem = executorImpl.requestWebHookExecution(webHookTemplateExecutionRequest);
		
		assertEquals(905, historyItem.getWebhookErrorStatus().getErrorCode());
	}
	
	@Override
	public String getHost() {
		return "localhost";
	}

	@Override
	public Integer getPort() {
		return 58001;
	}
	
	public static class MockWebHookHistoryItemFactory implements WebHookHistoryItemFactory {
		
		private final SProject sProject;
		private WebAddressTransformer webAddressTransformer = new WebAddressTransformerImpl();

		
		public MockWebHookHistoryItemFactory(SProject sproject) {
			this.sProject = sproject;
		}

		@Override
		public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, WebHookExecutionStats webHookExecutionStats,
				SBuild sBuild, WebHookErrorStatus errorStatus) {
			return null;
		}

		@Override
		public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, WebHookExecutionStats executionStats,
				SBuildType sBuildType, WebHookErrorStatus errorStatus) {
			return null;
		}

		@Override
		public WebHookHistoryItem getWebHookHistoryItem(WebHookConfig whc, WebHookExecutionStats executionStats,
				SProject project, WebHookErrorStatus errorStatus) {
			return null;
		}

		@Override
		public WebHookHistoryItem getWebHookHistoryTestItem(WebHookConfig whc,
				WebHookExecutionStats webHookExecutionStats, 
				SBuild sBuild, WebHookErrorStatus errorStatus) {
			try {
				return new WebHookHistoryItem(
						"prodjectId", sProject.getName(), 
						"buildTypeId", 
						"buildTypeName", "buildTypeExternalId", 1L, whc, 
						webHookExecutionStats, 
						errorStatus, new LocalDateTime(), 
						webAddressTransformer.getGeneralisedHostName(new URL(whc.getUrl())),
						true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public WebHookHistoryItem getWebHookHistoryTestItem(WebHookConfig whc, 
				WebHookExecutionStats webHookExecutionStats,
				SBuildType buildType, WebHookErrorStatus errorStatus) {
			try {
				return new WebHookHistoryItem(
						"prodjectId", sProject.getName(), 
						"buildTypeId", 
						"buildTypeName", "buildTypeExternalId", 1L, whc, 
						webHookExecutionStats, 
						errorStatus, new LocalDateTime(), 
						webAddressTransformer.getGeneralisedHostName(new URL(whc.getUrl())),
						true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public WebHookHistoryItem getWebHookHistoryTestItem(WebHookConfig whc, 
				WebHookExecutionStats webHookExecutionStats,
				SProject sProject, WebHookErrorStatus errorStatus) {
			try {
				return new WebHookHistoryItem(
						"prodjectId", sProject.getName(), 
						"buildTypeId", 
						"buildTypeName", "buildTypeExternalId", 1L, whc, 
						webHookExecutionStats, 
						errorStatus, new LocalDateTime(), 
						webAddressTransformer.getGeneralisedHostName(new URL(whc.getUrl())),
						true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
}