package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.BuildState;
import webhook.teamcity.settings.WebHookProjectSettings;


public class WebHookAjaxEditPageController extends BaseController {

	    private final WebControllerManager myWebManager;
	    private SBuildServer myServer;
	    private ProjectSettingsManager mySettings;
	    private final String myPluginPath;
	    
	    public WebHookAjaxEditPageController(SBuildServer server, WebControllerManager webManager, 
	    		ProjectSettingsManager settings, WebHookProjectSettings whSettings,
	    		PluginDescriptor pluginDescriptor) {
	        super(server);
	        myWebManager = webManager;
	        myServer = server;
	        mySettings = settings;
	        myPluginPath = pluginDescriptor.getPluginResourcesPath();
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/ajaxEdit.html", this);
	    }
	    
	    private Integer checkAndAddBuildState(HttpServletRequest r, Integer myRunningTotal, Integer myBuildState, String varName){
	    	if(myRunningTotal.equals(BuildState.ALL_ENABLED.intValue())){
	    		return BuildState.ALL_ENABLED;
	    	} else if ((r.getParameter(varName) != null)
	    		&& (r.getParameter(varName).equalsIgnoreCase("on"))){
	    		return myRunningTotal + myBuildState;
	    	} else {
		    	return myRunningTotal;
	    	}
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<String,Object>();
	        
	        SUser myUser = SessionUser.getUser(request);
	        SProject myProject = null;
	        WebHookProjectSettings projSettings = null;
	    	
	    	if (request.getMethod().equalsIgnoreCase("post")){
	    		if ((request.getParameter("projectId") != null)
	    			&& request.getParameter("projectId").startsWith("project")){
	    		    	projSettings = (WebHookProjectSettings) mySettings.getSettings(request.getParameter("projectId"), "webhooks");
	    		    	myProject = this.myServer.getProjectManager().findProjectById(request.getParameter("projectId"));

			    		if ((projSettings != null) && (myProject != null)
			    				&& (myUser.isPermissionGrantedForProject(myProject.getProjectId(), Permission.EDIT_PROJECT))){
			    			if ((request.getParameter("submitAction") != null ) 
			    				&& (request.getParameter("submitAction").equals("removeWebHook"))
			    				&& (request.getParameter("removedWebHookId") != null)){
			    					projSettings.deleteWebHook(request.getParameter("removedWebHookId"), myProject.getProjectId());
			    					if(projSettings.updateSuccessful()){
			    						myProject.persist();
			    						params.put("messages", "<errors />");
			    					} else {
			    						params.put("messages", "<errors><error id=\"messageArea\">The webhook was not found. Have the WebHooks been edited on disk or by another user?</error></errors>");		
			    					}
			    					
			    			} else if ((request.getParameter("submitAction") != null ) 
				    				&& (request.getParameter("submitAction").equals("updateWebHook"))){
			    				if((request.getParameter("URL") != null ) 
				    				&& (request.getParameter("URL").length() > 0 )){
			    					if (request.getParameter("webHookId") != null){
			    						Integer runningTotal = 0;
			    						Boolean enabled = false;
			    						if ((request.getParameter("webHooksEnabled") != null )
			    								&& (request.getParameter("webHooksEnabled").equalsIgnoreCase("on"))){
			    							enabled = true;
			    						}
			    						runningTotal = this.checkAndAddBuildState(request, runningTotal, BuildState.ALL_ENABLED, "selectAll");
			    						runningTotal = this.checkAndAddBuildState(request, runningTotal, BuildState.BUILD_STARTED, "BuildStarted");
			    						runningTotal = this.checkAndAddBuildState(request, runningTotal, BuildState.BUILD_INTERRUPTED, "BuildInterrupted");	
			    						runningTotal = this.checkAndAddBuildState(request, runningTotal, BuildState.BEFORE_BUILD_FINISHED, "BeforeFinished");
			    						runningTotal = this.checkAndAddBuildState(request, runningTotal, BuildState.BUILD_FINISHED, "BuildFinished");
			    						runningTotal = this.checkAndAddBuildState(request, runningTotal, BuildState.BUILD_CHANGED_STATUS, "StatusChanged");
			    						runningTotal = this.checkAndAddBuildState(request, runningTotal, BuildState.RESPONSIBILITY_CHANGED, "ResponsibilityChanged");
		    						
			    						if (request.getParameter("webHookId").equals("new")){
			    							projSettings.addNewWebHook(myProject.getProjectId(),request.getParameter("URL"), enabled, runningTotal);
			    							if(projSettings.updateSuccessful()){
			    								myProject.persist();
			    	    						params.put("messages", "<errors />");
			    							} else {
			    								params.put("message", "<errors><error id=\"\">" + projSettings.getUpdateMessage() + "</error>");
			    							}
			    						} else {
			    							projSettings.updateWebHook(myProject.getProjectId(),request.getParameter("webHookId"), request.getParameter("URL"), enabled, runningTotal);
			    							if(projSettings.updateSuccessful()){
			    								myProject.persist();
			    	    						params.put("messages", "<errors />");
			    							} else {
			    								params.put("message", "<errors><error id=\"\">" + projSettings.getUpdateMessage() + "</error>");
			    							}
			    						}
			    					} // TODO Need to handle webHookId being null
			    						
			    				} else {
			    					params.put("messages", "<errors><error id=\"error_webHookName\">Please enter a URL.</error></errors>");
			    				}
				    			
			    			}
			    		} else {
			    			params.put("messages", "<errors><error id=\"messageArea\">You do not appear to have permission to edit WebHooks.</error></errors>");
			    		}
	    		}
	    	}

	    	
	        if (request.getMethod().equalsIgnoreCase("get")
	        		&& request.getParameter("projectId") != null 
	        		&& request.getParameter("projectId").startsWith("project")){
	        	
		    	WebHookProjectSettings projSettings1 = (WebHookProjectSettings) mySettings.getSettings(request.getParameter("projectId"), "webhooks");
		    	SProject project = this.myServer.getProjectManager().findProjectById(request.getParameter("projectId"));
		    	
		    	String message = projSettings1.getWebHooksAsString();
		    	
		    	params.put("haveProject", "true");
		    	params.put("messages", message);
		    	params.put("projectId", project.getProjectId());
		    	params.put("projectName", project.getName());
		    	
		    	params.put("webHookCount", projSettings1.getWebHooksCount());
		    	if (projSettings1.getWebHooksCount() == 0){
		    		params.put("noWebHooks", "true");
		    		params.put("webHooks", "false");
		    	} else {
		    		params.put("noWebHooks", "false");
		    		params.put("webHooks", "true");
		    		params.put("webHookList", projSettings1.getWebHooksAsList());
		    		params.put("webHooksDisabled", !projSettings1.isEnabled());
		    	}
	        } else {
	        	params.put("haveProject", "false");
	        }
	        
	        return new ModelAndView(myPluginPath + "WebHook/index.jsp", params);
	        //return new ModelAndView("/WebHook/ajaxEdit.jsp", params);
	    }
}
