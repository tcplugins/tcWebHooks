package webhook.teamcity.payload.template;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.mockito.Mockito;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.WebHookFactoryImpl;
import webhook.teamcity.WebHookHttpClientFactory;
import webhook.teamcity.WebHookHttpClientFactoryImpl;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.standard.WebHooksBeanUtilsVariableResolverFactory;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplateJaxTestHelper;
import webhook.teamcity.settings.project.WebHookParameterStore;

public abstract class AbstractSpringTemplateTest {
	
	protected WebHookContentBuilder webHookContentBuilder;
	protected WebHookTemplateManager templateManager;
	protected WebHookPayloadManager payloadManager;
	protected WebHookTemplateJaxHelper webHookTemplateJaxHelper;
	protected WebHookFactory webHookFactory;
	protected MockSRunningBuild sRunningBuild;
	protected ProjectIdResolver projectIdResolver;
	
	@Before
	public void setup() {
		
		WebHookParameterStore webHookParameterStore = mock(WebHookParameterStore.class);
		projectIdResolver = mock(ProjectIdResolver.class);
		when(projectIdResolver.getExternalProjectId(Mockito.eq("project1"))).thenReturn("ATestProject");
		when(projectIdResolver.getInternalProjectId(Mockito.eq("ATestProject"))).thenReturn("project1");
		
		when(projectIdResolver.getExternalProjectId(Mockito.eq("_Root"))).thenReturn("_Root");
		when(projectIdResolver.getInternalProjectId(Mockito.eq("_Root"))).thenReturn("_Root");
		when(webHookParameterStore.getAllWebHookParameters(any())).thenReturn(Collections.emptyList());
		
		SBuildServer sBuildServer = mock(SBuildServer.class);
		WebHookMainSettings mainSettings = mock(WebHookMainSettings.class);
		webHookTemplateJaxHelper = new WebHookTemplateJaxTestHelper();
		WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
		payloadManager = new WebHookPayloadManager(sBuildServer);
		templateManager = new WebHookTemplateManager(payloadManager, webHookTemplateJaxHelper, projectIdResolver);
		WebHookHttpClientFactory clientFactory = new WebHookHttpClientFactoryImpl();
		
		WebHookVariableResolverManager variableResolverManager = new WebHookVariableResolverManagerImpl();
		variableResolverManager.registerVariableResolverFactory(new WebHooksBeanUtilsVariableResolverFactory());
		
		WebHookPayloadJsonTemplate webHookPayloadJsonTemplate = new WebHookPayloadJsonTemplate(payloadManager, variableResolverManager);
		webHookPayloadJsonTemplate.register();
		WebHookTemplateResolver templateResolver = new WebHookTemplateResolver(templateManager, payloadManager);
		webHookContentBuilder = new WebHookContentBuilder(sBuildServer, templateResolver, variableResolverManager, webHookParameterStore);
		
		when(mainSettings.getProxyConfigForUrl(getUrl())).thenReturn(null);
		
		webHookFactory = new WebHookFactoryImpl(mainSettings, authenticatorProvider, clientFactory);
		
		MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
		String triggeredBy = "SubVersion";
		sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
		SFinishedBuild previousBuild = mock(SFinishedBuild.class);
		when (previousBuild.getFinishDate()).thenReturn(new Date());
		List<SFinishedBuild> finishedBuilds = new ArrayList<>();
		finishedBuilds.add(previousBuild);
		BuildHistory buildHistory = mock(BuildHistory.class);
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedBuilds);
		when(sBuildServer.getRootUrl()).thenReturn("http://my-server/");
		when (sBuildServer.getHistory()).thenReturn(buildHistory);
		MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
		sBuildType.setProject(sProject);
		
		WebHookPayloadTemplate template = getTemplateInstance();
		template.register();
		
	}

	public abstract WebHookPayloadTemplate getTemplateInstance();
	public abstract String getUrl();

}
