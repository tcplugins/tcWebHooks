package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.extension.bean.template.EditTemplateRenderingBean;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.config.WebHookTemplateConfig;


public class WebHookTemplateEditPageController extends BaseController {

	    private static final String GET_VARIABLE_NAME_ACTION = "action";
	    private static final String GET_VARIABLE_NAME_TEMPLATE = "template";
		private final WebControllerManager myWebManager;
	    private final WebHookMainSettings myMainSettings;
	    private SBuildServer myServer;
	    private ProjectSettingsManager mySettings;
	    private PluginDescriptor myPluginDescriptor;
	    private final WebHookPayloadManager myManager;
		private final WebHookTemplateResolver myTemplateResolver;
		private final WebHookTemplateManager myTemplateManager;

	    public WebHookTemplateEditPageController(SBuildServer server, WebControllerManager webManager, 
	    		ProjectSettingsManager settings, PluginDescriptor pluginDescriptor, WebHookPayloadManager manager, 
	    		WebHookTemplateResolver templateResolver, WebHookTemplateManager webHookTemplateManager,
	    		WebHookMainSettings configSettings) {
	        super(server);
	        myWebManager = webManager;
	        myServer = server;
	        mySettings = settings;
	        myPluginDescriptor = pluginDescriptor;
	        myMainSettings = configSettings;
	        myManager = manager;
	        myTemplateResolver = templateResolver;
	        myTemplateManager = webHookTemplateManager;
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/templateModify.html", this);
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	    	HashMap<String,Object> params = new HashMap<String,Object>();
	    	params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
	    	params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
	    	params.put("rootContext", myServer.getServerRootPath());
	    	
	    	
	    	
	    	if (request.getParameter(GET_VARIABLE_NAME_ACTION) != null){
	    		
	    		
	    		if (request.getParameter(GET_VARIABLE_NAME_ACTION).equals("edit")){
	    			
	    			String templateName = request.getParameter(GET_VARIABLE_NAME_TEMPLATE).toString();
	    			if (templateName != null) {
	    				WebHookTemplateConfig templateConfig = myTemplateManager.getTemplateConfig(templateName);
	    				params.put("webhookTemplateBean", EditTemplateRenderingBean.build(templateConfig));
	    			}
	    			
	    			
	    		} else if (request.getParameter(GET_VARIABLE_NAME_ACTION).equals("override")){
	    			
	    			String templateName = request.getParameter(GET_VARIABLE_NAME_TEMPLATE).toString();
	    			if (templateName != null) {
	    				WebHookTemplateConfig templateConfig = myTemplateManager.getTemplateConfig(templateName);
	    				params.put("webhookTemplateBean", EditTemplateRenderingBean.build(templateConfig));
	    			}
	    			
	    		} else if (request.getParameter(GET_VARIABLE_NAME_ACTION).equals("clone")){
	    			
	    		}
	    		
	    		
	    	}
	    	
	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/templateEdit.jsp", params);
	    }

		private String getProjectName(String externalProjectId, String name) {
			if (externalProjectId.equalsIgnoreCase("_Root")){
				return externalProjectId;
			}
			return name;
		}
}
