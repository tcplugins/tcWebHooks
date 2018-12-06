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
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookTemplateListPageController extends WebHookTemplateBasePageController {

	    private final WebHookPayloadManager myPayloadManager;

		public WebHookTemplateListPageController(SBuildServer server, WebControllerManager webManager, 
	    		PluginDescriptor pluginDescriptor, WebHookPayloadManager payloadManager, 
	    		WebHookPluginDataResolver webHookPluginDataResolver, WebHookTemplateManager webHookTemplateManager) {
	    	super(server, webManager, pluginDescriptor, webHookPluginDataResolver, webHookTemplateManager);
	    	this.myPayloadManager = payloadManager;
	    }

	    @Override
	    protected String getUrl() {
	    	return "/webhooks/templates.html";
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<>();
	        addBaseParams(params);
	        
	        params.put("payloadFormats", myPayloadManager.getTemplatedFormats());
        	params.put("webHookTemplates", RegisteredWebHookTemplateBean.build(myTemplateManager, myTemplateManager.getRegisteredTemplates(),
        			myPayloadManager.getRegisteredFormats()).getTemplateList());

	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/templateList.jsp", params);
	    }


}
