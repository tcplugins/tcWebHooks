package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

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
import webhook.teamcity.Loggers;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.extension.bean.ProjectWebHooksBean;
import webhook.teamcity.extension.bean.ProjectWebHooksBeanGsonSerialiser;
import webhook.teamcity.extension.bean.RegisteredWebhookAuthenticationTypesBean;
import webhook.teamcity.extension.bean.TemplatesAndProjectWebHooksBean;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.extension.util.ProjectHistoryResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;


public class WebHookIndexPageController extends BaseController {

	    private final WebControllerManager myWebManager;
	    private final WebHookMainSettings myMainSettings;
	    private SBuildServer myServer;
	    private ProjectSettingsManager mySettings;
	    private PluginDescriptor myPluginDescriptor;
	    private final WebHookPayloadManager myManager;
		private final WebHookTemplateResolver myTemplateResolver;
		private final WebHookAuthenticatorProvider myAuthenticatorProvider;

	    public WebHookIndexPageController(SBuildServer server, WebControllerManager webManager, 
	    		ProjectSettingsManager settings, PluginDescriptor pluginDescriptor, WebHookPayloadManager manager, 
	    		WebHookTemplateResolver templateResolver,
	    		WebHookMainSettings configSettings, WebHookAuthenticatorProvider authenticatorProvider) {
	        super(server);
	        myWebManager = webManager;
	        myServer = server;
	        mySettings = settings;
	        myPluginDescriptor = pluginDescriptor;
	        myMainSettings = configSettings;
	        myManager = manager;
	        myTemplateResolver = templateResolver;
	        myAuthenticatorProvider = authenticatorProvider;
	        
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/index.html", this);
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<String,Object>();
	        params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
        	params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
        	params.put("rootContext", myServer.getServerRootPath());
        	params.put("pluginVersion", myPluginDescriptor.getPluginVersion());
	        
	    	if (myMainSettings.getInfoUrl() != null && myMainSettings.getInfoUrl().length() > 0){
	    		params.put("moreInfoText", "<li><a href=\"" + myMainSettings.getInfoUrl() + "\">" + myMainSettings.getInfoText() + "</a></li>");
	    		if (myMainSettings.getWebhookShowFurtherReading()){
	    			params.put("ShowFurtherReading", "ALL");
	    		} else {
	    			params.put("ShowFurtherReading", "SINGLE");
	    		}
	    	} else if (myMainSettings.getWebhookShowFurtherReading()){
	    		params.put("ShowFurtherReading", "DEFAULT");
	    	} else {
	    		params.put("ShowFurtherReading", "NONE");
	    	}
	        
	        if(request.getParameter("projectId") != null){
	        	
	        	SProject project = TeamCityIdResolver.findProjectById(this.myServer.getProjectManager(), request.getParameter("projectId"));
	        	if (project != null){
	        		
			    	WebHookProjectSettings projSettings = (WebHookProjectSettings) 
			    			mySettings.getSettings(project.getProjectId(), "webhooks");
			    	
			        SUser myUser = SessionUser.getUser(request);
			        params.put("hasPermission", myUser.isPermissionGrantedForProject(project.getProjectId(), Permission.EDIT_PROJECT));
			    	
			    	String message = projSettings.getWebHooksAsString();
			    	
			    	params.put("haveProject", "true");
			    	params.put("messages", message);
			    	params.put("projectId", project.getProjectId());
			    	params.put("buildTypeList", project.getBuildTypes());
			    	params.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
			    	params.put("projectName", getProjectName(TeamCityIdResolver.getExternalProjectId(project), project.getName()));
			    	
			    	Loggers.SERVER.debug(myMainSettings.getInfoText() + myMainSettings.getInfoUrl() + myMainSettings.getProxyListasString());
			    	
			    	params.put("webHookCount", projSettings.getWebHooksCount());
			    	params.put("formatList", RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
							myManager.getRegisteredFormats()).getTemplateList());
			    	
			    	if (projSettings.getWebHooksCount() == 0){
			    		params.put("noWebHooks", "true");
			    		params.put("webHooks", "false");
			    		params.put("projectWebHooksAsJson", ProjectWebHooksBeanGsonSerialiser.serialise(
								TemplatesAndProjectWebHooksBean.build(
										RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
																			myManager.getRegisteredFormats()), 
										ProjectWebHooksBean.build(projSettings, 
																	project, 
																	myManager.getRegisteredFormatsAsCollection(),
																	myTemplateResolver.findWebHookTemplatesForProject(project)
																),
										ProjectHistoryResolver.getProjectHistory(project),
										RegisteredWebhookAuthenticationTypesBean.build(myAuthenticatorProvider)
									)
								)
							);
			    	} else {
			    		params.put("noWebHooks", "false");
			    		params.put("webHooks", "true");
			    		
			    		params.put("webHookList", ProjectWebHooksBean.buildWithoutNew(projSettings, 
																			project, 
																			myManager.getRegisteredFormatsAsCollection(),
																			myTemplateResolver.findWebHookTemplatesForProject(project))
							);
			    		
			    		//params.put("webHookList", projSettings.getWebHooksAsList());
			    		params.put("webHooksDisabled", !projSettings.isEnabled());
			    		params.put("webHooksEnabledAsChecked", projSettings.isEnabledAsChecked());
			    		//params.put("projectWebHooksAsJson", ProjectWebHooksBeanJsonSerialiser.serialise(ProjectWebHooksBean.build(projSettings, project, myManager.getRegisteredFormatsAsCollection())));
			    		
			    		params.put("projectWebHooksAsJson", ProjectWebHooksBeanGsonSerialiser.serialise(
								TemplatesAndProjectWebHooksBean.build(
										RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
																			myManager.getRegisteredFormats()), 
										ProjectWebHooksBean.build(projSettings, 
																	project, 
																	myManager.getRegisteredFormatsAsCollection(),
																	myTemplateResolver.findWebHookTemplatesForProject(project)
																),
										ProjectHistoryResolver.getProjectHistory(project),
										RegisteredWebhookAuthenticationTypesBean.build(myAuthenticatorProvider)
										)
									)
								);

			    	}
		    	} else {
		    		params.put("haveProject", "false");
		    		params.put("errorReason", "The project requested does not appear to be valid.");
		    	}
        	} else if (request.getParameter("buildTypeId") != null){
        		SBuildType sBuildType = TeamCityIdResolver.findBuildTypeById(this.myServer.getProjectManager(), request.getParameter("buildTypeId"));
        		if (sBuildType != null){
		        	SProject project = sBuildType.getProject();
		        	if (project != null){
		        		
				    	WebHookProjectSettings projSettings = (WebHookProjectSettings) 
				    			mySettings.getSettings(project.getProjectId(), "webhooks");
				    	
				    	SUser myUser = SessionUser.getUser(request);
				        params.put("hasPermission", myUser.isPermissionGrantedForProject(project.getProjectId(), Permission.EDIT_PROJECT));
				    	
			    		ProjectWebHooksBean bean = ProjectWebHooksBean.buildWithoutNew(projSettings, sBuildType,
								project, 
								myManager.getRegisteredFormatsAsCollection(),
								myTemplateResolver.findWebHookTemplatesForProject(project)
			    				);
				    	
			    		params.put("webHookList", bean);
				    	params.put("formatList", RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
								myManager.getRegisteredFormats()).getTemplateList());
				    	params.put("webHooksDisabled", !projSettings.isEnabled());
				    	params.put("projectId", project.getProjectId());
				    	params.put("haveProject", "true");
				    	params.put("projectName", getProjectName(TeamCityIdResolver.getExternalProjectId(project), project.getName()));
				    	params.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
				    	params.put("haveBuild", "true");
				    	params.put("buildName", sBuildType.getName());
				    	params.put("buildExternalId", TeamCityIdResolver.getExternalBuildId(sBuildType));
				    	params.put("buildTypeList", project.getBuildTypes());
			    		params.put("noWebHooks", bean.getWebHookList().size() == 0);
			    		params.put("webHooks", bean.getWebHookList().size() != 0);
				    	
			    		params.put("projectWebHooksAsJson", ProjectWebHooksBeanGsonSerialiser.serialise(
								TemplatesAndProjectWebHooksBean.build(
										RegisteredWebHookTemplateBean.build(myTemplateResolver.findWebHookTemplatesForProject(project),
																			myManager.getRegisteredFormats()), 
										ProjectWebHooksBean.build(projSettings, sBuildType, project, myManager.getRegisteredFormatsAsCollection(),
																	myTemplateResolver.findWebHookTemplatesForProject(project)
																	),
										ProjectHistoryResolver.getBuildHistory(sBuildType),
										RegisteredWebhookAuthenticationTypesBean.build(myAuthenticatorProvider)
										)
									)
								);			    				
		        	}
        		} else {
		    		params.put("haveProject", "false");
		    		params.put("errorReason", "The build requested does not appear to be valid.");
        		}
	        } else {
	        	params.put("haveProject", "false");
	        	params.put("errorReason", "No project specified.");
	        }

	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/index.jsp", params);
	    }

		private String getProjectName(String externalProjectId, String name) {
			if (externalProjectId.equalsIgnoreCase("_Root")){
				return externalProjectId;
			}
			return name;
		}
}
