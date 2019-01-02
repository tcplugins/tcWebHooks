package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildState;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.settings.WebHookSearchResult.Match;

public class WebHookSettingsManagerImpl implements WebHookSettingsManager {

	@NotNull private final ProjectManager myProjectManager;
	@NotNull private final ProjectSettingsManager myProjectSettingsManager;
	@NotNull private final WebHookTemplateManager myWebHookTemplateManager;
	@NotNull private final WebHookPayloadManager myWebHookPayloadManager;
	@NotNull private final WebAddressTransformer myWebAddressTransformer;

	/** A Map of <code>projectInternId</code> to {@link WebHookProjectSettings} */
	private Map<String, WebHookProjectSettings> projectSettingsMap;

	/** A Map of webhook uniqueKey to {@link WebHookConfigEnhanced} */
	private Map<String, WebHookConfigEnhanced> webhooksEnhanced = new LinkedHashMap<>();


	public WebHookSettingsManagerImpl(
			@NotNull final ProjectManager projectManager,
			@NotNull final ProjectSettingsManager projectSettingsManager,
			@NotNull final WebHookTemplateManager webHookTemplateManager,
			@NotNull final WebHookPayloadManager webHookPayloadManager,
			@NotNull final WebAddressTransformer webAddressTransformer)
	{
		this.myProjectManager = projectManager;
		this.myProjectSettingsManager = projectSettingsManager;
		this.myWebHookTemplateManager = webHookTemplateManager;
		this.myWebHookPayloadManager = webHookPayloadManager;
		this.myWebAddressTransformer = webAddressTransformer;
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
		Map<String,WebHookProjectSettings> projectSettings = new LinkedHashMap<>();
		for (SProject sProject : this.myProjectManager.getActiveProjects()) {
			projectSettings.put(sProject.getProjectId(), getSettings(sProject.getProjectId()));
		}
		return projectSettings;
	}

	@Override
	public WebHookProjectSettings getSettings(String projectInternalId) {
		return (WebHookProjectSettings) myProjectSettingsManager.getSettings(projectInternalId, WebHookListener.WEBHOOKS_SETTINGS_ATTRIBUTE_NAME);

	}

	@Override
	public WebHookUpdateResult addNewWebHook(String projectInternalId, String projectExternalId, String url,
			Boolean enabled, BuildState buildState, String template, boolean buildTypeAll,
			boolean buildTypeSubProjects, Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig) {
		WebHookUpdateResult result = getSettings(projectInternalId).addNewWebHook(
												projectInternalId, projectExternalId, url,
												enabled, buildState, template, buildTypeAll,
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
			BuildState buildState, String template, boolean buildTypeAll, boolean buildSubProjects,
			Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig) {
		WebHookUpdateResult result = getSettings(projectInternalId).updateWebHook(
													projectInternalId, webHookId, url, enabled,
													buildState, template, buildTypeAll, buildSubProjects,
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
	public int getTemplateUsageCount(String templateId) {
		int count = 0;
		for (WebHookConfigEnhanced e : this.webhooksEnhanced.values()) {
			if (templateId.equalsIgnoreCase(e.getTemplateId())) {
				count++;
			}
		}
		return count;
	}

	@Override
	public List<WebHookSearchResult> findWebHooks(WebHookSearchFilter filter) {
		List<WebHookSearchResult> webhookResultList = new ArrayList<>();
		for (WebHookConfigEnhanced e : this.webhooksEnhanced.values()) {
			addMatchingWebHook(filter, webhookResultList, e);
		}
		return webhookResultList;
	}

	@Override
	public Map<String, List<WebHookSearchResult>> findWebHooksByProject(WebHookSearchFilter filter) {
		Map<String,List<WebHookSearchResult>> projectGroupedResults = new LinkedHashMap<>();
		for (Map.Entry<String, WebHookProjectSettings> entry : this.projectSettingsMap.entrySet()) {
			List<WebHookSearchResult> webhookResultList = new ArrayList<>();
			for (WebHookConfig c : entry.getValue().getWebHooksConfigs()) {
				addMatchingWebHook(filter, webhookResultList, this.webhooksEnhanced.get(c.getUniqueKey()));
			}
			if ( !webhookResultList.isEmpty()) {
				projectGroupedResults.put(entry.getKey(), webhookResultList);
			}
		}
		return projectGroupedResults;
	}

	/** 
	 * Perform the search and add any matching records to the <code>webhookResultList</code>.
	 * Modifies <code>webhookResultList</code>
	 * @param filter
	 * @param webhookResultList
	 * @param webHookConfigEnhanced
	 */
	private void addMatchingWebHook(WebHookSearchFilter filter, List<WebHookSearchResult> webhookResultList, WebHookConfigEnhanced e) {
		WebHookSearchResult result = new WebHookSearchResult();
		if (filter.getShow() != null && filter.getShow().equalsIgnoreCase("all")) {
			result.addMatch(Match.SHOW);
		} else {
		
			doTextSearch(filter, e, result);
			doPayloadFormatSearch(filter, e, result);
			
			doTemplateIdSearch(filter, e, result);
			doUrlSearch(filter, e, result);
			doProjectIdSearch(filter, e, result);
			doWebHookIdSearch(filter, e, result);
			doTagsSearch(filter, e, result);
			doBuildTypeIdSearch(filter, e, result);
		}

		if ( ! result.getMatches().isEmpty() ) {
			result.setWebHookConfigEnhanced(e);
			webhookResultList.add(result);
		}
	}

	/**
	 * Checks if:
	 * <ul><li>sBuildType is explicitly listed as a build in the webhook config (by its internal id),
	 * <li>or, sBuildType is in the same project as the webhook, and project has all builds in project enabled
	 * <li>or, sBuildType is in a sub-project of the webhook project, and the webhook has sub-projects enabled.</ul>
	 * @param filter
	 * @param e
	 * @param result
	 */
	private void doBuildTypeIdSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e, WebHookSearchResult result) {
		if (filter.getBuildTypeExternalId() != null && !filter.getBuildTypeExternalId().isEmpty()) {
			SBuildType myBuildType = myProjectManager.findBuildTypeByExternalId(filter.getBuildTypeExternalId());
			if (myBuildType != null &&
				(	
					(
						myBuildType.getProjectExternalId().equalsIgnoreCase(e.getProjectExternalId())
					  && e.getWebHookConfig().isEnabledForBuildType(myBuildType)
					)
				|| 	(
						e.getWebHookConfig().isEnabledForSubProjects() 
					  && myProjectManager.findProjectByExternalId(e.getProjectExternalId()).getProjects().contains(myBuildType.getProject())
					)
				)
			)
			{
				result.addMatch(Match.BUILD_TYPE);
			}
		}
	}

	private void doTagsSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e, WebHookSearchResult result) {
		if (!filter.getTags().isEmpty()) {
			for (String tag: e.getTags()) {
				if (filter.getTags().contains(tag.toLowerCase())) {
					result.addMatch(Match.TAG);
				}
			}
		}
	}

	private void doWebHookIdSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e, WebHookSearchResult result) {
		if (filter.getWebhookId() != null) {
			matchField(result, filter.getWebhookId(), Match.ID, e.getWebHookConfig().getUniqueKey());
		}
	}

	private void doProjectIdSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e, WebHookSearchResult result) {
		if (filter.getProjectExternalId() != null) {
			matchField(result, filter.getProjectExternalId(), Match.PROJECT, e.getProjectExternalId());
		}
	}

	private void doUrlSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e, WebHookSearchResult result) {
		if (filter.getUrlSubString() != null) {
			searchField(result, filter.getUrlSubString(), Match.URL, e.getWebHookConfig().getUrl());
		}
	}

	private void doTemplateIdSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e, WebHookSearchResult result) {
		if (filter.getTemplateId() != null) {
			matchField(result, filter.getTemplateId(), Match.TEMPLATE, e.getTemplateId());
		}
	}

	private void doPayloadFormatSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e,
			WebHookSearchResult result) {
		if (filter.getFormatShortName() != null) {
			matchField(result, filter.getFormatShortName(), Match.PAYLOAD_FORMAT, e.getPayloadFormat());
		}
	}

	private void doTextSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e, WebHookSearchResult result) {
		if (filter.getTextSearch() != null) {
			searchField(result, filter.getTextSearch(), Match.PAYLOAD_FORMAT, e.getPayloadFormat(), e.getPayloadFormatDescription());
			searchField(result, filter.getTextSearch(), Match.TEMPLATE, e.getTemplateId(), e.getTemplateDescription());
			searchField(result, filter.getTextSearch(), Match.URL, e.getWebHookConfig().getUrl());
			searchField(result, filter.getTextSearch(), Match.PROJECT, e.getProjectExternalId());
		}

		if (filter.textSearch != null && e.getTags().contains(filter.textSearch.toLowerCase())) {
			result.addMatch(Match.TAG);
		}
	}

	private void matchField(WebHookSearchResult result, String searchString, Match matchType, String...fields) {
		if (searchString != null) {
			for (String field : fields) {
				if (field.equalsIgnoreCase(searchString)) {
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
		if (Objects.nonNull(sProject)) {
			Loggers.SERVER.debug("WebHookSettingsManagerImpl :: rebuilding webhook cache for project: " + sProject.getExternalId() + " '" + sProject.getName() + "'");
			for (WebHookConfig c : getWebHooksConfigs(projectInternalId)) {
				String templateName = "Missing template";
				String templateFormat = "";
				String templateFormatDescription = "Unknown payload format";
				try {
					WebHookPayloadTemplate template = this.myWebHookTemplateManager.getTemplate(c.getPayloadTemplate());
					WebHookPayload format = this.myWebHookPayloadManager.getFormat(this.myWebHookTemplateManager.getTemplateConfig(template.getTemplateId(), TemplateState.BEST).getFormat());
					templateName = template.getTemplateDescription();
					templateFormat = format.getFormatShortName();
					templateFormatDescription = format.getFormatDescription();
				} catch (NullPointerException ex) {
					Loggers.SERVER.warn(String.format(
							"WebHookSettingsManagerImpl :: Template Not Found: Webhook '%s' from Project '%s' refers to template '%s', which was not found. WebHook URL is: %s",
							c.getUniqueKey(),
							sProject.getExternalId(),
							c.getPayloadTemplate(),
							c.getUrl()));
				}

				WebHookConfigEnhanced configEnhanced = WebHookConfigEnhanced.builder()
						.payloadFormat(templateFormat)
						.payloadFormatDescription(templateFormatDescription)
						.projectExternalId(sProject.getExternalId())
						.templateId(c.getPayloadTemplate())
						.templateDescription(templateName)
						.webHookConfig(c)
						.generalisedWebAddress(myWebAddressTransformer.getGeneralisedHostName(c.getUrl()))
						.build();
				configEnhanced.addTag(templateFormat)
						.addTag(c.getEnabled() ? "enabled" : "disabled")
						.addTag(c.getPayloadTemplate())
						.addTag(configEnhanced.getGeneralisedWebAddress().getGeneralisedAddress());
				if (c.getAuthenticationConfig() != null) {
					configEnhanced.addTag("authenticated").addTag(c.getAuthenticationConfig().getType());
				}
				addTagIfPresent(configEnhanced, c.getHeaders(), "headers");
				addTagIfPresent(configEnhanced, c.getTriggerFilters(), "filters");
				addTagIfPresent(configEnhanced, c.getParams(), "parameters");

				this.webhooksEnhanced.put(c.getUniqueKey(), configEnhanced);
				Loggers.SERVER.debug("WebHookSettingsManagerImpl :: updating webhook: '" + c.getUniqueKey() + "' " + configEnhanced.toString());
			}
		} else {
			Loggers.SERVER.debug("WebHookSettingsManagerImpl :: NOT rebuilding webhook cache. Project not found: " + projectInternalId);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void addTagIfPresent(WebHookConfigEnhanced config, Collection collection, String tagName) {
		if (collection != null && !collection.isEmpty()) {
			config.addTag(tagName);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void addTagIfPresent(WebHookConfigEnhanced config, Map map, String tagName) {
		if (map != null && !map.isEmpty()) {
			config.addTag(tagName);
		}
	}

}
