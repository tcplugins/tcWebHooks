package webhook.teamcity.settings;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.history.WebAddressTransformerImpl;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelperImpl;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookSemiMockingFrameworkImpl;
import webhook.testframework.util.ConfigLoaderUtil;

public class WebHookSettingsManagerImplTest {
	
	@Mock SProject sProject;
	@Mock ProjectManager projectManager;
	@Mock ProjectSettingsManager projectSettingsManager;
	
	private WebHookSettingsManager webHookSettingsManager;
	private WebHookProjectSettings projectSettings;
	
	@Before
	public void setup() throws JDOMException, IOException {
		MockitoAnnotations.initMocks(this);
		
		when(sProject.getProjectId()).thenReturn("project01");
		when(sProject.getExternalId()).thenReturn("MyProject");
		when(sProject.getName()).thenReturn("My Project");
		when(projectManager.getActiveProjects()).thenReturn(Collections.singletonList(sProject));
		when(projectManager.findProjectById(anyString())).thenReturn(sProject);
		
		projectSettings = new WebHookProjectSettings();
		projectSettings.readFrom(ConfigLoaderUtil.getFullConfigElement(new File("src/test/resources/project-settings-test-elastic.xml")).getChild("webhooks"));
		
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
		
		webHookSettingsManager = new WebHookSettingsManagerImpl(
														projectManager, 
														projectSettingsManager, 
														framework.getWebHookTemplateManager(), 
														framework.getWebHookPayloadManager(), 
														new WebAddressTransformerImpl());
		webHookSettingsManager.initialise();
	}

	@Test
	public void testFindWebHooksByTemplateFormat() {
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().formatShortName("jsonTemplate").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().formatShortName("jsonVelocityTemplate").build()).size());
	}
	
	@Test
	public void testFindWebHooksByTemplateId() {
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().templateId("elasticsearch").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().templateId("flowdock").build()).size());
	}
	
	@Test
	public void testFindWebHooksByUrlSubString() {
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().urlSubString("buildevent").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().urlSubString("blahblah").build()).size());
	}
	
	@Test
	public void testFindWebHooksByWebHookId() {
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().webhookId(projectSettings.getWebHooksConfigs().get(0).getUniqueKey()).build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().webhookId("blahblah").build()).size());
	}
	
	@Test
	public void testFindWebHooksBySearch() {
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("jsonTemplate").build()).size());
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("elasticsearch").build()).size());
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("buildevent").build()).size());
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("buildevent").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("blahblah").build()).size());
	}

	@Test
	public void testGetTemplateUsageCount() {
		assertEquals(1, webHookSettingsManager.getTemplateUsageCount("elasticsearch"));
		assertEquals(0, webHookSettingsManager.getTemplateUsageCount("something"));
	}
}
