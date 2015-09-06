package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.extension.bean.ProjectWebHooksBeanJsonSerialiser;
import webhook.teamcity.extension.bean.TemplatesAndProjectWebHooksBean;
import webhook.teamcity.extension.bean.WebhookBuildTypeEnabledStatusBean;
import webhook.teamcity.extension.bean.WebhookConfigAndBuildTypeListHolder;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.extension.util.EnabledBuildStateResolver;
import webhook.teamcity.extension.util.ProjectHistoryResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;


public class WebHookAjaxEditPageController extends BaseController {

	    protected static final String BEFORE_FINISHED = "BeforeFinished";
		protected static final String BUILD_INTERRUPTED = "BuildInterrupted";
		protected static final String BUILD_STARTED = "BuildStarted";
		protected static final String BUILD_BROKEN = "BuildBroken";
		protected static final String BUILD_FIXED = "BuildFixed";
		protected static final String BUILD_FAILED = "BuildFailed";
		protected static final String BUILD_SUCCESSFUL = "BuildSuccessful";
		
		private final WebControllerManager myWebManager;
	    private SBuildServer myServer;
	    private ProjectSettingsManager mySettings;
	    private final String myPluginPath;
	    private final WebHookPayloadManager myManager;
		private final WebHookTemplateResolver myTemplateResolver;
	    
	    public WebHookAjaxEditPageController(SBuildServer server, WebControllerManager webManager, 
	    		ProjectSettingsManager settings, WebHookProjectSettings whSettings, WebHookPayloadManager manager,
	    		WebHookTemplateResolver templateResolver, PluginDescriptor pluginDescriptor) {
	        super(server);
	        myWebManager = webManager;
	        myServer = server;
	        mySettings = settings;
	        myPluginPath = pluginDescriptor.getPluginResourcesPath();
	        myManager = manager;
	        myTemplateResolver = templateResolver;
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/ajaxEdit.html", this);
	    }
	    

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<String,Object>();
	        
	        SUser myUser = SessionUser.getUser(request);
	        SProject myProject = null;
	        WebHookProjectSettings projSettings = null;
	    	
	    	if (request.getMethod().equalsIgnoreCase("post")){
	    		if ((request.getParameter("projectId") != null)){
	    			myProject = this.myServer.getProjectManager().findProjectById(request.getParameter("projectId"));
		        	if (myProject == null){
		        		params.put("messages", "<errors><error id=\"messageArea\">The webhook was not found. No matching project found</error></errors>");
		        	} else {
	    		    	projSettings = (WebHookProjectSettings) mySettings.getSettings(request.getParameter("projectId"), "webhooks");

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
				    				&& (request.getParameter("URL").length() > 0 )
				    				&& (request.getParameter("payloadFormat") != null)
				    				&& (request.getParameter("payloadFormat").length() > 0)
				    				&& (request.getParameter("payloadTemplate") != null)
				    				&& (request.getParameter("payloadTemplate").length() > 0))
				    				{
			    					
			    					if (!myTemplateResolver.templateIsValid(myProject, request.getParameter("payloadFormat"), request.getParameter("payloadTemplate"))){
			    						params.put("messages", "<errors><error id=\"emptyPayloadFormat\">Please choose a Payload Format.</error></errors>");
			    					}else if (request.getParameter("webHookId") != null){
			    						Boolean enabled = false;
			    						Boolean buildTypeAll = false;
			    						Boolean buildTypeSubProjects = false;
			    						Set<String> buildTypes = new HashSet<String>();
			    						if ((request.getParameter("webHooksEnabled") != null )
			    								&& (request.getParameter("webHooksEnabled").equalsIgnoreCase("on"))){
			    							enabled = true;
			    						}
			    						BuildState states = new BuildState();
			    						EnabledBuildStateResolver buildStateResolver = new EnabledBuildStateResolver(myTemplateResolver, myProject);
			    						
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_SUCCESSFUL, BUILD_SUCCESSFUL);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_FAILED, BUILD_FAILED);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_FIXED, BUILD_FIXED);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_BROKEN, BUILD_BROKEN);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_STARTED, BUILD_STARTED);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_INTERRUPTED, BUILD_INTERRUPTED);	
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BEFORE_BUILD_FINISHED, BEFORE_FINISHED);
			    						buildStateResolver.checkAndAddBuildStateIfEitherSet(request, states, BuildStateEnum.BUILD_FINISHED, BUILD_SUCCESSFUL,BUILD_FAILED);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.RESPONSIBILITY_CHANGED, "ResponsibilityChanged");
			    						
			    						if ((request.getParameter("buildTypeSubProjects") != null ) && (request.getParameter("buildTypeSubProjects").equalsIgnoreCase("on"))){
			    							buildTypeSubProjects = true;
			    						}
			    						if ((request.getParameter("buildTypeAll") != null ) && (request.getParameter("buildTypeAll").equalsIgnoreCase("on"))){
			    							buildTypeAll = true;
			    						} else {
			    							if (request.getParameterValues("buildTypeId") != null){
			    								String[] types = request.getParameterValues("buildTypeId");
			    								for (String string : types) {
			    									buildTypes.add(string);
												}
			    							}
			    						}
		    						
			    						if (request.getParameter("webHookId").equals("new")){
			    							projSettings.addNewWebHook(myProject.getProjectId(),request.getParameter("URL"), enabled, 
			    														states,request.getParameter("payloadFormat"), request.getParameter("payloadTemplate"), buildTypeAll, buildTypeSubProjects, buildTypes);
			    							if(projSettings.updateSuccessful()){
			    								myProject.persist();
			    	    						params.put("messages", "<errors />");
			    							} else {
			    								params.put("message", "<errors><error id=\"\">" + projSettings.getUpdateMessage() + "</error>");
			    							}
			    						} else {
			    							projSettings.updateWebHook(myProject.getProjectId(),request.getParameter("webHookId"), 
			    														request.getParameter("URL"), enabled, 
			    														states, request.getParameter("payloadFormat"), request.getParameter("payloadTemplate"), buildTypeAll, buildTypeSubProjects, buildTypes);
			    							if(projSettings.updateSuccessful()){
			    								myProject.persist();
			    	    						params.put("messages", "<errors />");
			    							} else {
			    								params.put("message", "<errors><error id=\"\">" + projSettings.getUpdateMessage() + "</error>");
			    							}
			    						}
			    					} // TODO Need to handle webHookId being null
			    						
			    				} else {
			    					if ((request.getParameter("URL") == null ) 
				    				|| (request.getParameter("URL").length() == 0)){
			    						params.put("messages", "<errors><error id=\"emptyWebHookUrl\">Please enter a URL.</error></errors>");
			    					} else if ((request.getParameter("payloadFormat") == null)
				    				|| (request.getParameter("payloadFormat").length() == 0)){
			    						params.put("messages", "<errors><error id=\"emptyPayloadFormat\">Please choose a Payload Format.</error></errors>");
			    					}
			    				}
				    			
			    			}
			    		} else {
			    			params.put("messages", "<errors><error id=\"messageArea\">You do not appear to have permission to edit WebHooks.</error></errors>");
			    		}
		        	}
	    		}
	    	}

	    	params.put("formatList", RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(myProject),
					myManager.getRegisteredFormats()).getTemplateList());
	    	
	        if (request.getMethod().equalsIgnoreCase("get")
	        		&& request.getParameter("projectId") != null ){
	        		        	
	        	SProject project = TeamCityIdResolver.findProjectById(this.myServer.getProjectManager(), request.getParameter("projectId"));
	        	if (project != null){
	        	
			    	WebHookProjectSettings projSettings1 = (WebHookProjectSettings) mySettings.getSettings(request.getParameter("projectId"), "webhooks");
			    	
			    	String message = projSettings1.getWebHooksAsString();
			    	
			    	params.put("haveProject", "true");
			    	params.put("messages", message);
			    	params.put("projectId", project.getProjectId());
			    	params.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
			    	params.put("projectName", project.getName());
			    	
			    	params.put("webHookCount", projSettings1.getWebHooksCount());
			    	if (projSettings1.getWebHooksCount() == 0){
			    		params.put("noWebHooks", "true");
			    		params.put("webHooks", "false");
			    	} else {
			    		params.put("noWebHooks", "false");
			    		params.put("webHooks", "true");
			    		params.put("webHookList", projSettings.getWebHooksAsList());
			    		params.put("webHooksDisabled", !projSettings.isEnabled());
			    		params.put("webHooksEnabledAsChecked", projSettings.isEnabledAsChecked());
			    		
			    		params.put("projectWebHooksAsJson", ProjectWebHooksBeanJsonSerialiser.serialise(
								TemplatesAndProjectWebHooksBean.build(
										RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
																			myManager.getRegisteredFormats()), 
										ProjectWebHooksBean.build(projSettings, 
																	project, 
																	myManager.getRegisteredFormatsAsCollection(),
																	myTemplateResolver.findWebHookTemplatesForProject(project)
																	),
										ProjectHistoryResolver.getProjectHistory(project)																	
										)
									)
								);

			    		//params.put("projectWebHooksAsJson", ProjectWebHooksBeanJsonSerialiser.serialise(RegisteredWebHookTemplateBean.ProjectWebHooksBean.build(projSettings, project, myManager.getRegisteredFormatsAsCollection())));
			    	}
			    	
	        	} else {
	        		params.put("haveProject", "false");
	        	}
//	        } else {
//	        	params.put("haveProject", "false");
	        }
	        
	        return new ModelAndView(myPluginPath + "WebHook/ajaxEdit.jsp", params);
	    }
}
