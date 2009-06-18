package webhook.teamcity.settings;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;

public class WebHookProjectSettingsFactory implements ProjectSettingsFactory {
	//WebHookProjectSettings whs;
	
	public WebHookProjectSettingsFactory(ProjectSettingsManager projectSettingsManager){
	//WebHookProjectSettingsFactory(){
		//whs = new WebHookProjectSettings();
		Loggers.SERVER.info("WebHookProjectSettingsFactory :: Registering");
		projectSettingsManager.registerSettingsFactory("webhooks", this);
	}

	public WebHookProjectSettings createProjectSettings(String projectId) {
		Loggers.SERVER.info("WebHookProjectSettingsFactory::createProjectSettings : " + projectId);
		WebHookProjectSettings whs = new WebHookProjectSettings();
		return whs;
	}


}
