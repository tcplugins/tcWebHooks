package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.project.ProjectTab;
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookSettingsManager;


public class WebHookProjectTabExtension extends ProjectTab {

	private final WebHookSettingsManager myWebHookSettingsManager;
	private final String myPluginPath;
	private final WebHookHistoryRepository myWebHookHistoryRepository;
	private final WebHookPayloadManager myPayloadManager;
	private final WebHookTemplateResolver myTemplateResolver;

	public WebHookProjectTabExtension(
			@NotNull PagePlaces pagePlaces,
			@NotNull ProjectManager projectManager,
			@NotNull WebHookSettingsManager webHookSettingsManager,
			@NotNull PluginDescriptor pluginDescriptor,
			@NotNull WebHookHistoryRepository webHookHistoryRepository,
			@NotNull WebHookPayloadManager webhookPayloadManager,
			@NotNull WebHookTemplateResolver webHookTemplateResolver) {
		super("webHooks", "WebHooks", pagePlaces, projectManager);
		myWebHookSettingsManager = webHookSettingsManager;
		myPluginPath = pluginDescriptor.getPluginResourcesPath();
		myWebHookHistoryRepository = webHookHistoryRepository;
		myPayloadManager = webhookPayloadManager;
		myTemplateResolver = webHookTemplateResolver;
		addCssFile(myPluginPath+ "WebHook/css/styles.css");
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return true;
	}

	@Override
	protected void fillModel(Map<String,Object> model, HttpServletRequest request, @NotNull SProject currentProject, SUser user) {

		List<ProjectWebHooksBean> projectWebHooks = new ArrayList<>();
		this.myWebHookSettingsManager.getWebHooksForProjects(currentProject.getProjectPath()).forEach((project, webhooks) -> {
			ProjectWebHooksBean result =
					ProjectWebHooksBean.buildWithoutNew(webhooks, project,
							myPayloadManager.getRegisteredFormatsAsCollection(),
							myTemplateResolver.findWebHookTemplatesForProject(project),
							myWebHookSettingsManager.iswebHooksEnabledForProject(project.getProjectId()));
			projectWebHooks.add(result);
		});
		
		model.put("permissionError", "");
		model.put("projectAndParents", projectWebHooks);

		model.put("project", currentProject);
		model.put("items", myWebHookHistoryRepository.findHistoryItemsForProject(currentProject.getProjectId(), 1, 50));
	}

	@Override
	public String getIncludeUrl() {
		return myPluginPath+ "WebHook/webHookTabWithHistory.jsp";
	}

}
