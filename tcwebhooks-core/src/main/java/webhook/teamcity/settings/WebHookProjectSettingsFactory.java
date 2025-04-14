package webhook.teamcity.settings;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

public class WebHookProjectSettingsFactory implements ProjectSettingsFactory {
	private static final Logger LOG = Logger.getInstance(WebHookProjectSettingsFactory.class.getName());

    public WebHookProjectSettingsFactory(ProjectSettingsManager projectSettingsManager){
		LOG.debug("WebHookProjectSettingsFactory :: Registering");
		projectSettingsManager.registerSettingsFactory("webhooks", this);
	}

	public WebHookProjectSettings createProjectSettings(String projectId) {
		LOG.info("WebHookProjectSettingsFactory: re-reading settings for " + projectId);
		return new WebHookProjectSettings();
	}


}
