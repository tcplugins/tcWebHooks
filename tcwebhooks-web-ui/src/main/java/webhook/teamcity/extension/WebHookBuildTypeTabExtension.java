package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.extension.util.WebHookSecureValuesHelperService;
import webhook.teamcity.history.PagedList;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.WebHookConfigEnhanced;



public class WebHookBuildTypeTabExtension extends BuildTypeTab {
	private final WebHookSettingsManager myWebHookSettingsManager;
	private final String myPluginPath;
	private final WebHookHistoryRepository myWebHookHistoryRepository;
	private final WebHookPayloadManager myPayloadManager;
	private final WebHookTemplateResolver myTemplateResolver;
	private final WebHookSecureValuesHelperService myWebHookSecureValuesHelperService;	

	public WebHookBuildTypeTabExtension(
			@NotNull ProjectManager projectManager, 
			@NotNull WebHookSettingsManager webHookSettingsManager, 
			@NotNull WebControllerManager manager,
			@NotNull PluginDescriptor pluginDescriptor,
			@NotNull WebHookHistoryRepository webHookHistoryRepository,
			@NotNull WebHookPayloadManager webhookPayloadManager,
			@NotNull WebHookTemplateResolver webHookTemplateResolver,
			@NotNull WebHookSecureValuesHelperService webHookSecureValuesHelperService) {
		super("webHooks", "WebHooks", manager, projectManager);
		myWebHookSettingsManager = webHookSettingsManager;
		myPluginPath = pluginDescriptor.getPluginResourcesPath();
		myWebHookHistoryRepository = webHookHistoryRepository;
		myPayloadManager = webhookPayloadManager;
		myTemplateResolver = webHookTemplateResolver;
		myWebHookSecureValuesHelperService = webHookSecureValuesHelperService;
		addCssFile(myPluginPath+ "WebHook/css/styles.css");
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return true;
	}

	@Override
	protected void fillModel(Map<String,Object> model, HttpServletRequest request,
			 @NotNull SBuildType buildType, SUser user) {
		
		List<ProjectWebHooksBean> projectWebHooks = new ArrayList<>();
		List<ProjectWebHooksBean> buildWebHooks = new ArrayList<>();
		this.myWebHookSettingsManager.getWebHooksForProjects(buildType.getProject().getProjectPath()).forEach((project, webhooks) -> {
			
			// Filter out webhooks that are for our buildType. We'll get them again below.
			List<WebHookConfigEnhanced> filteredWebHooks = webhooks.stream().filter(w -> !w.getWebHookConfig().isSpecificBuildTypeEnabled(buildType)).collect(Collectors.toList());
			ProjectWebHooksBean result =
					ProjectWebHooksBean.buildWithoutNew(filteredWebHooks, project,
							myPayloadManager.getRegisteredFormatsAsCollection(),
							myTemplateResolver.findWebHookTemplatesForProject(project),
							myWebHookSettingsManager.iswebHooksEnabledForProject(project.getProjectId()));
			projectWebHooks.add(result);
		});
		
		this.myWebHookSettingsManager.getWebHooksForBuild(Arrays.asList(buildType.getProject()), buildType).forEach((project, webhooks) -> {
			ProjectWebHooksBean result =
					ProjectWebHooksBean.buildWithoutNew(webhooks, project,
							myPayloadManager.getRegisteredFormatsAsCollection(),
							myTemplateResolver.findWebHookTemplatesForProject(project),
							myWebHookSettingsManager.iswebHooksEnabledForProject(project.getProjectId()));
			buildWebHooks.add(result);
		});
		
		model.put("projectAndParents", projectWebHooks);
		model.put("buildWebHooks", buildWebHooks);
   	
    	model.put("projectId", buildType.getProject().getProjectId());
    	model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(buildType.getProject()));
    	model.put("projectName", buildType.getProject().getName());
    	
    	model.put("buildTypeId", buildType.getBuildTypeId());
    	model.put("buildExternalId", TeamCityIdResolver.getExternalBuildId(buildType));
    	model.put("buildName", buildType.getName());
    	PagedList<WebHookHistoryItem> historyItems = myWebHookHistoryRepository.findHistoryItemsForBuildType(buildType.getBuildTypeId(), 1, 50); 
    	model.put("items", historyItems);
		model.put("webhookSecureEnabledMap", myWebHookSecureValuesHelperService.assembleWebHookSecureValues(historyItems));

	}

	@Override
	public String getIncludeUrl() {
		return myPluginPath + "WebHook/webHookTabWithHistory.jsp";
	}
	
}
