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
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookTemplateListPageController extends BaseController {

	    private final WebControllerManager myWebManager;
	    private PluginDescriptor myPluginDescriptor;
	    private final WebHookPayloadManager myManager;
		private final WebHookTemplateManager myTemplateManager;
		private final WebHookPluginDataResolver myWebHookPluginDataResolver;

	    public WebHookTemplateListPageController(SBuildServer server, WebControllerManager webManager, 
	    		PluginDescriptor pluginDescriptor, WebHookPayloadManager manager, 
	    		WebHookPluginDataResolver webHookPluginDataResolver, WebHookTemplateManager webHookTemplateManager) {
	        super(server);
	        myWebManager = webManager;
	        myServer = server;
	        myPluginDescriptor = pluginDescriptor;
	        myManager = manager;
	        myWebHookPluginDataResolver = webHookPluginDataResolver;
	        myTemplateManager = webHookTemplateManager;
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/templates.html", this);
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<>();
	        params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
        	params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
        	params.put("rootContext", myServer.getServerRootPath());
        	params.put("isRestApiInstalled", myWebHookPluginDataResolver.isWebHooksRestApiInstalled());
	        
        	params.put("webHookTemplates", RegisteredWebHookTemplateBean.build(myTemplateManager, myTemplateManager.getRegisteredTemplates(),
					myManager.getRegisteredFormats()).getTemplateList());

	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/templateList.jsp", params);
	    }

}
