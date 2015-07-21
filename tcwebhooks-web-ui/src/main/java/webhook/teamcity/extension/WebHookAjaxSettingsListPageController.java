package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.extension.bean.ProjectWebHooksBeanJsonSerialiser;
import webhook.teamcity.extension.bean.WebhookBuildTypeEnabledStatusBean;
import webhook.teamcity.extension.bean.WebhookConfigAndBuildTypeListHolder;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;


public class WebHookAjaxSettingsListPageController extends BaseController {

	    private final WebControllerManager myWebManager;
	    private SBuildServer myServer;
	    private ProjectSettingsManager mySettings;
	    private PluginDescriptor myPluginDescriptor;
	    private final WebHookPayloadManager myManager;

	    public WebHookAjaxSettingsListPageController(SBuildServer server, WebControllerManager webManager, 
	    		ProjectSettingsManager settings, WebHookPayloadManager manager, PluginDescriptor pluginDescriptor) {
	        super(server);
	        myWebManager = webManager;
	        myServer = server;
	        mySettings = settings;
	        myPluginDescriptor = pluginDescriptor;
	        myManager = manager;
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/settingsList.html", this);
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<String,Object>();
	        params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
	        
	        
	        if(request.getParameter("projectId") != null){ 
	        	SProject project = this.myServer.getProjectManager().findProjectById(request.getParameter("projectId"));
	        	if (project == null){
	        		params.put("haveProject", "false");
	        	} else {
			    	WebHookProjectSettings projSettings = (WebHookProjectSettings) 
			    			mySettings.getSettings(request.getParameter("projectId"), "webhooks");
		    		params.put("projectWebHooksAsJson", ProjectWebHooksBeanJsonSerialiser.serialise(ProjectWebHooksBean.build(projSettings, project, myManager.getRegisteredFormatsAsCollection())));
	        	}
	        } else if (request.getParameter("buildTypeId") != null){
        		SBuildType sBuildType = TeamCityIdResolver.findBuildTypeById(this.myServer.getProjectManager(), request.getParameter("buildTypeId"));
        		if (sBuildType != null){
		        	SProject project = sBuildType.getProject();
		        	if (project != null){
				    	WebHookProjectSettings projSettings = (WebHookProjectSettings) 
				    			mySettings.getSettings(project.getProjectId(), "webhooks");
		        		params.put("projectWebHooksAsJson", ProjectWebHooksBeanJsonSerialiser.serialise(ProjectWebHooksBean.build(projSettings, sBuildType, project, myManager.getRegisteredFormatsAsCollection())));
		        	}
        		}
	        
	        } else {
	        	params.put("haveProject", "false");
	        }

	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/settingsList.jsp", params);
	        //return new ModelAndView("/WebHook/settingsList.jsp", params);
	    }
	    
}
