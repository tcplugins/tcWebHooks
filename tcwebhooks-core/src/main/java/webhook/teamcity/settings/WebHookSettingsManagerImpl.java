package webhook.teamcity.settings;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookSettingsManagerImpl implements WebHookSettingsManager {
	
	@NotNull private final ProjectManager myProjectManager;
	@NotNull private final ProjectSettingsManager myProjectSettingsManager;

	
	public WebHookSettingsManagerImpl(
			@NotNull final ProjectManager projectManager,
			@NotNull final ProjectSettingsManager projectSettingsManager)
	{
		this.myProjectManager = projectManager;
		this.myProjectSettingsManager = projectSettingsManager;
	}
	
	@Override
	public WebHookProjectSettings getSettings(String projectInternalId) {
		return (WebHookProjectSettings) myProjectSettingsManager.getSettings(projectInternalId, WebHookListener.WEBHOOKS_SETTINGS_ATTRIBUTE_NAME);

	}

}
