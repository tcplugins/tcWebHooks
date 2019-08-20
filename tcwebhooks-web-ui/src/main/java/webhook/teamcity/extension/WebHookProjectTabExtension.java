package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.project.ProjectTab;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.ProjectAndBuildWebhooksBean;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookSettingsManager;


public class WebHookProjectTabExtension extends ProjectTab {

	private final WebHookSettingsManager myWebHookSettingsManager;
	private final String myPluginPath;
	private final WebHookHistoryRepository myWebHookHistoryRepository;
	private final WebAddressTransformer myWebAddressTransformer;

	protected WebHookProjectTabExtension(
			@NotNull PagePlaces pagePlaces,
			@NotNull ProjectManager projectManager,
			@NotNull WebHookSettingsManager webHookSettingsManager,
			@NotNull PluginDescriptor pluginDescriptor,
			@NotNull WebHookHistoryRepository webHookHistoryRepository,
			@NotNull WebAddressTransformer webAddressTransformer) {
		super("webHooks", "WebHooks", pagePlaces, projectManager);
		this.myWebHookSettingsManager = webHookSettingsManager;
		myPluginPath = pluginDescriptor.getPluginResourcesPath();
		myWebHookHistoryRepository = webHookHistoryRepository;
		myWebAddressTransformer = webAddressTransformer;
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return true;
	}

	@Override
	protected void fillModel(Map<String,Object> model, HttpServletRequest request,
			 @NotNull SProject project, SUser user) {

		List<ProjectAndBuildWebhooksBean> projectAndParents = new ArrayList<>();
		List<SProject> parentProjects = project.getProjectPath();

		model.put("permissionError", "");
		for (SProject projectParent : parentProjects){
			projectAndParents.add(
					ProjectAndBuildWebhooksBean.newInstance(
							projectParent,
							this.myWebHookSettingsManager.getSettings(projectParent.getProjectId()),
							null,
							user.isPermissionGrantedForProject(projectParent.getProjectId(), Permission.EDIT_PROJECT),
							myWebAddressTransformer
						)
					);
		}

		model.put("projectAndParents", projectAndParents);

    	model.put("projectId", project.getProjectId());
    	model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
    	model.put("projectName", project.getName());
    	model.put("items", myWebHookHistoryRepository.findHistoryItemsForProject(project.getProjectId(), 1, 50));
	}

	@Override
	public String getIncludeUrl() {
		return myPluginPath+ "WebHook/webHookTabWithHistory.jsp";
	}

}
