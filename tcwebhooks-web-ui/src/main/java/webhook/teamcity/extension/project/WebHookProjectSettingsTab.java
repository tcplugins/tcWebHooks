package webhook.teamcity.extension.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.ProjectAndBuildWebhooksBean;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookProjectSettingsTab extends EditProjectTab {
	private static final String TAB_TITLE = "WebHooks";
	private SecurityContext mySecurityContext;
	ProjectSettingsManager projSettings;
	String myPluginPath;
	
	public WebHookProjectSettingsTab(@NotNull PagePlaces pagePlaces,
									@NotNull ProjectSettingsManager settings,
									@NotNull PluginDescriptor pluginDescriptor,
									@NotNull SecurityContext securityContext,
									@NotNull SBuildServer sBuildServer) {
		super(pagePlaces, pluginDescriptor.getPluginName(), "WebHook/webHookProjectSettingsTab.jsp", TAB_TITLE);
		this.projSettings = settings;
        this.mySecurityContext = securityContext;
        //addCssFile(pluginDescriptor.getPluginResourcesPath("debRepository/css/debRepository.css"));
        //addJsFile(pluginDescriptor.getPluginResourcesPath("debRepository/projectConfigSettings.js"));
    }

    @NotNull
    @Override
    public String getTabTitle(@NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return TAB_TITLE;
        }
		ProjectAndBuildWebhooksBean config = ProjectAndBuildWebhooksBean.newInstance(
				currentProject,
				(WebHookProjectSettings) this.projSettings.getSettings(currentProject.getProjectId(), "webhooks"),
				null
				);
		final int count = config.getBuildWebhookCount();
        if (count == 0) {
            return TAB_TITLE;
        }
        return TAB_TITLE + " (" + count + ")";
    }

    @Override
    public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
        final SProject currentProject = getProject(request);
        if (currentProject == null) {
            return;
        }
        
		List<ProjectAndBuildWebhooksBean> projectAndParents = new ArrayList<>();  
		List<SProject> parentProjects = currentProject.getProjectPath();
		
		model.put("permissionError", "");
		
		if (!AuthUtil.hasPermissionToManageProject(mySecurityContext.getAuthorityHolder(), "_Root")){
			parentProjects.remove(0);
			if (currentProject.getProjectId().equals("_Root")){
				model.put("permissionError", "<strong>You do not have permission to view WebHooks for the <em>_Root</em> project. Please contact your TeamCity Administrator</strong>");
			}
		}
		for (SProject projectParent : parentProjects){
			Loggers.SERVER.info("WebHookProjectSettingsTab: Assembling webhooks for project: " + projectParent.getName());
			projectAndParents.add(
					ProjectAndBuildWebhooksBean.newInstance(
							projectParent,
							(WebHookProjectSettings) this.projSettings.getSettings(projectParent.getProjectId(), "webhooks"),
							null
							)
					);
		}
		
		model.put("projectAndParents", projectAndParents);
   	
	    	model.put("projectId", currentProject.getProjectId());
	    	model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(currentProject));
	    	model.put("projectName", currentProject.getName());
    	}


	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return super.isAvailable(request);
	}
}