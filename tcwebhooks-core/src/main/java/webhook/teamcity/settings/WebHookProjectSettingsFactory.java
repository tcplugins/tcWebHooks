package webhook.teamcity.settings;

import webhook.teamcity.Loggers;
import webhook.teamcity.WebHookSettingsEventHandler;
import webhook.teamcity.WebHookSettingsEventType;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;


public class WebHookProjectSettingsFactory implements ProjectSettingsFactory {
	
	private WebHookSettingsEventHandler myWebHookSettingsEventHandler;

    public WebHookProjectSettingsFactory(ProjectSettingsManager projectSettingsManager, WebHookSettingsEventHandler webHookSettingsEventHandler){
		Loggers.SERVER.debug("WebHookProjectSettingsFactory :: Registering");
		this.myWebHookSettingsEventHandler = webHookSettingsEventHandler;
		projectSettingsManager.registerSettingsFactory("webhooks", this);
	}

	public WebHookProjectSettings createProjectSettings(String projectId) {
		Loggers.SERVER.info("WebHookProjectSettingsFactory: re-reading settings for " + projectId);
		WebHookProjectSettings whs = new WebHookProjectSettings();
		this.myWebHookSettingsEventHandler.handleEvent(WebHookSettingsEventType.PROJECT_CHANGED, projectId);
		return whs;
	}


}
