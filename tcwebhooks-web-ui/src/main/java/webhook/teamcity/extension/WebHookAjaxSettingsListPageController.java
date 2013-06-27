package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
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
	        
	        if(request.getParameter("projectId") != null 
	        		&& request.getParameter("projectId").startsWith("project")){
	        	
	        	SProject project = this.myServer.getProjectManager().findProjectById(request.getParameter("projectId"));
		    	WebHookProjectSettings projSettings = (WebHookProjectSettings) 
		    			mySettings.getSettings(request.getParameter("projectId"), "webhooks");
		    	
		    	String message = projSettings.getWebHooksAsString();
		    	
		    	params.put("haveProject", "true");
		    	params.put("messages", message);
		    	params.put("projectId", project.getProjectId());
		    	params.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
		    	params.put("projectName", project.getName());
		    	params.put("formatList", myManager.getRegisteredFormatsAsCollection());
		    	
		    	params.put("webHookCount", projSettings.getWebHooksCount());
		    	if (projSettings.getWebHooksCount() == 0){
		    		params.put("noWebHooks", "true");
		    		params.put("webHooks", "false");
		    	} else {
		    		params.put("noWebHooks", "false");
		    		params.put("webHooks", "true");
		    		params.put("webHookList", projSettings.getWebHooksAsList());
		    		params.put("webHooksDisabled", !projSettings.isEnabled());
		    		params.put("webHooksEnabledAsChecked", projSettings.isEnabledAsChecked());
		    	}
	        } else {
	        	params.put("haveProject", "false");
	        }

	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/settingsList.jsp", params);
	        //return new ModelAndView("/WebHook/settingsList.jsp", params);
	    }
}
