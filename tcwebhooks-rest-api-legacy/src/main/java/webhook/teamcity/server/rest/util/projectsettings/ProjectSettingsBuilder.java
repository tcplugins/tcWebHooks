package webhook.teamcity.server.rest.util.projectsettings;

import webhook.teamcity.server.rest.model.webhook.ProjectWebhook;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;

public class ProjectSettingsBuilder {
	
	private WebHookProjectSettings webhookProjectSettings;

	public ProjectSettingsBuilder(WebHookProjectSettings settings) {
		this.webhookProjectSettings = settings;
	}
	
	public ProjectWebhooks build(){
		ProjectWebhooks projectWebhooks = new ProjectWebhooks();
		projectWebhooks.setEnabled(this.webhookProjectSettings.isEnabled());
		for (WebHookConfig config : webhookProjectSettings.getWebHooksConfigs()){
			projectWebhooks.addWebhook(new ProjectWebhook(config));
		}
		return projectWebhooks;
	}

}
