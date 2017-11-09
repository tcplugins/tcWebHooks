package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.project.ProjectTab;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.ProjectAndBuildWebhooksBean;
import webhook.teamcity.settings.WebHookProjectSettings;



public class WebHookProjectTabExtension extends ProjectTab {
	
	ProjectSettingsManager projSettings;
	String myPluginPath;

	protected WebHookProjectTabExtension(
			PagePlaces pagePlaces, ProjectManager projectManager, 
			ProjectSettingsManager settings, PluginDescriptor pluginDescriptor) {
		super("webHooks", "WebHooks", pagePlaces, projectManager);
		this.projSettings = settings;
		myPluginPath = pluginDescriptor.getPluginResourcesPath();
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void fillModel(Map model, HttpServletRequest request,
			 @NotNull SProject project, SUser user) {
		
		List<ProjectAndBuildWebhooksBean> projectAndParents = new ArrayList<>();  
		List<SProject> parentProjects = project.getProjectPath();
		
		model.put("permissionError", "");
		
		if (!user.getGlobalPermissions().contains(Permission.CHANGE_SERVER_SETTINGS)){
			parentProjects.remove(0);
			if (project.getProjectId().equals("_Root")){
				model.put("permissionError", "<strong>You do not have permission to view WebHooks for the <em>_Root</em> project. Please contact your TeamCity Administrator</strong>");
			}
		}
		for (SProject projectParent : parentProjects){
			projectAndParents.add(
					ProjectAndBuildWebhooksBean.newInstance(
							projectParent,
							(WebHookProjectSettings) this.projSettings.getSettings(projectParent.getProjectId(), "webhooks"),
							null
							)
					);
		}
		
		model.put("projectAndParents", projectAndParents);
   	
    	model.put("projectId", project.getProjectId());
    	model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
    	model.put("projectName", project.getName());
	}

	@Override
	public String getIncludeUrl() {
		return myPluginPath+ "WebHook/webHookTab.jsp";
	}

}
