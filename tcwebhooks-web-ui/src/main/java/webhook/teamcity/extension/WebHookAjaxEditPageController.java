package webhook.teamcity.extension;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.extension.bean.ProjectWebHooksBeanGsonSerialiser;
import webhook.teamcity.extension.bean.RegisteredWebhookAuthenticationTypesBean;
import webhook.teamcity.extension.bean.TemplatesAndProjectWebHooksBean;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.extension.util.EnabledBuildStateResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookProjectSettings.WebHookUpdateResult;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookAjaxEditPageController extends BaseController {

	    private static final String PARAMS_MESSAGES_KEY = "messages";
		protected static final String BEFORE_FINISHED = "BeforeFinished";
		protected static final String BUILD_INTERRUPTED = "BuildInterrupted";
		protected static final String BUILD_ADDED_TO_QUEUE = "BuildAddedToQueue";
		protected static final String BUILD_REMOVED_FROM_QUEUE = "BuildRemovedFromQueue";
		protected static final String BUILD_STARTED = "BuildStarted";
		protected static final String CHANGES_LOADED = "ChangesLoaded";
		protected static final String BUILD_BROKEN = "BuildBroken";
		protected static final String BUILD_FIXED = "BuildFixed";
		protected static final String BUILD_FAILED = "BuildFailed";
		protected static final String BUILD_SUCCESSFUL = "BuildSuccessful";
		protected static final String BUILD_PINNED = "BuildPinned";
		protected static final String BUILD_UNPINNED = "BuildUnpinned";
		
		private final WebControllerManager myWebManager;
	    private ProjectSettingsManager mySettings;
	    private final String myPluginPath;
	    private final WebHookPayloadManager myManager;
		private final WebHookTemplateResolver myTemplateResolver;
		private final WebHookAuthenticatorProvider myAuthenticatorProvider;
	    
	    public WebHookAjaxEditPageController(SBuildServer server, WebControllerManager webManager, 
	    		ProjectSettingsManager settings, WebHookPayloadManager manager,
	    		WebHookTemplateResolver templateResolver, PluginDescriptor pluginDescriptor, WebHookAuthenticatorProvider authenticatorProvider) {
	        super(server);
	        myWebManager = webManager;
	        mySettings = settings;
	        myPluginPath = pluginDescriptor.getPluginResourcesPath();
	        myManager = manager;
	        myTemplateResolver = templateResolver;
	        myAuthenticatorProvider = authenticatorProvider;
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/ajaxEdit.html", this);
	    }
	    

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<>();
	        
	        SUser myUser = SessionUser.getUser(request);
	        SProject myProject = null;
	    	
	    	if (request.getMethod().equalsIgnoreCase("post")){
	    		boolean noErrors = true;
	    		if ((request.getParameter("projectId") != null)){
	    			myProject = this.myServer.getProjectManager().findProjectById(request.getParameter("projectId"));
		        	if (myProject == null){
		        		params.put(PARAMS_MESSAGES_KEY, "<errors><error id=\"messageArea\">The webhook was not found. No matching project found</error></errors>");
		        		noErrors = false;
		        	} else {
		        		WebHookProjectSettings projSettings = (WebHookProjectSettings) mySettings.getSettings(request.getParameter("projectId"), "webhooks");

			    		if (noErrors && (projSettings != null) && (myProject != null)
			    				&& (myUser.isPermissionGrantedForProject(myProject.getProjectId(), Permission.EDIT_PROJECT))){
			    			if ((request.getParameter("submitAction") != null ) 
			    				&& (request.getParameter("submitAction").equals("removeWebHook"))
			    				&& (request.getParameter("removedWebHookId") != null)){
			    					WebHookUpdateResult result = projSettings.deleteWebHook(request.getParameter("removedWebHookId"), myProject.getProjectId());
			    					if(result.isUpdated()){
			    						myProject.persist();
	    	    						params.put(PARAMS_MESSAGES_KEY, "<errors /><webhook action='delete' id='" + result.getWebHookConfig().getUniqueKey() + "'/>");
			    					} else {
			    						params.put(PARAMS_MESSAGES_KEY, "<errors><error id=\"messageArea\">The webhook was not found. Have the WebHooks been edited on disk or by another user?</error></errors>");		
			    						noErrors = false;
			    					}
			    					
			    			} else if (noErrors && (request.getParameter("submitAction") != null ) 
				    				&& (request.getParameter("submitAction").equals("updateWebHook") || request.getParameter("submitAction").equals("addWebHook"))){
			    				if((request.getParameter("URL") != null ) 
				    				&& (request.getParameter("URL").length() > 0 )
				    				&& (request.getParameter("payloadFormat") != null)
				    				&& (request.getParameter("payloadFormat").length() > 0)
				    				&& (request.getParameter("payloadTemplate") != null)
				    				&& (request.getParameter("payloadTemplate").length() > 0))
				    				{
			    					
			    					if (!myTemplateResolver.templateIsValid(myProject, request.getParameter("payloadFormat"), request.getParameter("payloadTemplate"))){
			    						params.put(PARAMS_MESSAGES_KEY, "<errors><error id=\"emptyPayloadFormat\">Please choose a Payload Format.</error></errors>");
			    						noErrors = false;
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
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_ADDED_TO_QUEUE, BUILD_ADDED_TO_QUEUE);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_REMOVED_FROM_QUEUE, BUILD_REMOVED_FROM_QUEUE);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_STARTED, BUILD_STARTED);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.CHANGES_LOADED, CHANGES_LOADED);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_INTERRUPTED, BUILD_INTERRUPTED);	
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BEFORE_BUILD_FINISHED, BEFORE_FINISHED);
			    						buildStateResolver.checkAndAddBuildStateIfEitherSet(request, states, BuildStateEnum.BUILD_FINISHED, BUILD_SUCCESSFUL,BUILD_FAILED);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.RESPONSIBILITY_CHANGED, "ResponsibilityChanged");
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_PINNED, BUILD_PINNED);
			    						buildStateResolver.checkAndAddBuildState(request, states, BuildStateEnum.BUILD_UNPINNED, BUILD_UNPINNED);
			    						
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
			    						WebHookAuthConfig webHookAuthConfig = null;
			    						if (noErrors && request.getParameter("extraAuthType") !=null 
			    								&& !request.getParameter("extraAuthType").equals("")){
			    							
			    							webHookAuthConfig =  new WebHookAuthConfig();
			    							webHookAuthConfig.setType(request.getParameter("extraAuthType"));
			    							webHookAuthConfig.setPreemptive(false);
			    							if (request.getParameter("extraAuthPreemptive") != null){
			    								webHookAuthConfig.setPreemptive(request.getParameter("extraAuthPreemptive").equalsIgnoreCase("on"));
			    							}
				    						Enumeration<String> attrs =  request.getParameterNames();
				    						while(attrs.hasMoreElements()) {
				    							String paramName = attrs.nextElement();
				    							if (paramName.startsWith("extraAuthParam_") && request.getParameter(paramName) != null){
				    								webHookAuthConfig.getParameters().put(paramName.substring("extraAuthParam_".length()), request.getParameter(paramName).toString());
				    							}
				    						}
				    						if (myAuthenticatorProvider.isRegisteredType(webHookAuthConfig.getType())) {
				    							if (myAuthenticatorProvider.areAllRequiredParametersPresent(webHookAuthConfig)){
				    								params.put(PARAMS_MESSAGES_KEY, "<errors />");
				    							} else {
				    								params.put(PARAMS_MESSAGES_KEY, "<errors><error id=\"emptyAuthParameter\">Please complete all required authentication fields.</error></errors>");
				    								noErrors = false;
				    							}
				    						} else {
				    							params.put(PARAMS_MESSAGES_KEY, "<errors><error id=\"emptyAuthParameter\">The authentication type selected is not valid.</error></errors>");
				    						}
			    						}
			    						
			    						if (noErrors && request.getParameter("webHookId").equals("new")){
			    							WebHookUpdateResult result = projSettings.addNewWebHook(myProject.getProjectId(), myProject.getExternalId(), request.getParameter("URL"), enabled, 
			    														states,request.getParameter("payloadFormat"), request.getParameter("payloadTemplate"), 
			    														buildTypeAll, buildTypeSubProjects, buildTypes, webHookAuthConfig);
			    							if(result.isUpdated()){
			    								myProject.persist();
			    	    						params.put(PARAMS_MESSAGES_KEY, "<errors /><webhook action='new' id='" + result.getWebHookConfig().getUniqueKey() + "'/>");
			    							} else {
			    								params.put("message", "<errors><error id=\"persistenceError\">Unable to perist webhook</error>");
			    							}
			    						} else if (noErrors) {
			    							WebHookUpdateResult result = projSettings.updateWebHook(myProject.getProjectId(),request.getParameter("webHookId"), 
			    														request.getParameter("URL"), enabled, 
			    														states, request.getParameter("payloadFormat"), request.getParameter("payloadTemplate"), 
			    														buildTypeAll, buildTypeSubProjects, buildTypes, webHookAuthConfig);
			    							if(result.isUpdated()){
			    								myProject.persist();
			    	    						params.put(PARAMS_MESSAGES_KEY, "<errors /><webhook action='update' id='" + result.getWebHookConfig().getUniqueKey() + "'/>");
			    							} else {
			    								params.put("message", "<errors><error id=\"persistenceError\">Unable to perist webhook</error>");
			    							}
			    						}
			    					} // TODO Need to handle webHookId being null
			    						
			    				} else {
			    					if ((request.getParameter("URL") == null ) 
				    				|| (request.getParameter("URL").length() == 0)){
			    						params.put(PARAMS_MESSAGES_KEY, "<errors><error id=\"emptyWebHookUrl\">Please enter a URL.</error></errors>");
			    					} else if ((request.getParameter("payloadFormat") == null)
				    				|| (request.getParameter("payloadFormat").length() == 0)){
			    						params.put(PARAMS_MESSAGES_KEY, "<errors><error id=\"emptyPayloadFormat\">Please choose a Payload Format.</error></errors>");
			    					}
			    				}
				    			
			    			}
			    		} else {
			    			params.put(PARAMS_MESSAGES_KEY, "<errors><error id=\"messageArea\">You do not appear to have permission to edit WebHooks.</error></errors>");
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
	        	
			    	WebHookProjectSettings projSettings = (WebHookProjectSettings) mySettings.getSettings(request.getParameter("projectId"), "webhooks");
			    	
			    	String message = projSettings.getWebHooksAsString();
			    	
			    	params.put("haveProject", "true");
			    	params.put(PARAMS_MESSAGES_KEY, message);
			    	params.put("projectId", project.getProjectId());
			    	params.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
			    	params.put("projectName", project.getName());
			    	
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
			    		
			    		params.put("projectWebHooksAsJson", ProjectWebHooksBeanGsonSerialiser.serialise(
								TemplatesAndProjectWebHooksBean.build(
										RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
																			myManager.getRegisteredFormats()), 
										ProjectWebHooksBean.build(projSettings, 
																	project, 
																	myManager.getRegisteredFormatsAsCollection(),
																	myTemplateResolver.findWebHookTemplatesForProject(project)
																	),
										//ProjectHistoryResolver.getProjectHistory(project),
										RegisteredWebhookAuthenticationTypesBean.build(myAuthenticatorProvider)
										)
									)
								);

			    	}
			    	
	        	} else {
	        		params.put("haveProject", "false");
	        	}
	        }
	        
	        return new ModelAndView(myPluginPath + "WebHook/ajaxEdit.jsp", params);
	    }
}
