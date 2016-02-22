package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.buildType.BuildTypeTab;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.ProjectAndBuildWebhooksBean;
import webhook.teamcity.settings.WebHookProjectSettings;



public class WebHookBuildTabExtension extends BuildTypeTab {
	WebHookProjectSettings settings;
	ProjectSettingsManager projSettings;
	String myPluginPath;

	protected WebHookBuildTabExtension(
			PagePlaces pagePlaces, ProjectManager projectManager, 
			ProjectSettingsManager settings, WebControllerManager manager,
			PluginDescriptor pluginDescriptor) {
		//super(myTitle, myTitle, null, projectManager);
		super("webHooks", "WebHooks", manager, projectManager);
		this.projSettings = settings;
		myPluginPath = pluginDescriptor.getPluginResourcesPath();
	}

	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void fillModel(Map model, HttpServletRequest request,
			 @NotNull SBuildType buildType, SUser user) {
		this.settings = 
			(WebHookProjectSettings)this.projSettings.getSettings(buildType.getProject().getProjectId(), "webhooks");
		
		List<ProjectAndBuildWebhooksBean> projectAndParents = new ArrayList<ProjectAndBuildWebhooksBean>();  
		List<SProject> parentProjects = buildType.getProject().getProjectPath();
		if (!user.getGlobalPermissions().contains(Permission.CHANGE_SERVER_SETTINGS)){
			parentProjects.remove(0);
		}
		for (SProject projectParent : parentProjects){
			projectAndParents.add(
					ProjectAndBuildWebhooksBean.newInstance(
							projectParent,
							(WebHookProjectSettings) this.projSettings.getSettings(projectParent.getProjectId(), "webhooks"),
							buildType
							)
					);
		}
		
//		projectAndParents.add(
//				ProjectAndBuildWebhooksBean.newInstance(
//						project,
//						(WebHookProjectSettings) this.projSettings.getSettings(project.getProjectId(), "webhooks"),
//						true
//						)
//				);

		model.put("projectAndParents", projectAndParents);
    	
//    	List<WebHookConfig> projectWebhooks = this.settings.getProjectWebHooksAsList();
//    	List<WebHookConfig> buildWebhooks = this.settings.getBuildWebHooksAsList(buildType);
//    	
//    	model.put("projectWebHookCount", projectWebhooks.size());
//    	if (projectWebhooks.size() == 0){
//    		model.put("noProjectWebHooks", "true");
//    		model.put("projectWebHooks", "false");
//    	} else {
//    		model.put("noProjectWebHooks", "false");
//    		model.put("projectWebHooks", "true");
//    		model.put("projectWebHookList", projectWebhooks);
//    		model.put("projectWebHooksDisabled", !this.settings.isEnabled());
//    	}
//    	
//    	model.put("buildWebHookCount", buildWebhooks.size());
//    	if (buildWebhooks.size() == 0){
//    		model.put("noBuildWebHooks", "true");
//    		model.put("buildWebHooks", "false");
//    	} else {
//    		model.put("noBuildWebHooks", "false");
//    		model.put("buildWebHooks", "true");
//    		model.put("buildWebHookList", buildWebhooks);
//    	}
//    	

    	model.put("projectId", buildType.getProject().getProjectId());
    	model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(buildType.getProject()));
    	model.put("projectName", buildType.getProject().getName());
    	
    	model.put("buildTypeId", buildType.getBuildTypeId());
    	model.put("buildExternalId", TeamCityIdResolver.getExternalBuildId(buildType));
    	model.put("buildName", buildType.getName());
	}

	@Override
	public String getIncludeUrl() {
		return myPluginPath + "WebHook/webHookTab.jsp";
	}


	
}
