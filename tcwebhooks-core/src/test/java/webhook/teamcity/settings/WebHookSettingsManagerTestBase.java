package webhook.teamcity.settings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.jdom.JDOMException;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.serverSide.ConfigAction;
import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.history.WebAddressTransformerImpl;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.payload.template.MicrosftTeams01XmlWebHookTemplate;
import webhook.teamcity.payload.template.MicrosftTeams02XmlWebHookTemplate;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookSemiMockingFrameworkImpl;
import webhook.testframework.util.ConfigLoaderUtil;

public abstract class WebHookSettingsManagerTestBase {

	@Mock
	SProject sProject;
	@Mock
	SProject sProject02;
	@Mock
	protected ProjectManager projectManager;
	@Mock
	protected ConfigActionFactory configActionFactory;
	@Mock
	protected ProjectSettingsManager projectSettingsManager;
	@Mock
	SBuildType sBuildType01;
	@Mock
	SBuildType sBuildType02;
	protected WebHookSettingsManager webHookSettingsManager;
	protected WebHookProjectSettings projectSettings;

	@Before
	public void setup() throws JDOMException, IOException {
		MockitoAnnotations.initMocks(this);
		
		when(sProject.getProjectId()).thenReturn("project01");
		when(sProject.getExternalId()).thenReturn("MyProject");
		when(sProject.getName()).thenReturn("My Project");
		when(projectManager.findProjectById("project01")).thenReturn(sProject);
		when(sProject.getBuildTypes()).thenReturn(Collections.singletonList(sBuildType01));
		when(sBuildType01.getExternalId()).thenReturn("MyProject_MyBuildType");
		when(sBuildType01.getInternalId()).thenReturn("bt154");
		
		when(sProject02.getProjectId()).thenReturn("project02");
		when(sProject02.getExternalId()).thenReturn("MyProject2");
		when(sProject02.getName()).thenReturn("My Project2");
		when(projectManager.findProjectById("project02")).thenReturn(sProject02);
		when(sProject02.getBuildTypes()).thenReturn(Collections.singletonList(sBuildType02));
		when(sBuildType02.getExternalId()).thenReturn("MyProject2_MyBuildType");
		when(sBuildType02.getInternalId()).thenReturn("bt200");
		
		when(projectManager.getActiveProjects()).thenReturn(Collections.singletonList(sProject));
		when(configActionFactory.createAction(eq(sProject), any())).thenReturn(Mockito.mock(ConfigAction.class));
		
		projectSettings = new WebHookProjectSettings();
		projectSettings.readFrom(ConfigLoaderUtil.getFullConfigElement(new File("src/test/resources/project-settings-test-with-build-types.xml")).getChild("webhooks"));
		for (WebHookConfig c : projectSettings.getWebHooksConfigs()) {
			c.setProjectInternalId("project01");
			c.setProjectExternalId("MyProject");
		}
		
		when(projectSettingsManager.getSettings("project01", "webhooks")).thenReturn(projectSettings);
		
		WebHookMockingFramework framework = WebHookSemiMockingFrameworkImpl.create(
														BuildStateEnum.BUILD_STARTED,
														new ExtraParameters(new HashMap<String, String>())
												);
		
		ElasticSearchXmlWebHookTemplate elasticTemplate = new ElasticSearchXmlWebHookTemplate(
														framework.getWebHookTemplateManager(),
														framework.getWebHookPayloadManager(),
														new WebHookTemplateJaxHelperImpl(),
														framework.getProjectIdResolver(),
														null
												);
		elasticTemplate.register();
		
		MicrosftTeams01XmlWebHookTemplate microsftTeams01XmlWebHookTemplate = new MicrosftTeams01XmlWebHookTemplate(
														framework.getWebHookTemplateManager(),
														framework.getWebHookPayloadManager(),
														new WebHookTemplateJaxHelperImpl(),
														framework.getProjectIdResolver(),
														null
												);
		microsftTeams01XmlWebHookTemplate.register();
		
		MicrosftTeams02XmlWebHookTemplate microsftTeams02XmlWebHookTemplate = new MicrosftTeams02XmlWebHookTemplate(
														framework.getWebHookTemplateManager(),
														framework.getWebHookPayloadManager(),
														new WebHookTemplateJaxHelperImpl(),
														framework.getProjectIdResolver(),
														null
														);
		microsftTeams02XmlWebHookTemplate.register();
		
		webHookSettingsManager = new WebHookSettingsManagerImpl(
														projectManager, 
														configActionFactory, projectSettingsManager, 
														framework.getWebHookTemplateManager(), 
														framework.getWebHookPayloadManager(), 
														new WebAddressTransformerImpl());
		webHookSettingsManager.initialise();
	}

}
