package webhook.teamcity.server.rest.web;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

/**
 * This class simply holds the actions available at  "/admin/manageWebHooksRestApi.html"
 * Actions need to inject this class and register themselves.
 */
public class WebHookRestApiActionController extends BaseAjaxActionController {
	
	public static final String ACTION_TYPE = "action";
    
  public WebHookRestApiActionController(@NotNull final PluginDescriptor pluginDescriptor,
                                        	   @NotNull final WebControllerManager controllerManager) {
    super(controllerManager);
    controllerManager.registerController("/admin/manageWebHooksRestApi.html", this);
  }
    
}