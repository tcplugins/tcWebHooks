package webhook.teamcity.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class WebHookSettingsManagerImplTest extends WebHookSettingsManagerTestBase {
	
	@Test
	public void testFindWebHooksByTemplateFormat() {
		assertEquals(2, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().formatShortName("jsonTemplate").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().formatShortName("jsonVelocityTemplate").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().formatShortName("xmlTemplate").build()).size());
	}
	
	@Test
	public void testFindWebHooksByTemplateId() {
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().templateId("elasticsearch").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().templateId("flowdock").build()).size());
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().templateId("microsoft-teams").build()).size());
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().templateId("microsoft-teams-2").build()).size());
	}
	
	@Test
	public void testFindWebHooksByUrlSubString() {
		assertEquals(2, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().urlSubString("buildevent").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().urlSubString("blahblah").build()).size());
	}
	
	@Test
	public void testFindWebHooksByWebHookId() {
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().webhookId(projectSettings.getWebHooksConfigs().get(0).getUniqueKey()).build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().webhookId("blahblah").build()).size());
	}
	
	@Test
	public void testFindWebHooksBySearch() {
		assertEquals(2, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("jsonTemplate").build()).size());
		assertEquals(2, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("microsoft-teams").build()).size());
		assertEquals(1, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("microsoft-teams-2").build()).size());
		assertEquals(2, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("buildevent").build()).size());
		assertEquals(2, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("buildevent").build()).size());
		assertEquals(0, webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().textSearch("blahblah").build()).size());
	}

	@Test
	public void testGetTemplateUsageCount() {
		assertEquals(1, webHookSettingsManager.getTemplateUsageCount("microsoft-teams"));
		assertEquals(1, webHookSettingsManager.getTemplateUsageCount("microsoft-teams-2"));
		assertEquals(0, webHookSettingsManager.getTemplateUsageCount("something"));
	}
	
	@Test
	public void testMapOldBuildTypesToNewBuildTypes() {
	    WebHookSettingsManagerImpl webHookSettingsManagerImpl = new WebHookSettingsManagerImpl(projectManager, configActionFactory, webhookFeaturesStore, null, null, null);
	    WebHookConfig whc01 = webHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().formatShortName("jsonTemplate").build()).get(0).getWebHookConfig();
	    Set<String> renamedBuildTypeIds = webHookSettingsManagerImpl.mapOldBuildTypesToNewBuildTypes(whc01, "project01",  "project02");
	    assertTrue(renamedBuildTypeIds.contains("bt200"));
	}
	
	@Test
	public void testFuzzyNameMatcher() {
	    assertTrue(WebHookSettingsManagerImpl.fuzzyNameMatcher("Project1", "Project1_Build01", "Project02", "Project02_Build01"));
	    assertTrue(WebHookSettingsManagerImpl.fuzzyNameMatcher("project1", "Project1_Build01", "Project02", "Project02_Build01"));
	    assertTrue(WebHookSettingsManagerImpl.fuzzyNameMatcher("Project1", "Project1Build01", "Project02", "Project02Build01"));
	}
	
}
