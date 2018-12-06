package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.extension.bean.template.EditTemplateRenderingBean;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookTemplateEditPageController extends WebHookTemplateBasePageController {

		private final WebHookPayloadManager myPayloadManager;
	
	    public WebHookTemplateEditPageController(SBuildServer server, WebControllerManager webManager, 
	    		PluginDescriptor pluginDescriptor, WebHookPayloadManager payloadManager, 
	    		WebHookPluginDataResolver webHookPluginDataResolver, WebHookTemplateManager webHookTemplateManager) {
	        super(server, webManager, pluginDescriptor, webHookPluginDataResolver, webHookTemplateManager);
	        this.myPayloadManager = payloadManager;
	    }

	    @Override
	    protected String getUrl() {
	    	return "/webhooks/template.html";
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	    	HashMap<String,Object> params = new HashMap<>();
	    	addBaseParams(params);
	    	
	    	if (request.getParameter(GET_VARIABLE_NAME_TEMPLATE) != null){
	    		
    			String templateName = request.getParameter(GET_VARIABLE_NAME_TEMPLATE);
    			if (templateName != null) {
    				
    				WebHookTemplateConfig templateConfig = myTemplateManager.getTemplateConfig(templateName);
    				
    				if (templateConfig != null) {
    					params.put("payloadFormats", myPayloadManager.getTemplatedFormats());
    					params.put("webhookTemplateBean", EditTemplateRenderingBean.build(templateConfig, myTemplateManager.getTemplateState(templateConfig.getId())));
    					return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/templateEdit.jsp", params);
    				}
    			}
	    		
	    	}
	    	return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/templateEditNotFound.jsp", params);
	    	
	    }


}
