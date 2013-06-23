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
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SRunningBuild;
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
	WebHookMainSettings configSettings = mock(WebHookMainSettings.class);
	WebHookPayloadManager manager = mock(WebHookPayloadManager.class);
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
	SRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	SProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
	WebHookListener whl;
	SortedMap<String, String> extraParameters;
	BuildStateEnum buildstateEnum;
	
	private WebHookMockingFrameworkImpl() {
		webHookImpl = new WebHookImpl();
		spyWebHook = spy(webHookImpl);   
		whl = new WebHookListener(sBuildServer, settings, configSettings, manager, factory);
		projSettings = new WebHookProjectSettings();
		when(factory.getWebHook()).thenReturn(spyWebHook);
		when(manager.isRegisteredFormat("JSON")).thenReturn(true);
		when(manager.getFormat("JSON")).thenReturn(payload);
		when(manager.getServer()).thenReturn(sBuildServer);
		when(sBuildServer.getHistory()).thenReturn(buildHistory);
		when(sBuildServer.getRootUrl()).thenReturn("http://test.server");
		when(previousSuccessfulBuild.getBuildStatus()).thenReturn(Status.NORMAL);
		when(previousSuccessfulBuild.isPersonal()).thenReturn(false);
		when(previousFailedBuild.getBuildStatus()).thenReturn(Status.FAILURE);
		when(previousFailedBuild.isPersonal()).thenReturn(false);
		finishedSuccessfulBuilds.add(previousSuccessfulBuild);
		finishedFailedBuilds.add(previousFailedBuild);
		when(settings.getSettings(sRunningBuild.getProjectId(), "webhooks")).thenReturn(projSettings);
		((MockSBuildType) sBuildType).setProject(sProject);
		whl.register();
		
	}

	public static WebHookMockingFramework create(BuildStateEnum buildState, ExtraParametersMap extraParameters) {
		WebHookMockingFrameworkImpl framework = new WebHookMockingFrameworkImpl();
		framework.buildstateEnum = buildState;
		framework.extraParameters = extraParameters;
		framework.content = new WebHookPayloadContent(framework.sBuildServer, framework.sRunningBuild, framework.previousSuccessfulBuild, buildState, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
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
		this.content = new WebHookPayloadContent(this.sBuildServer, this.sRunningBuild, this.previousSuccessfulBuild, this.buildstateEnum, extraParameters, webHookConfig.getEnabledTemplates());
		
	}
	
	@Override
	public WebHookConfig getWebHookConfig() {
		return webHookConfig;
	}

}
