package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.project.ProjectTab;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.BuildWebhooksBean;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;



public class WebHookProjectTabExtension extends ProjectTab {
	WebHookProjectSettings settings;
	ProjectSettingsManager projSettings;
	String myPluginPath;

	protected WebHookProjectTabExtension(
			PagePlaces pagePlaces, ProjectManager projectManager, 
			ProjectSettingsManager settings, PluginDescriptor pluginDescriptor) {
		super("webHooks", "WebHooks", pagePlaces, projectManager);
		this.projSettings = settings;
		myPluginPath = pluginDescriptor.getPluginResourcesPath();
	}

	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void fillModel(Map model, HttpServletRequest request,
			 @NotNull SProject project, SUser user) {
		this.settings = (WebHookProjectSettings)this.projSettings.getSettings(project.getProjectId(), "webhooks");
    	
    	List<WebHookConfig> projectWebhooks = this.settings.getProjectWebHooksAsList();
    	List<BuildWebhooksBean> buildWebhooks = new ArrayList<BuildWebhooksBean>();
    	for (SBuildType build : project.getBuildTypes()){
    		buildWebhooks.add(new BuildWebhooksBean(build, settings.getBuildWebHooksAsList(build)));
    	}

    	model.put("projectWebHookCount", projectWebhooks.size());
    	if (projectWebhooks.size() == 0){
    		model.put("noProjectWebHooks", "true");
    		model.put("projectWebHooks", "false");
    	} else {
    		model.put("noProjectWebHooks", "false");
    		model.put("projectWebHooks", "true");
    		model.put("projectWebHookList", projectWebhooks);
    		model.put("projectWebHooksDisabled", !this.settings.isEnabled());
    	}
    	
		model.put("buildWebHookList", buildWebhooks);
    	
    	model.put("projectId", project.getProjectId());
    	model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
    	model.put("projectName", project.getName());
	}

	@Override
	public String getIncludeUrl() {
		return myPluginPath+ "WebHook/projectWebHookTab.jsp";
	}

}
