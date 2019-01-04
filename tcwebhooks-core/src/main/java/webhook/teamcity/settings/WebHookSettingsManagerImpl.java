package webhook.teamcity.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildState;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookSettingsManagerImpl implements WebHookSettingsManager {
	
	@NotNull private final ProjectManager myProjectManager;
	@NotNull private final ProjectSettingsManager myProjectSettingsManager;
	private Map<String,WebHookProjectSettings> projectSettingsMap;

	
	public WebHookSettingsManagerImpl(
			@NotNull final ProjectManager projectManager,
			@NotNull final ProjectSettingsManager projectSettingsManager)
	{
		this.myProjectManager = projectManager;
		this.myProjectSettingsManager = projectSettingsManager;
		this.projectSettingsMap = rebuildProjectSettingsMap();
	}
	
	private Map<String,WebHookProjectSettings> rebuildProjectSettingsMap() {
		Map<String,WebHookProjectSettings> projectSettingsMap = new LinkedHashMap<>();
		for (SProject sProject : this.myProjectManager.getActiveProjects()) {
			projectSettingsMap.put(sProject.getProjectId(), getSettings(sProject.getProjectId()));
		}
		return projectSettingsMap;
	}
	
	@Override
	public WebHookProjectSettings getSettings(String projectInternalId) {
		return (WebHookProjectSettings) myProjectSettingsManager.getSettings(projectInternalId, WebHookListener.WEBHOOKS_SETTINGS_ATTRIBUTE_NAME);

	}

	@Override
	public WebHookUpdateResult addNewWebHook(String projectInternalId, String projectExternalId, String url,
			Boolean enabled, BuildState buildState, String format, String template, boolean buildTypeAll,
			boolean buildTypeSubProjects, Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig) {
		return getSettings(projectInternalId).addNewWebHook(
												projectInternalId, projectExternalId, url,
												enabled, buildState, format, template, buildTypeAll,
												buildTypeSubProjects, buildTypesEnabled, webHookAuthConfig
											);
		
	}

	@Override
	public WebHookUpdateResult deleteWebHook(String webHookId, String projectInternalId) {
		return getSettings(projectInternalId).deleteWebHook(webHookId, projectInternalId);
	}

	@Override
	public WebHookUpdateResult updateWebHook(String projectInternalId, String webHookId, String url, Boolean enabled,
			BuildState buildState, String format, String template, boolean buildTypeAll, boolean buildSubProjects,
			Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig) {
		return getSettings(projectInternalId).updateWebHook(
													projectInternalId, webHookId, url, enabled,
													buildState, format, template, buildTypeAll, buildSubProjects,
													buildTypesEnabled,  webHookAuthConfig
												);
	}

	@Override
	public boolean iswebHooksEnabledForProject(String projectInternalId) {
		return getSettings(projectInternalId).isEnabled();
	}

	@Override
	public List<WebHookConfig> getWebHooksConfigs(String projectInternalId) {
		return getSettings(projectInternalId).getWebHooksConfigs();
	}

}
