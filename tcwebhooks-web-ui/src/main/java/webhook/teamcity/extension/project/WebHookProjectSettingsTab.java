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
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookProjectSettingsTab extends EditProjectTab {
	private static final String WEBHOOKS = "webhooks";
	private static final String TAB_TITLE = "WebHooks";
	private SecurityContext mySecurityContext;
	ProjectSettingsManager projSettings;
	private final WebHookPayloadManager myManager;
	private final WebHookTemplateResolver myTemplateResolver;
	String myPluginPath;
	
	public WebHookProjectSettingsTab(@NotNull PagePlaces pagePlaces,
									@NotNull ProjectSettingsManager settings,
									@NotNull PluginDescriptor pluginDescriptor,
									@NotNull SecurityContext securityContext,
									@NotNull WebHookPayloadManager payloadManager,
									@NotNull WebHookTemplateResolver templateResolver) {
		super(pagePlaces, pluginDescriptor.getPluginName(), "WebHook/webHookProjectSettingsTab.jsp", TAB_TITLE);
		this.projSettings = settings;
        this.mySecurityContext = securityContext;
        this.myManager = payloadManager;
        this.myTemplateResolver = templateResolver;
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
		ProjectWebHooksBean config = ProjectWebHooksBean.buildWithoutNew(
				(WebHookProjectSettings) this.projSettings.getSettings(currentProject.getProjectId(), WEBHOOKS), 
				currentProject, 
				myManager.getRegisteredFormatsAsCollection(),
				myTemplateResolver.findWebHookTemplatesForProject(currentProject)
			);
		final int count = config.getWebHookList().size();
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
        
		List<ProjectWebHooksBean> parentProjectBeans = new ArrayList<>();  
		ProjectWebHooksBean projectBean = null;  
		List<SProject> parentProjects = currentProject.getProjectPath();
		
		for (SProject projectParent : parentProjects){
			Loggers.SERVER.info("WebHookProjectSettingsTab: Assembling webhooks for project: " + projectParent.getName());
			if (currentProject.getProjectId().equals(projectParent.getProjectId())) {
				
				projectBean = ProjectWebHooksBean.buildWithoutNew(
						(WebHookProjectSettings) this.projSettings.getSettings(projectParent.getProjectId(), WEBHOOKS), 
						currentProject, 
						myManager.getRegisteredFormatsAsCollection(),
						myTemplateResolver.findWebHookTemplatesForProject(currentProject)
					);
				
			} else {
				parentProjectBeans.add(
						ProjectWebHooksBean.buildWithoutNew(
							(WebHookProjectSettings) this.projSettings.getSettings(projectParent.getProjectId(), WEBHOOKS),
							projectParent,
							myManager.getRegisteredFormatsAsCollection(),
							myTemplateResolver.findWebHookTemplatesForProject(projectParent)
							)
					);
			}
		}
		
		model.put("parentProjectBeans", parentProjectBeans);
		model.put("projectBean", projectBean);
		model.put("project", currentProject);
   	
    	model.put("projectId", currentProject.getProjectId());
    	model.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(currentProject));
    	model.put("externalId", TeamCityIdResolver.getExternalProjectId(currentProject));
    	model.put("projectName", currentProject.getName());
    }

}