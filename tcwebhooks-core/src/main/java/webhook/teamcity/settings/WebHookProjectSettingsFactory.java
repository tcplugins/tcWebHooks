package webhook.teamcity.settings;

import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.Loggers;

public class WebHookProjectSettingsFactory implements ProjectSettingsFactory {
	
    public WebHookProjectSettingsFactory(ProjectSettingsManager projectSettingsManager){
		Loggers.SERVER.debug("WebHookProjectSettingsFactory :: Registering");
		projectSettingsManager.registerSettingsFactory("webhooks", this);
	}

	public WebHookProjectSettings createProjectSettings(String projectId) {
		Loggers.SERVER.info("WebHookProjectSettingsFactory: re-reading settings for " + projectId);
		return new WebHookProjectSettings();
	}


}
