package webhook.teamcity.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.BuildTypeIdentity;
import jetbrains.buildServer.serverSide.ConfigAction;
import jetbrains.buildServer.serverSide.ConfigActionFactory;
import jetbrains.buildServer.serverSide.PersistFailedException;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.WebHookSettingsEventHandler.WebHookSettingsEvent;
import webhook.teamcity.WebHookSettingsEventType;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.WebHookConfigEnhanced.Tag;
import webhook.teamcity.settings.WebHookConfigEnhanced.TagType;
import webhook.teamcity.settings.WebHookSearchResult.Match;

public class WebHookSettingsManagerImpl implements WebHookSettingsManager, WebHookSecureValuesEnquirer {
	private static final Logger LOG = Logger.getInstance(WebHookSettingsManagerImpl.class.getName());

	@NotNull private final ProjectManager myProjectManager;
	@NotNull private final ConfigActionFactory myConfigActionFactory;
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
			@NotNull final ConfigActionFactory configActionFactory,
			@NotNull final ProjectSettingsManager projectSettingsManager,
			@NotNull final WebHookTemplateManager webHookTemplateManager,
			@NotNull final WebHookPayloadManager webHookPayloadManager,
			@NotNull final WebAddressTransformer webAddressTransformer)
	{
		this.myProjectManager = projectManager;
		this.myConfigActionFactory = configActionFactory;
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
		WebHookProjectSettings webHookProjectSettings = (WebHookProjectSettings) myProjectSettingsManager.getSettings(projectInternalId, WebHookListener.WEBHOOKS_SETTINGS_ATTRIBUTE_NAME);
		for (WebHookConfig whc : webHookProjectSettings.getWebHooksConfigs()) {
			if (whc.getProjectInternalId() == null) {
				whc.setProjectInternalId(projectInternalId);
			}
		}
		return webHookProjectSettings;
	}

	@Override
	public WebHookUpdateResult addNewWebHook(String projectInternalId, String projectExternalId, String url,
			Boolean enabled, BuildState buildState, String template, boolean buildTypeAll,
			boolean buildTypeSubProjects, Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig,
			ExtraParameters extraParameters, List<WebHookFilterConfig> filters, List<WebHookHeaderConfig> headers, boolean hideSecureValues) {
		WebHookUpdateResult result = getSettings(projectInternalId).addNewWebHook(
												projectInternalId, projectExternalId, url,
												enabled, buildState, template, buildTypeAll,
												buildTypeSubProjects, buildTypesEnabled, webHookAuthConfig, 
												extraParameters,
												filters, headers, hideSecureValues
											);
		if (result.updated) {
			if (persist(projectInternalId, "Added new WebHook")) {
				result.updated = true;
				rebuildWebHooksEnhanced(projectInternalId);
			} else {
				result.updated = false;
			}
		}
		return result;
	}
	
	@Override
	public WebHookUpdateResult addNewWebHook(String projectInternalId, WebHookConfig config) {
		WebHookUpdateResult result = getSettings(projectInternalId).addNewWebHook(config);
		if (result.updated) {
			if (persist(projectInternalId, "Added new WebHook")) {
				result.updated = true;
				rebuildWebHooksEnhanced(projectInternalId);
			} else {
				result.updated = false;
			}
		}
		return result;
	}

	@Override
	public WebHookUpdateResult deleteWebHook(String webHookId, String projectInternalId) {
		WebHookUpdateResult result = getSettings(projectInternalId).deleteWebHook(webHookId, projectInternalId);
		if (result.updated) {
			if (persist(projectInternalId, "Deleted existing WebHook")) {
				result.updated = true;
				this.webhooksEnhanced.remove(webHookId);
				rebuildWebHooksEnhanced(projectInternalId);
			} else {
				result.updated = false;
			}
		}
		return result;
	}
	
	@Override
	public WebHookUpdateResult deleteWebHook(String projectInternalId, WebHookConfig config) {
		WebHookUpdateResult result = getSettings(projectInternalId).deleteWebHook(config.getUniqueKey(), projectInternalId);
		if (result.updated) {
			if (persist(projectInternalId, "Deleted existing WebHook")) {
				result.updated = true;
				this.webhooksEnhanced.remove(config.getUniqueKey());
				rebuildWebHooksEnhanced(projectInternalId);
			} else {
				result.updated = false;
			}
		}
		return result;
	}
	
	

	@Override
	public WebHookUpdateResult updateWebHook(String projectInternalId, WebHookConfig config) {
		WebHookUpdateResult result = getSettings(projectInternalId).updateWebHook(config);
		if (result.updated) {
			if (persist(projectInternalId, "Edited existing WebHook")) {
				result.updated = true;
				rebuildWebHooksEnhanced(projectInternalId);
			} else {
				result.updated = false;
			}
		}
		return result;
	}
	
	@Override
	public WebHookUpdateResult updateWebHook(String projectInternalId, String webHookId, String url, Boolean enabled,
			BuildState buildState, String template, boolean buildTypeAll, boolean buildSubProjects,
			Set<String> buildTypesEnabled, WebHookAuthConfig webHookAuthConfig, ExtraParameters extraParameters, 
			List<WebHookFilterConfig> filters, List<WebHookHeaderConfig> headers, boolean hideSecureValues) {
		WebHookUpdateResult result = getSettings(projectInternalId).updateWebHook(
				projectInternalId, webHookId, url, enabled,
				buildState, template, buildTypeAll, buildSubProjects,
				buildTypesEnabled,  webHookAuthConfig, extraParameters,
				filters, headers, hideSecureValues
				);
		if (result.updated) {
			if (persist(projectInternalId, "Edited existing WebHook")) {
				result.updated = true;
				rebuildWebHooksEnhanced(projectInternalId);
			} else {
				result.updated = false;
			}
		}
		return result;
	}
	
	private boolean persist(String projectInternalId, String message) {
		try {
			SProject project = this.myProjectManager.findProjectById(projectInternalId);
			ConfigAction cause = myConfigActionFactory.createAction(project, message);
			project.persist(cause);
			return true;
		} catch (AccessDeniedException | PersistFailedException ex) {
			LOG.warn("WebHookSettingsManagerImpl :: Failed to persist webhook in projectId: " + projectInternalId, ex);
			return false;
		}
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
		LOG.debug(String.format("WebHookSettingsManagerImpl :: getWebHooksConfigs. ProjectId '%s'. Found %s webhook config(s).", projectInternalId, webHookConfigCopies.size()));
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
	
	@Override
	public List<WebHookConfigEnhanced> getWebHooksForProject(SProject project) {
		List<WebHookConfigEnhanced> webhookConfigs = new ArrayList<>();
		for (WebHookConfigEnhanced wh : this.webhooksEnhanced.values()) {
			if (project.getExternalId().equals(wh.getProjectExternalId())) {
				webhookConfigs.add(wh);
			}
		}
		return webhookConfigs;
	}
	
	@Override
	public Map<SProject, List<WebHookConfigEnhanced>> getWebHooksForProjects(List<SProject> sProjects) {
		Map<SProject,List<WebHookConfigEnhanced>> projectGroupedResults = new LinkedHashMap<>();
		for (SProject project : sProjects) {
			List<WebHookConfigEnhanced> webhookConfigs = new ArrayList<>();
			for (WebHookConfigEnhanced wh : this.webhooksEnhanced.values()) {
				if (project.getExternalId().equals(wh.getProjectExternalId())) {
					webhookConfigs.add(wh);
				}
			}
			projectGroupedResults.put(project, webhookConfigs);
		}
		return projectGroupedResults;
	}
	
	public Map<SProject, List<WebHookConfigEnhanced>> getWebHooksForBuild(List<SProject> sProjects, SBuildType sBuildType) {
		Map<SProject,List<WebHookConfigEnhanced>> projectGroupedResults = new LinkedHashMap<>();
		for (SProject project : sProjects) {
			List<WebHookConfigEnhanced> webhookConfigs = new ArrayList<>();
			for (WebHookConfigEnhanced wh : this.webhooksEnhanced.values()) {
				if (project.getExternalId().equals(wh.getProjectExternalId()) && wh.getWebHookConfig().isSpecificBuildTypeEnabled(sBuildType)) {
					webhookConfigs.add(wh);
				}
			}
			projectGroupedResults.put(project, webhookConfigs);
		}
		return projectGroupedResults;
	}
	
    @Override
    public void handleProjectChangedEvent(WebHookSettingsEvent event) {
        if (WebHookSettingsEventType.PROJECT_CHANGED.equals(event.getEventType()) || WebHookSettingsEventType.PROJECT_PERSISTED.equals(event.getEventType())) {
            if (!this.projectSettingsMap.containsKey(event.getProjectInternalId())) {
                SProject sProject = this.myProjectManager.findProjectById(event.getProjectInternalId());
                if (sProject != null && !sProject.isArchived()) {
                    this.projectSettingsMap.put(sProject.getProjectId(), getSettings(sProject.getProjectId()));
                }
                this.rebuildWebHooksEnhanced(event.getProjectInternalId());
            }
        } else if (WebHookSettingsEventType.BUILD_TYPE_DELETED.equals(event.getEventType())) {
            SBuildType sBuildType = (SBuildType) event.getBaggage();
            if (sBuildType != null && this.projectSettingsMap.containsKey(sBuildType.getProjectId())) {
                handleBuildTypeDeleteEvent(sBuildType);
                this.rebuildWebHooksEnhanced(sBuildType.getProjectId());
            }
        }
    }
    

    private void handleBuildTypeDeleteEvent(SBuildType sBuildType) {
        for (WebHookSearchResult w : this.findWebHooks(WebHookSearchFilter.builder().buildTypeExternalId(sBuildType.getExternalId()).build())) {
            if (w.getWebHookConfig().isSpecificBuildTypeEnabled(sBuildType)) {
                WebHookConfig c = w.getWebHookConfig().copy();
                c.getEnabledBuildTypesSet().remove(sBuildType.getInternalId());
                this.updateWebHook(sBuildType.getProjectId(), c);
                LOG.info(String.format("WebHookSettingsManagerImpl :: Handling SBuildType deleted event for webhook. Build removed from WebHook Configuation. BuildType: '%s (%s)', webHookId: '%s'", sBuildType.getExternalId(), sBuildType.getInternalId(), c.getUniqueKey()));
            }
        }
    }

    @Override
    public void removeAllWebHooksFromCacheForProject(String projectInternalId) {
        WebHookProjectSettings p = this.projectSettingsMap.remove(projectInternalId);
        if (p != null) {
            LOG.info(String.format("Removed project from webhook cache: projectId: '%s'", projectInternalId));
        }
        List<String> keysForDeletion = new ArrayList<>();
        for (Map.Entry<String,WebHookConfigEnhanced> e : this.webhooksEnhanced.entrySet()) {
            if (projectInternalId.equals(e.getValue().getProjectInternalId())) {
                keysForDeletion.add(e.getKey());
            }
        }
        for (String k : keysForDeletion) {
            WebHookConfigEnhanced w = this.webhooksEnhanced.remove(k);
            if (w != null) {
                LOG.info(String.format("Removed webhook from webhook cache: projectId: '%s', webHookId: '%s'", w.getProjectInternalId(), w.getWebHookConfig().getUniqueKey()));
            }
        }
        LOG.info("Removed webhooks for project from cache with project id: " + projectInternalId);
    }

	/** 
	 * Perform the search and add any matching records to the <code>webhookResultList</code>.
	 * Modifies <code>webhookResultList</code>
	 * @param filter
	 * @param webhookResultList
	 * @param webHookConfigEnhanced
	 */
	private void addMatchingWebHook(WebHookSearchFilter filter, List<WebHookSearchResult> webhookResultList, WebHookConfigEnhanced webHookConfigEnhanced) {
		WebHookSearchResult result = new WebHookSearchResult();
		if (filter.getShow() != null && filter.getShow().equalsIgnoreCase("all")) {
			result.addMatch(Match.SHOW);
		} else {
		
			doTextSearch(filter, webHookConfigEnhanced, result);
			doPayloadFormatSearch(filter, webHookConfigEnhanced, result);
			
			doTemplateIdSearch(filter, webHookConfigEnhanced, result);
			doUrlSearch(filter, webHookConfigEnhanced, result);
			doProjectIdSearch(filter, webHookConfigEnhanced, result);
			doWebHookIdSearch(filter, webHookConfigEnhanced, result);
			doTagsSearch(filter, webHookConfigEnhanced, result);
			doBuildTypeIdSearch(filter, webHookConfigEnhanced, result);
		}

		if ( ! result.getMatches().isEmpty() ) {
			result.setWebHookConfigEnhanced(webHookConfigEnhanced);
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
			try {
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
			} catch (AccessDeniedException ex) {
				result.setFilteredResult(true);
			}
		}
	}

	private void doTagsSearch(WebHookSearchFilter filter, WebHookConfigEnhanced e, WebHookSearchResult result) {
		if (!filter.getTags().isEmpty()) {
			for (Tag tag: e.getTags()) {
				if (filter.getTags().contains(tag.getName().toLowerCase())) {
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

		if (filter.textSearch != null && e.getTagNames().contains(filter.textSearch.toLowerCase())) {
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
		    boolean persistRequired = false;
		    WebHookProjectSettings webHookProjectSettings = getSettings(projectInternalId);
			LOG.debug("WebHookSettingsManagerImpl :: rebuilding webhook cache for project: " + sProject.getExternalId() + " '" + sProject.getName() + "'");
			for (WebHookConfig whc : getWebHooksConfigs(projectInternalId)) {
			    WebHookConfig c = checkForConflictingUniqueId(whc, projectInternalId);
				String templateName = "Missing template";
				String templateFormat = "";
				String templateFormatDescription = "Unknown payload format";
				Set<BuildStateEnum> enabledBuildStates = null;
				try {
					WebHookPayloadTemplate template = this.myWebHookTemplateManager.getTemplate(c.getPayloadTemplate());
					WebHookPayload format = this.myWebHookPayloadManager.getFormat(this.myWebHookTemplateManager.getTemplateConfig(template.getTemplateId(), TemplateState.BEST).getFormat());
					templateName = template.getTemplateDescription();
					templateFormat = format.getFormatShortName();
					templateFormatDescription = format.getFormatDescription();
					enabledBuildStates = determineEnabledBuildStates(c, template);
				} catch (NullPointerException ex) {
					LOG.warn(String.format(
							"WebHookSettingsManagerImpl :: Template Not Found: Webhook '%s' from Project '%s' refers to template '%s', which was not found. WebHook URL is: %s",
							c.getUniqueKey(),
							sProject.getExternalId(),
							c.getPayloadTemplate(),
							c.getUrl()));
				}

				WebHookConfigEnhanced configEnhanced = createdEnhancedConfig(sProject, c, templateName, templateFormat, templateFormatDescription, enabledBuildStates);

				this.webhooksEnhanced.put(c.getUniqueKey(), configEnhanced);
				LOG.debug("WebHookSettingsManagerImpl :: updating webhook: '" + c.getUniqueKey() + "' " + configEnhanced.toString());
				if (!whc.getUniqueKey().equals(c.getUniqueKey())) {
				    persistRequired = recreateConflictingWebHook(projectInternalId, webHookProjectSettings, whc, c);
				}
			}
			if (persistRequired) {
			    persist(projectInternalId, "Conflicting WebHook ids remapped");
			}
		} else {
			LOG.debug("WebHookSettingsManagerImpl :: NOT rebuilding webhook cache. Project not found: " + projectInternalId);
		}
	}

    private WebHookConfigEnhanced createdEnhancedConfig(SProject sProject, WebHookConfig whc, String templateName,
            String templateFormat, String templateFormatDescription, Set<BuildStateEnum> enabledBuildStates) {
        WebHookConfigEnhanced configEnhanced = WebHookConfigEnhanced.builder()
        		.payloadFormat(templateFormat)
        		.payloadFormatDescription(templateFormatDescription)
        		.projectExternalId(sProject.getExternalId())
        		.projectInternalId(sProject.getProjectId())
        		.templateId(whc.getPayloadTemplate())
        		.templateDescription(templateName)
        		.buildStates(enabledBuildStates)
        		.webHookConfig(whc)
        		.generalisedWebAddress(myWebAddressTransformer.getGeneralisedHostName(whc.getUrl()))
        		.build();
        configEnhanced.addTag(templateFormat, TagType.FORMAT)
        		.addTag(Boolean.TRUE.equals(whc.getEnabled()) ? "enabled" : "disabled", TagType.WEBHOOK_ENABLED)
        		.addTag(Boolean.TRUE.equals(whc.isHideSecureValues()) ? "hideSecure" : "showSecure", TagType.SHOW_SECURE)
        		.addTag(whc.getPayloadTemplate(), TagType.TEMPLATE_ID)
        		.addTag(configEnhanced.getGeneralisedWebAddress().getGeneralisedAddress(), TagType.GENERALISED_URL);
        if (whc.getAuthenticationConfig() != null) {
        	configEnhanced.addTag("authenticated", TagType.AUTHENTICATED)
        	              .addTag(whc.getAuthenticationConfig().getType(), TagType.AUTHENTICATION_TYPE);
        }
        addTagIfPresent(configEnhanced, whc.getHeaders(), "headers", TagType.HEADER);
        addTagIfPresent(configEnhanced, whc.getTriggerFilters(), "filters", TagType.FILTER);
        addTagIfPresent(configEnhanced, whc.getParams(), "parameters", TagType.PARAMETER);
        return configEnhanced;
    }

    private boolean recreateConflictingWebHook(String projectInternalId, WebHookProjectSettings webHookProjectSettings, WebHookConfig origConfig, WebHookConfig newConfig) {
        boolean persistRequired;
        persistRequired = true;
        WebHookUpdateResult deleteResult = webHookProjectSettings.deleteWebHook(origConfig.getUniqueKey(), projectInternalId);
        if (deleteResult.isUpdated()) {
            WebHookUpdateResult addResult = webHookProjectSettings.addNewWebHook(newConfig);
            if (!addResult.isUpdated()) {
                LOG.warn("Problem re-keying webhook. Failed to recreate old webhook with id: " + origConfig.getUniqueKey());
            }
        } else {
            LOG.warn("Problem re-keying webhook. Failed to remove old webhook with id: " + origConfig.getUniqueKey());
        }
        return persistRequired;
    }
	
	private WebHookConfig checkForConflictingUniqueId(WebHookConfig origConfig, String projectInternalId) {
	    if (this.webhooksEnhanced.containsKey(origConfig.getUniqueKey()) &&  ! projectInternalId.equals(webhooksEnhanced.get(origConfig.getUniqueKey()).getProjectInternalId())) {
	        // Have the same unique ID, but on a different project.
	        // Therefore, generate a new UniqueId for this WebHook.
            WebHookConfig newWebHook = origConfig.cloneWithNewUniqueId();
            LOG.info(String.format("WebHookSettingsManagerImpl :: WebHook uniqueId selected as candidate for updating to avoid conflict with existing webhooks. projectId: '%s', oldUniqueId: '%s', newUniqueId: '%s'", projectInternalId, origConfig.getUniqueKey(), newWebHook.getUniqueKey()));
            Set<String> btRemappings = mapOldBuildTypesToNewBuildTypes(origConfig, webhooksEnhanced.get(origConfig.getUniqueKey()).getProjectInternalId(), projectInternalId);
            if (!btRemappings.isEmpty()) {
                newWebHook.clearAllEnabledBuildsInProject();
                btRemappings.forEach(newWebHook::enableBuildInProject);
                LOG.info(String.format("WebHookSettingsManagerImpl :: Remapping buildTypeIds for updated WebHook. projectId: '%s', newUniqueId: '%s', oldBuildTypeIds: %s, newBuildTypeIds: %s", projectInternalId, newWebHook.getUniqueKey(), origConfig.getEnabledBuildTypesSet(), newWebHook.getEnabledBuildTypesSet()));
            }
            return newWebHook;
        }
        return origConfig;
    }
	
	protected Set<String> mapOldBuildTypesToNewBuildTypes(WebHookConfig oldWebHookConfig, String oldProjectInternalId, String newProjectInternalId) {
	    Set<String> newBts = new TreeSet<>();
	    SProject oldSProject = this.myProjectManager.findProjectById(oldProjectInternalId);
	    SProject newSProject = this.myProjectManager.findProjectById(newProjectInternalId);
	    if (oldSProject == null || newSProject == null) {
	        return newBts;
	    }
	    Map<String,SBuildType> oldBtIdsToOldBTs = oldSProject.getBuildTypes().stream().collect(Collectors.toMap(BuildTypeIdentity::getInternalId, b -> b));
	    Map<String,SBuildType> newBtIdsToNewBTs = newSProject.getBuildTypes().stream().collect(Collectors.toMap(BuildTypeIdentity::getInternalId, b -> b));
	    
	    oldWebHookConfig.getEnabledBuildTypesSet().forEach(bt -> {
	        if (oldBtIdsToOldBTs.containsKey(bt)) {
	            newBtIdsToNewBTs.forEach((s,b) -> {
	                if (fuzzyNameMatcher(oldSProject.getExternalId(), 
	                        oldBtIdsToOldBTs.get(bt).getExternalId(), 
	                        newSProject.getExternalId(), 
	                        b.getExternalId())) {
	                    newBts.add(s);
	                }
	            });
	        }
	    });
	    return newBts;
	}
	
	protected static boolean fuzzyNameMatcher(String oldProjectExtId, String oldBuildTypeExtId, String newProjectExtId, String newBuildTypeExtId) {
	    // first try to match by removing the projectExternalId
	    if (oldBuildTypeExtId.toLowerCase().startsWith(oldProjectExtId.toLowerCase()) &&  //NOSONAR
	            newBuildTypeExtId.toLowerCase().startsWith(newProjectExtId.toLowerCase()) &&
	            oldBuildTypeExtId.substring(oldProjectExtId.length()).equalsIgnoreCase(
	                    newBuildTypeExtId.substring(newProjectExtId.length())
	            )
	        )
	    {
	        return true;
	    }
	    return false;
	}

    private Set<BuildStateEnum> determineEnabledBuildStates(WebHookConfig c, WebHookPayloadTemplate template) {
		if(Objects.nonNull(c.isEnabledForAllBuildsInProject())) {
			return new HashSet<>(template.getSupportedBuildStates());
		} else {
			Set<BuildStateEnum> enabled = new HashSet<>();
			for (BuildStateEnum s : c.getBuildStates().getStateSet()) {
				if (template.getSupportedBuildStates().contains(s)) {
					enabled.add(s);
				}
			}
			return enabled;
		}
	}

	@SuppressWarnings("rawtypes")
	private void addTagIfPresent(WebHookConfigEnhanced config, Collection collection, String tagName, TagType tagType) {
		if (collection != null && !collection.isEmpty()) {
			config.addTag(new Tag(tagName, tagType));
		}
	}

	@Override
	public boolean isHideSecureValuesEnabled(String webhookId) {
		if (webhookId == null) {
			return true;
		}
		for ( WebHookProjectSettings settings : this.projectSettingsMap.values()) {
			for (WebHookConfig config: settings.getWebHooksConfigs()) {
				if (webhookId.equals(config.getUniqueKey())) {
					return config.isHideSecureValues();
				}
			}
			
		}
		return true;
	}

}
