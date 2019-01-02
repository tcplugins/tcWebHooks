package webhook.teamcity.settings;

import webhook.teamcity.settings.WebHookProjectSettings;

public interface WebHookSettingsManager {

	WebHookProjectSettings getSettings(String projectInternalId);
	

}