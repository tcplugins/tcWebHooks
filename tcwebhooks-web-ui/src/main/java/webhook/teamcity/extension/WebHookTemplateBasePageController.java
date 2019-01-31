package webhook.teamcity.extension;

import java.util.HashMap;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookSettingsManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class WebHookTemplateBasePageController extends BaseController {

	    static final String GET_VARIABLE_NAME_TEMPLATE = "template";
		final WebControllerManager myWebManager;
	    final PluginDescriptor myPluginDescriptor;
		final WebHookTemplateManager myTemplateManager;
		final WebHookPluginDataResolver myWebHookPluginDataResolver;
		final WebHookSettingsManager myWebHookSettingsManager;

	    public WebHookTemplateBasePageController(SBuildServer server, WebControllerManager webManager, 
	    		PluginDescriptor pluginDescriptor, WebHookPluginDataResolver webHookPluginDataResolver,
	    		WebHookTemplateManager webHookTemplateManager, WebHookSettingsManager webHookSettingsManager) {
	        super(server);
	        myWebManager = webManager;
	        myPluginDescriptor = pluginDescriptor;
	        myWebHookPluginDataResolver = webHookPluginDataResolver;
	        myTemplateManager = webHookTemplateManager;
	        myWebHookSettingsManager = webHookSettingsManager;
	    }
	    
	    protected abstract String getUrl();

	    public void register(){
	      myWebManager.registerController(getUrl(), this);
	    }

		protected void addBaseParams(HashMap<String, Object> params) {
			params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
	    	params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
	    	params.put("rootContext", myServer.getServerRootPath());
	    	params.put("isRestApiInstalled", myWebHookPluginDataResolver.isWebHooksRestApiInstalled());
		}

}
