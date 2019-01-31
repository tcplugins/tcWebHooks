package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildState;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSearchResult.Match;

public class WebHookSettingsManagerImpl implements WebHookSettingsManager {
	
	@NotNull private final ProjectManager myProjectManager;
	@NotNull private final ProjectSettingsManager myProjectSettingsManager;
	@NotNull private final WebHookTemplateManager myWebHookTemplateManager;
	@NotNull private final WebHookPayloadManager myWebHookPayloadManager;
	private Map<String, WebHookProjectSettings> projectSettingsMap;
	private Map<String, WebHookConfigEnhanced> webhooksEnhanced = new LinkedHashMap<>();

	
	public WebHookSettingsManagerImpl(
			@NotNull final ProjectManager projectManager,
			@NotNull final ProjectSettingsManager projectSettingsManager,
			@NotNull final WebHookTemplateManager webHookTemplateManager,
			@NotNull final WebHookPayloadManager webHookPayloadManager)
	{
		this.myProjectManager = projectManager;
		this.myProjectSettingsManager = projectSettingsManager;
		this.myWebHookTemplateManager = webHookTemplateManager;
		this.myWebHookPayloadManager = webHookPayloadManager;
	}
	
	@Override
	public void initialise() {
		if (this.projectSettingsMap == null) {
			this.projectSettingsMap = rebuildProjectSettingsMap();
			for (String projectId : this.projectSettingsMap.keySet()) {
				this.rebuildWebHooksEnhanced(projectId);
			}
		}
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
		WebHookUpdateResult result = getSettings(projectInternalId).addNewWebHook(
												projectInternalId, projectExternalId, url,
												enabled, buildState, format, template, buildTypeAll,
												buildTypeSubProjects, buildTypesEnabled, webHookAuthConfig
											);
		if (result.updated) {
			rebuildWebHooksEnhanced(projectInternalId);
		}
		return result;
		
	}

	@Override
	public WebHookUpdateResult deleteWebHook(String webHookId, String projectInternalId) {
		WebHookUpdateResult result = getSettings(projectInternalId).deleteWebHook(webHookId, projectInternalId);
		if (result.updated) {
			rebuildWebHooksEnhanced(projectInternalId);
		}
		return result;

	}

	@Override
	public WebHookUpdateResult updateWebHook(String projectInternalId, String webHookId, String url, Boolean enabled,
			BuildState buildState, String format, String template, boolean buildTypeAll, boolean buildSubProjects,
			Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig) {
		WebHookUpdateResult result = getSettings(projectInternalId).updateWebHook(
													projectInternalId, webHookId, url, enabled,
													buildState, format, template, buildTypeAll, buildSubProjects,
													buildTypesEnabled,  webHookAuthConfig
												);
		if (result.updated) {
			rebuildWebHooksEnhanced(projectInternalId);
		}
		return result;
	}

	@Override
	public boolean iswebHooksEnabledForProject(String projectInternalId) {
		return getSettings(projectInternalId).isEnabled();
	}

	@Override
	public List<WebHookConfig> getWebHooksConfigs(String projectInternalId) {
		List<WebHookConfig> webHookConfigCopies = new ArrayList<>();
		for (WebHookConfig c : getSettings(projectInternalId).getWebHooksConfigs()) {
			webHookConfigCopies.add(c.copy());
		}
		return webHookConfigCopies;
	}

	@Override
	public List<WebHookSearchResult> findWebHooks(WebHookSearchFilter filter) {
		List<WebHookSearchResult> webhookResultList = new ArrayList<>();
		for (WebHookConfigEnhanced e : this.webhooksEnhanced.values()) {
			WebHookSearchResult result = new WebHookSearchResult();
			if (filter.getTextSearch() != null) {
				searchField(result, filter.getTextSearch(), Match.PAYLOAD_FORMAT, e.getPayloadFormat(), e.getPayloadFormatDescription());
				searchField(result, filter.getTextSearch(), Match.TEMPLATE, e.getTemplateId(), e.getTemplateDescription());
				searchField(result, filter.getTextSearch(), Match.URL, e.getWebHookConfig().getUrl());
			}
			
			if (filter.textSearch != null && e.getTags().contains(filter.textSearch.toLowerCase())) {
				result.addMatch(Match.TAG);
			}
			
			if (filter.getTemplateId() != null) {
				matchField(result, filter.getTemplateId(), Match.TEMPLATE, e.getTemplateId());
			}
			
			if ( ! result.getMatches().isEmpty() ) {
				result.setWebHookConfigEnhanced(e);
				webhookResultList.add(result);
			}
		}
		
		return webhookResultList;
	}
	
	private void matchField(WebHookSearchResult result, String searchString, Match matchType, String...fields) {
		if (searchString != null) {
			for (String field : fields) {
				if (field.toLowerCase().equals(searchString.toLowerCase())) {
					result.addMatch(matchType);
				}
			}
		}
	}
	
	private void searchField(WebHookSearchResult result, String searchString, Match matchType, String...fields) {
		if (searchString != null) {
			for (String field : fields) {
				if (field.toLowerCase().contains(searchString.toLowerCase())) {
					result.addMatch(matchType);
				}
			}
		}
	}
	
	private void rebuildWebHooksEnhanced(String projectInternalId) {
		SProject sProject = myProjectManager.findProjectById(projectInternalId);
		Loggers.SERVER.debug("WebHookSettingsManagerImpl :: rebuilding webhook cache for project: " + sProject.getExternalId() + " '" + sProject.getName() + "'");
		for (WebHookConfig c: getWebHooksConfigs(projectInternalId)) {
			String templateName = "missing template";
			try {
				templateName = this.myWebHookTemplateManager.getTemplate(c.getPayloadTemplate()).getTemplateDescription();
			} catch (NullPointerException ex) {
				Loggers.SERVER.warn(String.format(
						"WebHookSettingsManagerImpl :: Template Not Found: Webhook '%s' from Project '%s' refers to template '%s', which was not found. WebHook URL is: %s", 
						c.getUniqueKey(),
						sProject.getExternalId(),
						c.getPayloadTemplate(),
						c.getUrl()));
			}
			
			WebHookConfigEnhanced configEnhanced = WebHookConfigEnhanced.builder()
														   .payloadFormat(c.getPayloadFormat())
														   .payloadFormatDescription(this.myWebHookPayloadManager.getFormat(c.getPayloadFormat()).getFormatDescription())
														   .projectExternalId(sProject.getExternalId())
														   .templateId(c.getPayloadTemplate())
														   .templateDescription(templateName)
														   .webHookConfig(c)
														   .build();
			configEnhanced.addTag(c.getPayloadFormat())
			 			  .addTag(sProject.getExternalId())
			 			  .addTag(c.getPayloadTemplate());
			
			this.webhooksEnhanced.put(c.getUniqueKey(), configEnhanced);
			Loggers.SERVER.debug("WebHookSettingsManagerImpl :: updating webhook: '" + c.getUniqueKey() + "' " + configEnhanced.toString());
		}
	}

	@Override
	public int getTemplateUsageCount(String templateId, String formatShortName) {
		int count = 0;
		for (WebHookConfigEnhanced e : this.webhooksEnhanced.values()) {
			if (templateId.equalsIgnoreCase(e.getTemplateId())
					&& formatShortName.equalsIgnoreCase(e.getPayloadFormat())) {
				count++;
			}
		}
		return count;
	}
	
}
