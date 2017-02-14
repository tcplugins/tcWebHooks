package webhook.teamcity.extension;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookMainSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;


public class WebHookTemplateListPageController extends BaseController {

    private final WebControllerManager myWebManager;
    private final WebHookMainSettings myMainSettings;
    private SBuildServer myServer;
    private ProjectSettingsManager mySettings;
    private PluginDescriptor myPluginDescriptor;
    private final WebHookPayloadManager myManager;
    private final WebHookTemplateResolver myTemplateResolver;
    private final WebHookTemplateManager myTemplateManager;

    public WebHookTemplateListPageController(SBuildServer server, WebControllerManager webManager,
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

    public void register() {
        myWebManager.registerController("/webhooks/templates.html", this);
    }

    @Nullable
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("jspHome", this.myPluginDescriptor.getPluginResourcesPath());
        params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
        params.put("rootContext", myServer.getServerRootPath());

        params.put("webHookTemplates", RegisteredWebHookTemplateBean.build(myTemplateManager, myTemplateManager.getRegisteredTemplates(),
                myManager.getRegisteredFormats()).getTemplateList());

        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/templateList.jsp", params);
    }

    private String getProjectName(String externalProjectId, String name) {
        if (externalProjectId.equalsIgnoreCase("_Root")) {
            return externalProjectId;
        }
        return name;
    }
}
