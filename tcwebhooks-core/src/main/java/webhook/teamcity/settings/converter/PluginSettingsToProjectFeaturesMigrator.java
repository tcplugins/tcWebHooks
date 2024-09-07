package webhook.teamcity.settings.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jdom.JDOMException;

import jetbrains.buildServer.serverSide.ConfigAction;
import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.FileWatchingPropertiesModel;
import jetbrains.buildServer.serverSide.PersistFailedException;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import lombok.RequiredArgsConstructor;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.Loggers;
import webhook.teamcity.exception.ProjectFeatureMigrationException;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFeaturesStore;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookProjectSettingsFactory;
import webhook.teamcity.settings.WebHookSearchFilter;
import webhook.teamcity.settings.WebHookSearchResult;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.WebHookUpdateResult;

/**
 * A Migrator that does the following: <br>
 * 1. Get list of projects <br>
 * 2. Check for file in each project's plugins settings directory. <br>
 * 3. Read file and check if webhooks are found. <br>
 * 4. If webhooks enabled, and webhooks count > 0 <br>
 * 5. Backup file with name like plugin-settings-tcWebHooks-backup-yyyy-MM-dd_HH-mm-ss.xml from LocalTime. <br>
 *  <br>
 * Do the following for each project with webhooks. <br>
 *  <br>
 * a. Use ReadFrom to load configs. <br>
 * b. Iterate over each webhook <br>
 *      - call addNewWebHook  <br>
 *      - remove from WebHookPluginSettings internal map. <br>
 * c. Call persist on each project. <br>
 *  <br>
 * Add a setting to config/internal.properties for teamcity.plugin.tcWebHooks.pluginSettingsToProjectFeaturesMigation=completed
 *  <br> <br>
 * Other considerations:  <br>
 * - How to migrate when VCS settings prevent write back. <br>
 * - Convert to ProjectFeatures XML format to show copy and paste. <br>
 * - Convert to KotlinDSL format to show copy and paste. <br>
 * - Do this from somewhere that they can choose the file to load. <br>
 * 
 * 
 * if pluginSettingsToProjectFeaturesMigationCompleted is true <br>
 *  - Don't register the {@link WebHookProjectSettingsFactory} in this code.... projectSettingsManager.registerSettingsFactory <br>
 *  - Skip this migration on startup.<br>
 */

@RequiredArgsConstructor
public class PluginSettingsToProjectFeaturesMigrator implements DeferrableService {
	
	
	private final ProjectManager myProjectManager;
	private final WebHookSettingsManager myWebHookSettingsManager;
	private final ProjectSettingsManager myProjectSettingsManager;
	private final WebHookFeaturesStore myWebHookFeaturesStore;
	private final ConfigActionFactory myConfigActionFactory;
	private final ServerPaths myServerPaths;
	private final ScheduledExecutorService myExecutorService;
	private final DeferrableServiceManager myDeferrableServiceManager;
	private ScheduledFuture<?> future;

	private static final String propertyKey = "teamcity.plugin.tcWebHooks.pluginSettingsToProjectFeaturesMigation";
	
	
	
	public void executeAutomatedMigration() {
		// If our migration was started or completed, we don't want to attempt it again.
		if (TeamCityProperties.getPropertyOrNull(propertyKey) != null) {
			Loggers.SERVER.info("Skipping tcWebHooks ProjectFeatures migration. Migration is already marked as completed");
		} else {
			try {
				markMigrationAs("started");
				attemptMigration(true);
			} catch (ProjectFeatureMigrationException e) {
				Loggers.SERVER.warn("Failed to complete tcWebHooks PluginSettings to ProjectFeatures migration.", e);
			}
		}
		
	}

	protected void attemptMigration(boolean failIfCantRemoveWebhookFromCurrentConfiguration) throws ProjectFeatureMigrationException {
		Map<SProject, WebHookProjectSettings> projectsForMigration = getProjectsThatRequireMigration();
		if (projectsForMigration.isEmpty()) {
			Loggers.SERVER.info("No projects require tcWebHooks ProjectFeature migration. Marking migration as completed.");
			markMigrationAs("completed");
			return;
		}
		for (Map.Entry<SProject, WebHookProjectSettings> e : projectsForMigration.entrySet()) {
			if (checkIfProjectHasVcsEnabledAndSyncDisabled(e.getKey())) {
				Loggers.SERVER.warn(String.format("Project '%s' has VCS Settings enabled and Sync disabled. tcWebHooks configurations will NOT be migrated, and will have to be done by hand.", e.getKey().getExternalId()));
				continue;
			} else if (checkIfProjectHasVcsEnabledAndSyncEnabled(e.getKey())) {
				Loggers.SERVER.warn(String.format("Project '%s' has VCS Settings enabled. tcWebHooks configurations will be attempted but may still have to be done by hand.", e.getKey().getExternalId()));
				attemptMigration(e.getKey(), e.getValue(), failIfCantRemoveWebhookFromCurrentConfiguration);
			} else {
				attemptMigration(e.getKey(), e.getValue(), failIfCantRemoveWebhookFromCurrentConfiguration);
			}
		}
	}

	private void attemptMigration(SProject project, WebHookProjectSettings webhookSettings, boolean failIfCantRemoveWebhookFromCurrentConfiguration) throws ProjectFeatureMigrationException {
		try {
			backupExistingPluginSettings(project);
		} catch (IOException e) {
			throw new ProjectFeatureMigrationException(String.format("Unable to create backup plugin-settings.xml file for project '%s'. All further migrations will be aborted.", project.getExternalId()), e);
		}
		WebHookProjectSettings webHookProjectSettings =  (WebHookProjectSettings) this.myProjectSettingsManager.getSettings(project.getProjectId(), "webhooks");
		for (WebHookConfig w : webhookSettings.getWebHooksAsList()) {
			
			List<WebHookSearchResult> result = this.myWebHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().projectExternalId(project.getExternalId()).webhookId(w.getUniqueKey()).build());
			if (!result.isEmpty()) {
				Loggers.SERVER.warn(String.format("WebHook with id '%s' in project '%s' already exists. Will not migrate this webhook", w.getUniqueKey(), project.getExternalId()));
			} else {
				WebHookUpdateResult addResult = this.myWebHookFeaturesStore.addWebHookConfig(project, w);
				if (!addResult.isUpdated()) {
					throw new ProjectFeatureMigrationException("Unable to add webhook from Plugin Settings as a ProjectFeature", addResult.getWebHookConfig());
				} else {
					Loggers.SERVER.info(String.format("Copied webHook to ProjectFeatures. Id: '%s', Project: '%s'", addResult.getWebHookConfig().getUniqueKey(), project.getExternalId()));
				}
				WebHookUpdateResult deleteResult = webHookProjectSettings.deleteWebHook(w.getUniqueKey(), project.getProjectId());
				if (!deleteResult.isUpdated() && failIfCantRemoveWebhookFromCurrentConfiguration) {
					throw new ProjectFeatureMigrationException("Unable to delete existing webhook from Project Settings", deleteResult.getWebHookConfig());
				} else if (!deleteResult.isUpdated()){
					Loggers.SERVER.warn(String.format("Unable to delete existing webhook from Plugin Settings. WebHook id '%s', ProjectId: '%s'", w.getUniqueKey(), project.getExternalId()));
				} else {
					Loggers.SERVER.info(String.format("Deleted webHook from PluginSettings. Id: '%s', Project: '%s'", addResult.getWebHookConfig().getUniqueKey(), project.getExternalId()));
				}
			}
		}
		try {
			ConfigAction cause = myConfigActionFactory.createAction(project, "Migrated webhooks configuration");
			project.persist(cause);
		} catch (AccessDeniedException | PersistFailedException ex) {
			throw new ProjectFeatureMigrationException("Failed to perist updated configurations for project " + project.getExternalId(), ex);
		}
	}

	private void backupExistingPluginSettings(SProject project) throws IOException {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
		LocalDateTime now = LocalDateTime.now();
		File projectConfigDir = project.getConfigDirectory();
		File projectPluginsConfigDir = new File(projectConfigDir,"pluginData");
		File projectPluginSettingsFile = new File(projectPluginsConfigDir, "plugin-settings.xml");
		File projectPluginSettingsBackupFile = new File(projectPluginsConfigDir, String.format("plugin-settings-%s.xml", now.format(dateTimeFormatter)));
		FileUtils.copyFile(projectPluginSettingsFile, projectPluginSettingsBackupFile);
		Loggers.SERVER.info(String.format("Backed up existing plugin-settings file in project '%s'. Copied from '%s' to '%s'", project.getExternalId(), projectPluginSettingsFile, projectPluginSettingsBackupFile));
	}

	private boolean checkIfProjectHasVcsEnabledAndSyncDisabled(SProject key) {
		Collection<SProjectFeatureDescriptor> versionedSettings = key.getAvailableFeaturesOfType("versionedSettings");
		Optional<SProjectFeatureDescriptor> vs = versionedSettings.stream().findFirst();
		if (vs.isPresent() && Boolean.valueOf(vs.get().getParameters().get("enabled")) && !Boolean.valueOf(vs.get().getParameters().get("twoWaySynchronization"))) {
			return true;
		}
		return false;
	}
	
	private boolean checkIfProjectHasVcsEnabledAndSyncEnabled(SProject key) {
		Collection<SProjectFeatureDescriptor> versionedSettings = key.getAvailableFeaturesOfType("versionedSettings");
		Optional<SProjectFeatureDescriptor> vs = versionedSettings.stream().findFirst();
		if (vs.isPresent() && Boolean.valueOf(vs.get().getParameters().get("enabled")) && Boolean.valueOf(vs.get().getParameters().get("twoWaySynchronization"))) {
			return true;
		}
		return false;
	}

	private void markMigrationAs(String status) throws ProjectFeatureMigrationException {
		String teamcityInternalPropertiesFilename = this.myServerPaths.getConfigDir() + File.separator + FileWatchingPropertiesModel.DEFAULT_PROPERTIES_FILE_NAME;
		Properties teamcityInternalProperties = new Properties();
		try {
			teamcityInternalProperties.load(new FileInputStream(teamcityInternalPropertiesFilename));
			teamcityInternalProperties.put(propertyKey, status);
			teamcityInternalProperties.store(new FileWriter(teamcityInternalPropertiesFilename), String.format("Set tcWebHooks migration '%s' flag to TeamCity's internal.properties file", status));
		} catch (IOException e) {
			throw new ProjectFeatureMigrationException("Unable to set migration status flag in internal.properties file", e);
		}
	}
	private Map<SProject, WebHookProjectSettings> getProjectsThatRequireMigration() {
		Map<SProject, WebHookProjectSettings> projectsThatRequireMigrations = new LinkedHashMap<>();
		for (SProject project : myProjectManager.getActiveProjects()) {
			File projectConfigDir = project.getConfigDirectory();
			File projectPluginsConfigDir = new File(projectConfigDir,"pluginData");
			File projectPluginSettingsFile = new File(projectPluginsConfigDir, "plugin-settings.xml");
			if (projectPluginsConfigDir.isDirectory() && projectPluginsConfigDir.canWrite()) {
				if (projectPluginSettingsFile.canRead()) {
					try {
						WebHookProjectSettings webhooks = ConfigLoaderUtil.getAllWebHooksInConfig(projectPluginSettingsFile);
						if (webhooks != null && webhooks.isEnabled() && !webhooks.getWebHooksAsList().isEmpty()) {
							projectsThatRequireMigrations.put(project, webhooks);
						} else {
							Loggers.SERVER.info(String.format(
									"pluginData/plugin-settings.xml for project '%s' does not contain a webhooks XML element. No webhook migration will be attempted for this project.",
									project.getExternalId()));
						}
					} catch (JDOMException | IOException e) {
						Loggers.SERVER.info(String.format(
								"pluginData/plugin-settings.xml for project '%s' is not readable as plugin-settings file. No webhook migration will be attempted for this project. Reason: %s",
								project.getExternalId(),
								e.getMessage()));
					}
				} else {
					Loggers.SERVER.info(String.format(
							"pluginData/plugin-settings.xml for project '%s' does not exist. No webhook migration will be attempted for this project.",
							project.getExternalId()));
				}
			} else {
				Loggers.SERVER.info(String.format(
						"pluginData directory for project '%s' does not exist or is not writable. No webhook migration will be attempted for this project.",
						project.getExternalId()));
			}
			
		}
		return projectsThatRequireMigrations;
	}

	@Override
	public void requestDeferredRegistration() {
		Loggers.SERVER.info("PluginSettingsToProjectFeaturesMigrator :: Registering as a deferrable service");
		myDeferrableServiceManager.registerService(this);
	}

	@Override
	public void register() {
		Loggers.SERVER.info("PluginSettingsToProjectFeaturesMigrator :: Scheduling migration to start in 60 seconds.");
		this.future =  this.myExecutorService.schedule(new PluginSettingsToProjectFeaturesMigratorScheduledTask(this), 60, TimeUnit.SECONDS);

	}

	@Override
	public void unregister() {
		if (!this.future.isDone()) {
			this.future.cancel(true);
		}
	}
	
	@RequiredArgsConstructor
	public class PluginSettingsToProjectFeaturesMigratorScheduledTask implements Runnable {
		
		private final PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator;
		@Override
		public void run() {
			pluginSettingsToProjectFeaturesMigrator.executeAutomatedMigration();
		}
		
	}
}
