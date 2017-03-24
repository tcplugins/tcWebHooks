package webhook.testframework;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.auth.UsernamePasswordAuthenticatorFactory;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.testframework.util.ConfigLoaderUtil;

public class WebHookMockingFrameworkImpl implements WebHookMockingFramework {
	
	WebHookPayloadContent content;
	WebHookConfig webHookConfig;
	SBuildServer sBuildServer = mock(SBuildServer.class);
	BuildHistory buildHistory = mock(BuildHistory.class);
	ProjectSettingsManager settings = mock(ProjectSettingsManager.class);
	ProjectManager projectManager = mock(ProjectManager.class);
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookPayloadManager manager = mock(WebHookPayloadManager.class);
	WebHookAuthenticatorProvider authenticatorProvider = new WebHookAuthenticatorProvider();
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
	SBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	SBuildType sBuildType02 = new MockSBuildType("Test Build-2", "A Test Build 02", "bt2");
	SBuildType sBuildType03 = new MockSBuildType("Test Build-2", "A Test Build 03", "bt3");
	SRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	SProject sProject = new MockSProject("Test Project", "A test project", "project01", "ATestProject", sBuildType);
	SProject sProject02 = new MockSProject("Test Project 02", "A test project 02", "project2", "TestProjectNumber02", sBuildType);
	SProject sProject03 = new MockSProject("Test Project 03", "A test sub project 03", "project3", "TestProjectNumber02_TestProjectNumber03", sBuildType);
	
	UsernamePasswordAuthenticatorFactory authenticatorFactory = new UsernamePasswordAuthenticatorFactory(authenticatorProvider);
	
	SBuildType build2 = mock(SBuildType.class);
	SBuildType build3 = mock(SBuildType.class);
	
	WebHookListener whl;
	SortedMap<String, String> extraParameters;
	SortedMap<String, String> teamcityProperties;
	BuildStateEnum buildstateEnum;
	
	private WebHookMockingFrameworkImpl() {
		webHookImpl = new WebHookImpl();
		spyWebHook = spy(webHookImpl);   
		whl = new WebHookListener(sBuildServer, settings, configSettings, manager, factory, authenticatorProvider);
		projSettings = new WebHookProjectSettings();
		//when(factory.getWebHook()).thenReturn(spyWebHook);
		when(factory.getWebHook()).thenReturn(webHookImpl);
		when(manager.isRegisteredFormat("JSON")).thenReturn(true);
		when(manager.getFormat("JSON")).thenReturn(payload);
		when(manager.getServer()).thenReturn(sBuildServer);
		when(projectManager.findProjectById("project01")).thenReturn(sProject);
		when(sBuildServer.getHistory()).thenReturn(buildHistory);
		when(sBuildServer.getRootUrl()).thenReturn("http://test.server");
		when(sBuildServer.getProjectManager()).thenReturn(projectManager);
		when(previousSuccessfulBuild.getBuildStatus()).thenReturn(Status.NORMAL);
		when(previousSuccessfulBuild.isPersonal()).thenReturn(false);
		when(previousFailedBuild.getBuildStatus()).thenReturn(Status.FAILURE);
		when(previousFailedBuild.isPersonal()).thenReturn(false);
		finishedSuccessfulBuilds.add(previousSuccessfulBuild);
		finishedFailedBuilds.add(previousFailedBuild);
		((MockSBuildType) sBuildType).setProject(sProject);
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		
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
		authenticatorFactory.register();
	}

	public static WebHookMockingFramework create(BuildStateEnum buildState, ExtraParametersMap extraParameters, ExtraParametersMap teamcityProperties) {
		WebHookMockingFrameworkImpl framework = new WebHookMockingFrameworkImpl();
		framework.buildstateEnum = buildState;
		framework.extraParameters = extraParameters;
		framework.teamcityProperties = teamcityProperties;
		framework.content = new WebHookPayloadContent(framework.sBuildServer, framework.sRunningBuild, framework.previousSuccessfulBuild, buildState, extraParameters, teamcityProperties, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
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
		//webHookConfig = new WebHookConfig(ConfigLoaderUtil.getFullConfigElement(xmlConfigFile));
		webHookConfig = ConfigLoaderUtil.getFirstWebHookInConfig(xmlConfigFile);
		this.content = new WebHookPayloadContent(this.sBuildServer, this.sRunningBuild, this.previousSuccessfulBuild, this.buildstateEnum, extraParameters, teamcityProperties, webHookConfig.getEnabledTemplates());
		
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
	public WebHookListener getWebHookListener() {
		return whl;
	}

}
