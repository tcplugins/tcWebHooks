package webhook.teamcity.settings;

import java.util.List;
import java.util.Map;
import java.util.Set;

import webhook.teamcity.BuildState;
import webhook.teamcity.auth.WebHookAuthConfig;

public interface WebHookSettingsManager {

	public void initialise();
	public boolean iswebHooksEnabledForProject(String projectInernalId);
	
	public List<WebHookConfig> getWebHooksConfigs(String projectInternalId);

	public WebHookUpdateResult addNewWebHook(String projectInternalId, String projectExternalId, String url,
			Boolean enabled, BuildState buildState, String format, String template, boolean buildTypeAll,
			boolean buildTypeSubProjects, Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig);

	public WebHookUpdateResult deleteWebHook(String webHookId, String projectId);

	public WebHookUpdateResult updateWebHook(String projectId, String webHookId, String url, Boolean enabled,
			BuildState buildState, String format, String template, boolean buildTypeAll, boolean buildSubProjects,
			Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig);
	

	public List<WebHookSearchResult> findWebHooks(WebHookSearchFilter filter);
	
	public int getTemplateUsageCount(String templateId, String formatShortName);
	public Map<String, List<WebHookSearchResult>> findWebHooksByProject(WebHookSearchFilter buildFilter);
	public WebHookProjectSettings getSettings(String projectInternalId);
}