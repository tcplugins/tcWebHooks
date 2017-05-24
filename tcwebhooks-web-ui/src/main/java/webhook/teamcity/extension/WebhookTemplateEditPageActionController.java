package webhook.teamcity.extension;
import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

/**
 * This class simply holds the actions available at  "/admin/manageWebhookTemplate.html"
 * Actions need to inject this class and register themselves.
 */
public class WebhookTemplateEditPageActionController extends BaseAjaxActionController {
	
	public static final String ACTION_TYPE = "action";
    public static final String DEBREPO_UUID = "debrepo.uuid";
    public static final String DEBREPO_NAME = "debrepo.name";
    public static final String DEBREPO_PROJECT_ID = "debrepo.project.id";
    public static final String DEBREPO_FILTER_ID = "debrepo.filter.id";
    public static final String DEBREPO_FILTER_REGEX = "debrepo.filter.regex";
    public static final String DEBREPO_FILTER_DIST = "debrepo.filter.dist";
    public static final String DEBREPO_FILTER_COMPONENT = "debrepo.filter.component";
    public static final String DEBREPO_FILTER_BUILD_TYPE_ID = "debrepo.filter.buildtypeid";
    
  public WebhookTemplateEditPageActionController(@NotNull final PluginDescriptor pluginDescriptor,
                                        	   @NotNull final WebControllerManager controllerManager) {
    super(controllerManager);
    controllerManager.registerController("/admin/webhookTemplateAction.html", this);
  }
    
}