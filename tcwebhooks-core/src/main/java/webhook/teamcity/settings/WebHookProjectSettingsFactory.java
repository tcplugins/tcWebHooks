package webhook.teamcity.settings;

import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.Loggers;


public class WebHookProjectSettingsFactory implements ProjectSettingsFactory {

    public WebHookProjectSettingsFactory(ProjectSettingsManager projectSettingsManager) {
        Loggers.SERVER.info("WebHookProjectSettingsFactory :: Registering");
        projectSettingsManager.registerSettingsFactory("webhooks", this);
    }

    public WebHookProjectSettings createProjectSettings(String projectId) {
        Loggers.SERVER.info("WebHookProjectSettingsFactory: re-reading settings for " + projectId);
        WebHookProjectSettings whs = new WebHookProjectSettings();
        return whs;
    }


}
