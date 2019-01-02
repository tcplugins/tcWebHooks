package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.ProjectAndBuildWebhooksBean;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookSettingsManager;



public class WebHookBuildTypeTabExtension extends BuildTypeTab {
	private final WebHookSettingsManager myProjectSettingsManager;
	private final String myPluginPath;
	private final WebHookHistoryRepository myWebHookHistoryRepository;
	private final WebAddressTransformer myWebAddressTransformer;

	protected WebHookBuildTypeTabExtension(
			@NotNull ProjectManager projectManager, 
			@NotNull WebHookSettingsManager projectSettingsManager, 
			@NotNull WebControllerManager manager,
			@NotNull PluginDescriptor pluginDescriptor,
			@NotNull WebHookHistoryRepository webHookHistoryRepository,
			@NotNull WebAddressTransformer webAddressTransformer) {
		super("webHooks", "WebHooks", manager, projectManager);
		myProjectSettingsManager = projectSettingsManager;
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
			 @NotNull SBuildType buildType, SUser user) {
		List<ProjectAndBuildWebhooksBean> projectAndParents = new ArrayList<>();  
		List<SProject> parentProjects = buildType.getProject().getProjectPath();
		for (SProject projectParent : parentProjects){
			projectAndParents.add(
					ProjectAndBuildWebhooksBean.newInstance(
							projectParent,
							this.myProjectSettingsManager.getSettings(projectParent.getProjectId()),
							buildType, 
							user.isPermissionGrantedForProject(projectParent.getProjectId(), Permission.EDIT_PROJECT), 
							myWebAddressTransformer
							)
					);
		}
		
		model.put("projectAndParents", projectAndParents);
   	
    	model.put("projectId", buildType.getProject().getProjectId());
    	model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(buildType.getProject()));
    	model.put("projectName", buildType.getProject().getName());
    	
    	model.put("buildTypeId", buildType.getBuildTypeId());
    	model.put("buildExternalId", TeamCityIdResolver.getExternalBuildId(buildType));
    	model.put("buildName", buildType.getName());
    	model.put("items", myWebHookHistoryRepository.findHistoryItemsForBuildType(buildType.getBuildTypeId(), 1, 50));
	}

	@Override
	public String getIncludeUrl() {
		return myPluginPath + "WebHook/webHookTabWithHistory.jsp";
	}
	
}
