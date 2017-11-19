package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.extension.bean.template.EditTemplateRenderingBean;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookTemplateEditPageController extends BaseController {

	    private static final String GET_VARIABLE_NAME_TEMPLATE = "template";
		private final WebControllerManager myWebManager;
	    private PluginDescriptor myPluginDescriptor;
		private final WebHookTemplateManager myTemplateManager;
		private final WebHookPluginDataResolver myWebHookPluginDataResolver;

	    public WebHookTemplateEditPageController(SBuildServer server, WebControllerManager webManager, 
	    		PluginDescriptor pluginDescriptor, WebHookPluginDataResolver webHookPluginDataResolver,
	    		WebHookTemplateManager webHookTemplateManager) {
	        super(server);
	        myWebManager = webManager;
	        myPluginDescriptor = pluginDescriptor;
	        myWebHookPluginDataResolver = webHookPluginDataResolver;
	        myTemplateManager = webHookTemplateManager;
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/template.html", this);
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	    	HashMap<String,Object> params = new HashMap<>();
	    	params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
	    	params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
	    	params.put("rootContext", myServer.getServerRootPath());
	    	params.put("isRestApiInstalled", myWebHookPluginDataResolver.isWebHooksRestApiInstalled());
	    	
	    	
	    	if (request.getParameter(GET_VARIABLE_NAME_TEMPLATE) != null){
	    		
    			String templateName = request.getParameter(GET_VARIABLE_NAME_TEMPLATE);
    			if (templateName != null) {
    				WebHookTemplateConfig templateConfig = myTemplateManager.getTemplateConfig(templateName);
    				params.put("webhookTemplateBean", EditTemplateRenderingBean.build(templateConfig, myTemplateManager.getTemplateState(templateConfig.getId())));
    			}
	    		
	    	}
	    	
	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/templateEdit.jsp", params);
	    }

}
