package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;


public class WebHookIndexPageController extends BaseController {

	    private final WebControllerManager myWebManager;
	    private final WebHookMainSettings myMainSettings;
	    private SBuildServer myServer;
	    private ProjectSettingsManager mySettings;
	    private PluginDescriptor myPluginDescriptor;

	    public WebHookIndexPageController(SBuildServer server, WebControllerManager webManager, 
	    		ProjectSettingsManager settings, PluginDescriptor pluginDescriptor,
	    		WebHookMainSettings configSettings) {
	        super(server);
	        myWebManager = webManager;
	        myServer = server;
	        mySettings = settings;
	        myPluginDescriptor = pluginDescriptor;
	        myMainSettings = configSettings;
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/index.html", this);
	      //myWebManager.registerController("/webhooks/settingsList.html", this);
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<String,Object>();
	        params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
	        
	        if(request.getParameter("projectId") != null 
	        		&& request.getParameter("projectId").startsWith("project")){
	        	
		    	WebHookProjectSettings projSettings = (WebHookProjectSettings) 
		    			mySettings.getSettings(request.getParameter("projectId"), "webhooks");
		    	SProject project = this.myServer.getProjectManager().findProjectById(request.getParameter("projectId"));
		    	
		    	String message = projSettings.getWebHooksAsString();
		    	
		    	params.put("haveProject", "true");
		    	params.put("messages", message);
		    	params.put("projectId", project.getProjectId());
		    	params.put("projectName", project.getName());
		    	
		    	if (myMainSettings.getInfoUrl() != null && myMainSettings.getInfoUrl().length() > 0){
		    		params.put("moreInfoText", "<li><a href=\"" + myMainSettings.getInfoUrl() + "\">" + myMainSettings.getInfoText() + "</a></li>");
		    	}
		    	
		    	Loggers.SERVER.debug(myMainSettings.getInfoText() + myMainSettings.getInfoUrl() + myMainSettings.getProxyListasString());
		    	
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

	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/index.jsp", params);
	        //return new ModelAndView("/WebHook/index.jsp", params);
	    }
}
