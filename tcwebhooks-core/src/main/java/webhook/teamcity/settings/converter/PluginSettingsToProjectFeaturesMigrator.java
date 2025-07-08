package webhook.teamcity.settings.converter;

import java.io.BufferedWriter;
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
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom.JDOMException;

import jetbrains.buildServer.serverSide.ConfigAction;
import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.FileWatchingPropertiesModel;
import jetbrains.buildServer.serverSide.PersistFailedException;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import lombok.RequiredArgsConstructor;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.Loggers;
import webhook.teamcity.TeamCityCoreFacade;
import webhook.teamcity.TeamCityCoreFacade.ProjectVcsStatus;
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
	
	
	private final TeamCityCoreFacade myTeamCityCoreFacade;
	private final WebHookSettingsManager myWebHookSettingsManager;
	private final ProjectSettingsManager myProjectSettingsManager;
	private final WebHookFeaturesStore myWebHookFeaturesStore;
	private final ConfigActionFactory myConfigActionFactory;
	private final ServerPaths myServerPaths;
	private final DeferrableServiceManager myDeferrableServiceManager;
	private ScheduledFuture<?> future;
	private final ExecutorServices executorServices;
	private final WebHookConfigToKotlinDslRenderer myWebHookConfigToKotlinDslRenderer;
	private final WebHookConfigToProjectFeatureXmlRenderer myWebHookConfigToProjectFeatureXmlRenderer;


	private static final String TEAMCITY_INTERNAL_PROPERTY_KEY = "teamcity.plugin.tcWebHooks.pluginSettingsToProjectFeaturesMigration";
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	private static final DateTimeFormatter logDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	
	
	
	public void executeAutomatedMigration() {
		// If our migration was started or completed, we don't want to attempt it again.
		String existingMigrationResult = TeamCityProperties.getPropertyOrNull(TEAMCITY_INTERNAL_PROPERTY_KEY);
		if (existingMigrationResult != null) {
			Loggers.SERVER.info(String.format("Skipping tcWebHooks ProjectFeatures migration. Migration is already marked as '%s'", existingMigrationResult));
		} else {
		    LocalDateTime now = LocalDateTime.now();
		    String fileName = getReportFileName(now);
		    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
    			try {
    				markMigrationAs("started");
    				writer.write("Starting PluginSettings To ProjectFeatures Migration at " + now.format(dateTimeFormatter) );
    				writer.write("\n----------------------------------------------------------------------------------------\n");
    				attemptMigration(true, now, writer);
    				markMigrationAs("completed");
    				addSectionToReport(writer, String.format("tcWebHooks PluginSettings to ProjectFeatures migration completed see '%s' for a report.", fileName));
    			} catch (ProjectFeatureMigrationException e) {
    			    addToReport(writer, "Failed to complete tcWebHooks PluginSettings to ProjectFeatures migration.", e);
    				Loggers.SERVER.warn("Failed to complete tcWebHooks PluginSettings to ProjectFeatures migration.", e);
    			}
		    } catch (IOException e1) {
                Loggers.SERVER.warn("Failed to create migration report at: " + fileName);
            }
		}
		
	}

	private void addToReport(BufferedWriter writer, String string) {
	    addToReport(writer, string, null);
	}
	
    private void addToReport(BufferedWriter writer, String string, Exception e) {
        if (e != null) {
            Loggers.SERVER.warn(string, e);
        } else {
            Loggers.SERVER.info(string);
        }
        if (writer != null) {
            try {
                writer.write(LocalDateTime.now().format(logDateTimeFormatter) + " : " + string + "\n");
                if (e != null) {
                    writer.write(ExceptionUtils.getStackTrace(e));
                }
                writer.flush();
            } catch (IOException e1) {
                // do nothing
            }
        }
    }
    
    private void addSectionToReport(BufferedWriter writer, String string) {
        if (writer != null) {
            try {
                writer.write("\n\n" + LocalDateTime.now().format(logDateTimeFormatter) + " : " + string + "\n");
                writer.flush();
            } catch (IOException e1) {
                // do nothing
            }
        }
    }
    
    private void addWarningToReport(BufferedWriter writer, String string) {
        Loggers.SERVER.warn(string);
        if (writer != null) {
            try {
                writer.write(LocalDateTime.now().format(logDateTimeFormatter) + " : " + string + "\n");
                writer.flush();
            } catch (IOException e1) {
                // do nothing
            }
        }
    }


    private String getReportFileName(LocalDateTime now) {
        return this.myServerPaths.getSystemDir() + File.separator + "tcWebHooks-ProjectFeatures-migration-report-" + now.format(dateTimeFormatter) + ".txt";
    }

    protected void attemptMigration(boolean failIfCantRemoveWebhookFromCurrentConfiguration, LocalDateTime now, BufferedWriter writer) throws ProjectFeatureMigrationException {
		Map<SProject, WebHookProjectSettings> projectsForMigration = getProjectsThatRequireMigration(writer);
		if (projectsForMigration.isEmpty()) {
			addToReport(writer, "No projects require tcWebHooks ProjectFeature migration. Marking migration as completed.");
			markMigrationAs("completed");
			return;
		}
		addToReport(writer, String.format("The following projects has been selected for migration: %s", projectsForMigration.keySet().stream().map(p -> p.getExternalId()).collect(Collectors.joining(", "))));
		for (Map.Entry<SProject, WebHookProjectSettings> e : projectsForMigration.entrySet()) {
			determineThenAttemptMigration(failIfCantRemoveWebhookFromCurrentConfiguration, now, writer, e.getKey(), e.getValue());
		}
	}

    private void determineThenAttemptMigration(boolean failIfCantRemoveWebhookFromCurrentConfiguration,
            LocalDateTime now, BufferedWriter writer, SProject project, WebHookProjectSettings settings)
            throws ProjectFeatureMigrationException {
		ProjectVcsStatus vcsStatus = myTeamCityCoreFacade.getProjectVcsStatus(project);
        if (vcsStatus.isVcsEnabled() && vcsStatus.isKotlin() && !vcsStatus.isVcsSyncEnabled()) {
            addWarningToReport(writer, String.format("Project '%s' has VCS Settings enabled and Sync disabled. tcWebHooks configurations will NOT be migrated, and will have to be done by hand.", project.getExternalId()));
            addToReport(writer, "A kotlin DSL configuration of each webhook is produced below. Please copy and paste these configuration(s) into settings.kts inside the features block.");
            settings.getWebHooksAsList().forEach(w -> addToReport(writer,"\n" + myWebHookConfigToKotlinDslRenderer.renderAsKotlinDsl(w,12)));
        } else if (vcsStatus.isVcsEnabled() && vcsStatus.isKotlin() && vcsStatus.isVcsSyncEnabled()) {
            addWarningToReport(writer, String.format("Project '%s' has VCS Settings enabled. tcWebHooks configurations will be attempted but may still have to be done by hand.", project.getExternalId()));
            addToReport(writer, "A kotlin DSL configuration of each webhook is produced below. You may need to copy and paste these configuration(s) into settings.kts inside the features block. TeamCity may also have created a patch file in the project's settings repo. This will need to be fixed.");
            settings.getWebHooksAsList().forEach(w -> addToReport(writer,"\n" + myWebHookConfigToKotlinDslRenderer.renderAsKotlinDsl(w,12)));
        	attemptMigration(project, settings, failIfCantRemoveWebhookFromCurrentConfiguration, now, writer);
        } else if (vcsStatus.isVcsEnabled() && !vcsStatus.isKotlin() && !vcsStatus.isVcsSyncEnabled()) {
            addWarningToReport(writer, String.format("Project '%s' has VCS Settings enabled and Sync disabled. tcWebHooks configurations will NOT be migrated, and will have to be done by hand.", project.getExternalId()));
            addToReport(writer, "An project-config.xml configuration of each webhook is produced below. Please copy and paste these configuration(s) into project-config.xml in your VCS settings.");
            try {
                addToReport(writer,"\n" + myWebHookConfigToProjectFeatureXmlRenderer.renderAsXml(settings.getWebHooksAsList()));
            } catch (JAXBException jaxbException) {
                throw new ProjectFeatureMigrationException(String.format("Unable to create backup plugin-settings.xml file for project '%s'. All further migrations will be aborted.", project.getExternalId()), jaxbException);
            }
        } else {
        	attemptMigration(project, settings, failIfCantRemoveWebhookFromCurrentConfiguration, now, writer);
        }
    }

	private void attemptMigration(SProject project, WebHookProjectSettings webhookSettings, boolean failIfCantRemoveWebhookFromCurrentConfiguration, LocalDateTime now, BufferedWriter writer) throws ProjectFeatureMigrationException {
		addSectionToReport(writer, String.format("Starting migration of project '%s'. There are '%s' webhooks to migrate", project.getExternalId(), webhookSettings.getWebHooksAsList().size()));
		try {
			backupExistingPluginSettings(project, now, writer);
		} catch (IOException e) {
			throw new ProjectFeatureMigrationException(String.format("Unable to create backup plugin-settings.xml file for project '%s'. All further migrations will be aborted.", project.getExternalId()), e);
		}
		WebHookProjectSettings webHookProjectSettings =  (WebHookProjectSettings) this.myProjectSettingsManager.getSettings(project.getProjectId(), "webhooks");
		addToReport(writer, "Existing webhook IDs in ProjectFeatures: " + this.myWebHookSettingsManager.getWebHooksForProject(project).stream().map(w -> w.getWebHookConfig().getUniqueKey()).collect(Collectors.joining(", ")));
		addToReport(writer, "Existing webhook IDs in ProjectSettingsManager: " + webHookProjectSettings.getWebHooksAsList().stream().map(w -> w.getUniqueKey()).collect(Collectors.joining(", ")));
		addToReport(writer, "Existing webhook IDs found in plugins-settings.xml file: " + webhookSettings.getWebHooksAsList().stream().map(w -> w.getUniqueKey()).collect(Collectors.joining(", ")));
		for (WebHookConfig w : webhookSettings.getWebHooksAsList()) {
			
			List<WebHookSearchResult> result = this.myWebHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().webhookId(w.getUniqueKey()).build());
			if (!result.isEmpty()) {
				addWarningToReport(writer, String.format("WebHook with id '%s' in project '%s' already exists. Will not migrate this webhook", w.getUniqueKey(), project.getExternalId()));
			} else {
				WebHookUpdateResult addResult = this.myWebHookFeaturesStore.addWebHookConfig(project, w);
				if (!addResult.isUpdated()) {
					throw new ProjectFeatureMigrationException("Unable to add webhook from Plugin Settings as a ProjectFeature", addResult.getWebHookConfig());
				} else {
				    addToReport(writer, String.format("Copied webHook to ProjectFeatures. Id: '%s', Project: '%s'", addResult.getWebHookConfig().getUniqueKey(), project.getExternalId()));
				}
				WebHookUpdateResult deleteResult = webHookProjectSettings.deleteWebHook(w.getUniqueKey(), project.getProjectId());
				if (!deleteResult.isUpdated() && failIfCantRemoveWebhookFromCurrentConfiguration) {
					throw new ProjectFeatureMigrationException("Unable to delete existing webhook from Project Settings", deleteResult.getWebHookConfig());
				} else if (!deleteResult.isUpdated()){
				    addWarningToReport(writer, String.format("Unable to delete existing webhook from Plugin Settings. WebHook id '%s', ProjectId: '%s'", w.getUniqueKey(), project.getExternalId()));
				} else {
				    addToReport(writer, String.format("Deleted webHook from PluginSettings. Id: '%s', Project: '%s'", addResult.getWebHookConfig().getUniqueKey(), project.getExternalId()));
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

	private void backupExistingPluginSettings(SProject project, LocalDateTime now, BufferedWriter writer) throws IOException {
		File projectConfigDir = project.getConfigDirectory();
		File projectPluginsConfigDir = new File(projectConfigDir,"pluginData");
		File projectPluginSettingsFile = new File(projectPluginsConfigDir, "plugin-settings.xml");
		File projectPluginSettingsBackupFile = new File(projectPluginsConfigDir, String.format("plugin-settings-%s.xml", now.format(dateTimeFormatter)));
		FileUtils.copyFile(projectPluginSettingsFile, projectPluginSettingsBackupFile);
		addToReport(writer, String.format("Backed up existing plugin-settings.xml file in project '%s'. Copied from '%s' to '%s'", project.getExternalId(), projectPluginSettingsFile, projectPluginSettingsBackupFile));
	}
	
	
	
	public boolean checkIfProjectIsKotlin(SProject key) {
	    Collection<SProjectFeatureDescriptor> versionedSettings = key.getAvailableFeaturesOfType("versionedSettings");
	    Optional<SProjectFeatureDescriptor> vs = versionedSettings.stream().findFirst();
	    if (Boolean.TRUE.equals(vs.isPresent() && vs.get().getParameters().containsKey("format") && vs.get().getParameters().get("format").equals("kotlin"))) {
	        return true;
	    }
	    return false;
	}
	
	public boolean checkIfProjectHasVcsEnabled(SProject key) {
	    Collection<SProjectFeatureDescriptor> versionedSettings = key.getAvailableFeaturesOfType("versionedSettings");
	    Optional<SProjectFeatureDescriptor> vs = versionedSettings.stream().findFirst();
	    if (Boolean.TRUE.equals(vs.isPresent())) {
	        return true;
	    }
	    return false;
	}

	public boolean checkIfProjectHasVcsEnabledAndSyncDisabled(SProject key) {
		Collection<SProjectFeatureDescriptor> versionedSettings = key.getAvailableFeaturesOfType("versionedSettings");
		Optional<SProjectFeatureDescriptor> vs = versionedSettings.stream().findFirst();
		if (Boolean.TRUE.equals(vs.isPresent() && Boolean.valueOf(vs.get().getParameters().get("enabled"))) && Boolean.FALSE.equals(Boolean.valueOf(vs.get().getParameters().get("twoWaySynchronization")))) {
			return true;
		}
		return false;
	}
	
	public boolean checkIfProjectHasVcsEnabledAndSyncEnabled(SProject key) {
		Collection<SProjectFeatureDescriptor> versionedSettings = key.getAvailableFeaturesOfType("versionedSettings");
		Optional<SProjectFeatureDescriptor> vs = versionedSettings.stream().findFirst();
		if (vs.isPresent() && Boolean.TRUE.equals(Boolean.valueOf(vs.get().getParameters().get("enabled"))) && Boolean.TRUE.equals(Boolean.valueOf(vs.get().getParameters().get("twoWaySynchronization")))) {
			return true;
		}
		return false;
	}

	private void markMigrationAs(String status) throws ProjectFeatureMigrationException {
		String teamcityInternalPropertiesFilename = this.myServerPaths.getConfigDir() + File.separator + FileWatchingPropertiesModel.DEFAULT_PROPERTIES_FILE_NAME;
		Properties teamcityInternalProperties = new Properties();
		
		try (FileInputStream reader = new FileInputStream(teamcityInternalPropertiesFilename); FileWriter writer = new FileWriter(teamcityInternalPropertiesFilename)){
			teamcityInternalProperties.load(reader);
			teamcityInternalProperties.put(TEAMCITY_INTERNAL_PROPERTY_KEY, status);
			
			teamcityInternalProperties.store(writer,String.format("Set tcWebHooks migration '%s' flag in TeamCity's internal.properties file", status));
		} catch (IOException e) {
			throw new ProjectFeatureMigrationException("Unable to set migration status flag in internal.properties file", e);
		}
	}
	private Map<SProject, WebHookProjectSettings> getProjectsThatRequireMigration(BufferedWriter writer) {
		Map<SProject, WebHookProjectSettings> projectsThatRequireMigrations = new LinkedHashMap<>();
		for (SProject project : myTeamCityCoreFacade.getActiveProjects()) {
			Pair<String,WebHookProjectSettings> webhooks = getProjectSettingsIfItCanBeMigrated(project);
			if (webhooks.getRight() != null) {
			    projectsThatRequireMigrations.put(project, webhooks.getRight());
			} else {
			    Loggers.SERVER.info(webhooks.getLeft());
			}
		}
		return projectsThatRequireMigrations;
	}

    private Pair<String, WebHookProjectSettings> getProjectSettingsIfItCanBeMigrated(SProject project) {
        File projectConfigDir = project.getConfigDirectory();
        File projectPluginsConfigDir = new File(projectConfigDir,"pluginData");
        File projectPluginSettingsFile = new File(projectPluginsConfigDir, "plugin-settings.xml");
        if (projectPluginsConfigDir.isDirectory() && projectPluginsConfigDir.canWrite()) {
        	if (projectPluginSettingsFile.canRead()) {
        		try {
        			WebHookProjectSettings webhooks = ConfigLoaderUtil.getAllWebHooksInConfig(projectPluginSettingsFile);
        			if (webhooks != null && webhooks.isEnabled() && !webhooks.getWebHooksAsList().isEmpty()) {
        				return Pair.of(null, webhooks);
        			} else {
        				return Pair.of(String.format(
        						"File '%s' for project '%s' does not contain a webhooks XML element. No webhook migration will be attempted for this project.",
        						projectPluginSettingsFile.toString(),
        						project.getExternalId()), null);
        			}
        		} catch (JDOMException | IOException e) {
        		    return Pair.of(String.format(
        					"File '%s' for project '%s' is not readable as a plugin-settings file. No webhook migration will be attempted for this project. Reason: %s",
        					projectPluginSettingsFile.toString(),
        					project.getExternalId(),
        					e.getMessage()), null);
        		}
        	} else {
        	    return Pair.of(String.format(
        				"File '%s' for project '%s' does not exist. No webhook migration will be attempted for this project.",
        				projectPluginSettingsFile.toString(),
        				project.getExternalId()), null);
        	}
        } else {
        	return Pair.of(String.format(
        			"Directory '%s' for project '%s' does not exist or is not writable. No webhook migration will be attempted for this project.",
        			projectPluginsConfigDir.toString(),
        			project.getExternalId()), null);
        }
    }

	@Override
	public void requestDeferredRegistration() {
		Loggers.SERVER.info("PluginSettingsToProjectFeaturesMigrator :: Registering as a deferrable service");
		myDeferrableServiceManager.registerService(this);
	}

	@Override
	public void register() {
		ScheduledExecutorService executorService = this.executorServices.getNormalExecutorService();
		Loggers.SERVER.info("PluginSettingsToProjectFeaturesMigrator :: Scheduling migration to start in 60 seconds.");
		this.future = executorService.schedule(new PluginSettingsToProjectFeaturesMigratorScheduledTask(this), 60, TimeUnit.SECONDS);

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

    public Pair<String,List<WebHookConfig>> getCandidates(SProject myProject) {
        Pair<String, WebHookProjectSettings> settings = getProjectSettingsIfItCanBeMigrated(myProject);
        if (settings.getRight() != null) {
            return Pair.of(null, settings.getRight().getWebHooksAsList());
        }
        return Pair.of(settings.getLeft(), null);
    }
}
