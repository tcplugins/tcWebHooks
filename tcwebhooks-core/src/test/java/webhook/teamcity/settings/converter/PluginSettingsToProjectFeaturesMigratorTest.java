package webhook.teamcity.settings.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;

import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.TeamCityCoreFacade;
import webhook.teamcity.TeamCityCoreFacadeImpl;
import webhook.teamcity.exception.ProjectFeatureMigrationException;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.WebHookFeaturesStore;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSearchResult;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.WebHookUpdateResult;
import webhook.testframework.util.ConfigLoaderUtil;

public class PluginSettingsToProjectFeaturesMigratorTest {

	@Mock
	private ProjectManager myProjectManager;
	
	private TeamCityCoreFacade myTeamCityCoreFacade;
	
	@Mock
	private WebHookSettingsManager myWebHookSettingsManager;
	@Mock
	private ProjectSettingsManager  myProjectSettingsManager;
	
	@Mock
	private WebHookFeaturesStore myWebHookFeaturesStore;
	
	@Mock
	private ConfigActionFactory myConfigActionFactory;
	
	@Mock
	private ServerPaths myServerPaths;
	
	@Captor
	private ArgumentCaptor<WebHookConfig> webHookConfigCaptor;
	
	@Captor
	private ArgumentCaptor<SProject> projectCaptor;
	
	private WebHookConfig config;
	private MockSProject project01;
	
	@Mock WebHookConfigToKotlinDslRenderer myWebHookConfigToKotlinDslRenderer;
	@Mock WebHookConfigToProjectFeatureXmlRenderer myWebHookConfigToProjectFeatureXmlRenderer;
	
	
	@Before
	public void setup() throws JDOMException, IOException {
		MockitoAnnotations.initMocks(this);
		
		String projectDir = "src/test/resources/testMigrationConfigurations/projects/FirstProject";
		
		Element e = ConfigLoaderUtil.getFullConfigElement(new File(projectDir + "/pluginData/plugin-settings.xml"));
		WebHookProjectSettings settingsFromProjectSettingsManager = new WebHookProjectSettings();
		settingsFromProjectSettingsManager.readFrom(e.getChild("webhooks"));
		
		config = ConfigLoaderUtil.getFirstWebHookInConfig(new File(projectDir + "/pluginData/plugin-settings.xml"));
		SBuildType sBuildType = new MockSBuildType("Build 01", "My First Build", "bt01");
		project01 = new MockSProject("Project 01", "My First Project", "project01", "FirstProject", sBuildType);
		when(myProjectManager.findProjectById("project01")).thenReturn(project01);
		when(myProjectManager.getActiveProjects()).thenReturn(Collections.singletonList(project01));
		when(myWebHookFeaturesStore.addWebHookConfig(any(), any())).thenReturn(new WebHookUpdateResult(true, config));
		when(myProjectSettingsManager.getSettings(project01.getProjectId(), "webhooks")).thenReturn(settingsFromProjectSettingsManager);
		myTeamCityCoreFacade = new TeamCityCoreFacadeImpl(myProjectManager);
		
		// Create a bunch of temp files in "target" so that we can read and write them.
		Path teamcityTempConfigDir = Files.createTempDirectory(Paths.get("target"), "tmpBuildServerConfig");
		teamcityTempConfigDir.toFile().deleteOnExit();
		new File(teamcityTempConfigDir.toFile().toString() + "/internal.properties").createNewFile();
		when(myServerPaths.getConfigDir()).thenReturn(teamcityTempConfigDir.toString());
		when(myServerPaths.getSystemDir()).thenReturn(teamcityTempConfigDir.toString());
		
		File tempProjectDir = new File(teamcityTempConfigDir.toFile().toString() + "/projects/FirstProject");
		File pluginSettingsDir = new File(tempProjectDir.toString() + "/pluginData/");
		File pluginSettingsFile = new File(pluginSettingsDir.toString() + "/plugin-settings.xml");
		Files.createDirectories(pluginSettingsDir.toPath());
		if (!pluginSettingsFile.exists()) {
			Files.copy(new File(projectDir + "/pluginData/plugin-settings.xml").toPath(), pluginSettingsFile.toPath());
		}
		project01.setConfigDirectory(tempProjectDir);
	}
	
	
	@Test
	public void testHappyPath() throws JDOMException, IOException {
		PluginSettingsToProjectFeaturesMigrator migrator = new PluginSettingsToProjectFeaturesMigrator(
				myTeamCityCoreFacade, myWebHookSettingsManager, myProjectSettingsManager, myWebHookFeaturesStore, myConfigActionFactory, myServerPaths, null, null, myWebHookConfigToKotlinDslRenderer, myWebHookConfigToProjectFeatureXmlRenderer);
		migrator.executeAutomatedMigration();
		
		assertEquals(1, project01.getPersistCount());
		verify(myConfigActionFactory).createAction(project01, "Migrated webhooks configuration");
		verify(myWebHookFeaturesStore).addWebHookConfig(projectCaptor.capture(), webHookConfigCaptor.capture());
		assertEquals(project01.getProjectId(), projectCaptor.getValue().getProjectId());
		assertEquals(config.getUniqueKey(), webHookConfigCaptor.getValue().getUniqueKey());
	}
	
	@Test
	public void testThatWebHookIsNotCreatedIfExistsAlreadyInSettingsManager() throws JDOMException, IOException {
		WebHookSearchResult searchResult = new WebHookSearchResult();
		searchResult.setWebHookConfigEnhanced(WebHookConfigEnhanced.builder().webHookConfig(config).build());
		when(myWebHookSettingsManager.findWebHooks(any())).thenReturn(Collections.singletonList(searchResult));
		
		PluginSettingsToProjectFeaturesMigrator migrator = new PluginSettingsToProjectFeaturesMigrator(
				myTeamCityCoreFacade, myWebHookSettingsManager, myProjectSettingsManager, myWebHookFeaturesStore, myConfigActionFactory, myServerPaths, null, null, myWebHookConfigToKotlinDslRenderer, myWebHookConfigToProjectFeatureXmlRenderer);
		migrator.executeAutomatedMigration();
		
		assertEquals(1, project01.getPersistCount());
		verify(myConfigActionFactory).createAction(project01, "Migrated webhooks configuration");
		verifyZeroInteractions(myWebHookFeaturesStore) ;
	}
	
	@Test
	public void testThatPeristIsNeverCalledWhenNoProjectsFound() throws JDOMException, IOException {

		when(myTeamCityCoreFacade.getActiveProjects()).thenReturn(Collections.emptyList());

		PluginSettingsToProjectFeaturesMigrator migrator = new PluginSettingsToProjectFeaturesMigrator(
				myTeamCityCoreFacade, myWebHookSettingsManager, myProjectSettingsManager, myWebHookFeaturesStore, myConfigActionFactory, myServerPaths, null, null, myWebHookConfigToKotlinDslRenderer, myWebHookConfigToProjectFeatureXmlRenderer);
		migrator.executeAutomatedMigration();
		
		assertEquals(0, project01.getPersistCount());
		verifyZeroInteractions(myProjectSettingsManager);
		verifyZeroInteractions(myConfigActionFactory);
		verifyZeroInteractions(myWebHookFeaturesStore);
	}
	@Test
	public void testThatPeristIsNeverCalledWhenNewWebHookCannotBeSaved() throws JDOMException, IOException {
		
		when(myWebHookFeaturesStore.addWebHookConfig(any(), any())).thenReturn(new WebHookUpdateResult(false, config));
		
		PluginSettingsToProjectFeaturesMigrator migrator = new PluginSettingsToProjectFeaturesMigrator(
				myTeamCityCoreFacade, myWebHookSettingsManager, myProjectSettingsManager, myWebHookFeaturesStore, myConfigActionFactory, myServerPaths, null, null, myWebHookConfigToKotlinDslRenderer, myWebHookConfigToProjectFeatureXmlRenderer);
		migrator.executeAutomatedMigration();
		
		//Save should be attempted
		verify(myWebHookFeaturesStore).addWebHookConfig(projectCaptor.capture(), webHookConfigCaptor.capture());
		
		// But persist should not be attempted.
		assertEquals(0, project01.getPersistCount());
		verifyZeroInteractions(myConfigActionFactory);
	}
	
	@Test
	public void testThatPeristIsNeverCalledWhenOldWebHookCannotBeRemoved() throws JDOMException, IOException {
		// Mock out the ProjectSettingsManager, so that no existing webhooks are found in plugin settings
		when(myProjectSettingsManager.getSettings(project01.getProjectId(), "webhooks")).thenReturn(new WebHookProjectSettings());

		PluginSettingsToProjectFeaturesMigrator migrator = new PluginSettingsToProjectFeaturesMigrator(
				myTeamCityCoreFacade, myWebHookSettingsManager, myProjectSettingsManager, myWebHookFeaturesStore, myConfigActionFactory, myServerPaths, null, null, myWebHookConfigToKotlinDslRenderer, myWebHookConfigToProjectFeatureXmlRenderer);
		migrator.executeAutomatedMigration();
		
		//Save should be attempted
		verify(myWebHookFeaturesStore).addWebHookConfig(projectCaptor.capture(), webHookConfigCaptor.capture());
		
		// But persist should not be attempted.
		assertEquals(0, project01.getPersistCount());
		verifyZeroInteractions(myConfigActionFactory);
	}
	@Test
	public void testThatPeristIsCalledEvenWhenOldWebHookCannotBeRemovedIfAllowedToContinue() throws JDOMException, IOException, ProjectFeatureMigrationException {
		// Mock out the ProjectSettingsManager, so that no existing webhooks are found in plugin settings
		when(myProjectSettingsManager.getSettings(project01.getProjectId(), "webhooks")).thenReturn(new WebHookProjectSettings());
		
		PluginSettingsToProjectFeaturesMigrator migrator = new PluginSettingsToProjectFeaturesMigrator(
				myTeamCityCoreFacade, myWebHookSettingsManager, myProjectSettingsManager, myWebHookFeaturesStore, myConfigActionFactory, myServerPaths, null, null, myWebHookConfigToKotlinDslRenderer, myWebHookConfigToProjectFeatureXmlRenderer);
		migrator.attemptMigration(false, LocalDateTime.now(), null);
		
		//Save should be attempted
		verify(myWebHookFeaturesStore).addWebHookConfig(projectCaptor.capture(), webHookConfigCaptor.capture());
		
		// But persist should also be attempted.
		assertEquals(1, project01.getPersistCount());
		verify(myConfigActionFactory).createAction(project01, "Migrated webhooks configuration");
	}
	
	@Test
	public void testThatNothingMuchHappensWhenVcsSettingsAreConfigured() throws JDOMException, IOException {
		
		Map<String,String> featureMap = ImmutableMap.of(
				"enabled" , "true",
				"twoWaySynchronization", "false",
				"format", "kotlin"
			);
		project01.addFeature("versionedSettings", featureMap);
		
		PluginSettingsToProjectFeaturesMigrator migrator = new PluginSettingsToProjectFeaturesMigrator(
				myTeamCityCoreFacade, myWebHookSettingsManager, myProjectSettingsManager, myWebHookFeaturesStore, myConfigActionFactory, myServerPaths, null, null, myWebHookConfigToKotlinDslRenderer, myWebHookConfigToProjectFeatureXmlRenderer);
		migrator.executeAutomatedMigration();
		
		//Save should not be attempted
		verify(myWebHookFeaturesStore, times(0)).addWebHookConfig(projectCaptor.capture(), webHookConfigCaptor.capture());
		
		// But persist should not be attempted.
		assertEquals(0, project01.getPersistCount());
		verifyZeroInteractions(myConfigActionFactory);
	}
	
	@Test
	public void testThatMigrationIsAttemptedWhenVcsSettingsAreConfiguredButSyncIsEnabled() throws JDOMException, IOException {
		
		Map<String,String> featureMap = ImmutableMap.of(
				"enabled" , "true",
				"twoWaySynchronization", "true"
				);
		project01.addFeature("versionedSettings", featureMap);
		
		PluginSettingsToProjectFeaturesMigrator migrator = new PluginSettingsToProjectFeaturesMigrator(
				myTeamCityCoreFacade, myWebHookSettingsManager, myProjectSettingsManager, myWebHookFeaturesStore, myConfigActionFactory, myServerPaths, null, null, myWebHookConfigToKotlinDslRenderer, myWebHookConfigToProjectFeatureXmlRenderer);
		migrator.executeAutomatedMigration();
		
		assertEquals(1, project01.getPersistCount());
		verify(myConfigActionFactory).createAction(project01, "Migrated webhooks configuration");
		verify(myWebHookFeaturesStore).addWebHookConfig(projectCaptor.capture(), webHookConfigCaptor.capture());
		assertEquals(project01.getProjectId(), projectCaptor.getValue().getProjectId());
		assertEquals(config.getUniqueKey(), webHookConfigCaptor.getValue().getUniqueKey());

	}

}
