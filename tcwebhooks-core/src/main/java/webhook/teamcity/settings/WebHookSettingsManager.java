package webhook.teamcity.settings;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.BuildState;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.payload.content.ExtraParameters;

public interface WebHookSettingsManager {

	public void initialise();
	public boolean iswebHooksEnabledForProject(String projectInernalId);
	
	public List<WebHookConfig> getWebHooksConfigs(String projectInternalId);

	public WebHookUpdateResult addNewWebHook(String projectInternalId, String projectExternalId, String url,
			Boolean enabled, BuildState buildState, String template, boolean buildTypeAll,
			boolean buildTypeSubProjects, Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig,
			ExtraParameters extraParameters, boolean hideSecureValues);

	public WebHookUpdateResult deleteWebHook(String webHookId, String projectId);

	public WebHookUpdateResult updateWebHook(String projectId, String webHookId, String url, Boolean enabled,
			BuildState buildState, String template, boolean buildTypeAll, boolean buildSubProjects,
			Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig, ExtraParameters extraParameters,
											 List<WebHookFilterConfig> filters, boolean hideSecureValues);
	

	public List<WebHookSearchResult> findWebHooks(WebHookSearchFilter filter);
	
	public int getTemplateUsageCount(String templateId);
	public Map<String, List<WebHookSearchResult>> findWebHooksByProject(WebHookSearchFilter buildFilter);
	public WebHookProjectSettings getSettings(String projectInternalId);
	
	public Map<SProject, List<WebHookConfigEnhanced>> getWebHooksForProjects(List<SProject> sProjects);
	public Map<SProject, List<WebHookConfigEnhanced>> getWebHooksForBuild(List<SProject> projectPath, SBuildType buildType);
}