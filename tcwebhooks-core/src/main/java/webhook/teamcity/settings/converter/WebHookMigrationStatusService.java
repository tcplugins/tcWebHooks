package webhook.teamcity.settings.converter;

import java.util.List;

import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.settings.WebHookConfig;

public interface WebHookMigrationStatusService {

    public boolean isMigrationCompleted();
    public String getMigrationStatus();
    public List<WebHookConfig> getUnMigratedWebHooks(SProject sProject);

}
