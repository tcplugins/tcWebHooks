package webhook.teamcity.settings.converter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import lombok.RequiredArgsConstructor;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;

@RequiredArgsConstructor
public class WebHookMigrationStatusServiceImpl implements WebHookMigrationStatusService {

    private static final String STARTED = "started";
    private static final String COMPLETED = "completed";
    private static final String DISABLED = "disabled";
    private static final String UNKNOWN = "unknown";
    private final ProjectSettingsManager myProjectSettingsManager;
    private final ProjectManager myProjectManager;
    
    @Override
    public boolean isMigrationCompleted() {
        AtomicBoolean completed = new AtomicBoolean();
        completed.set(true);
        myProjectManager.getActiveProjects().forEach(p -> {
            if (!getUnMigratedWebHooks(p).isEmpty()){
                completed.set(false);;
            }
        });
        if (completed.get()) {
            return true;
        }
        String migrationStatus = TeamCityProperties.getPropertyOrNull(PluginSettingsToProjectFeaturesMigrator.TEAMCITY_INTERNAL_PROPERTY_KEY);
        if (COMPLETED.equals(migrationStatus) || DISABLED.equals(migrationStatus)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<WebHookConfig> getUnMigratedWebHooks(SProject project) {
        WebHookProjectSettings webHookProjectSettings =  (WebHookProjectSettings) this.myProjectSettingsManager.getSettings(project.getProjectId(), "webhooks");
        return webHookProjectSettings.getWebHooksConfigs();
    }

    @Override
    public String getMigrationStatus() {
        switch (TeamCityProperties.getPropertyOrNull(PluginSettingsToProjectFeaturesMigrator.TEAMCITY_INTERNAL_PROPERTY_KEY)) {
            case STARTED:
                return STARTED;
            case COMPLETED:
                return COMPLETED;
            case DISABLED:
                return DISABLED;
            default:
                return UNKNOWN;
        }
    }

}
