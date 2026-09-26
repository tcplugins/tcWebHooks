package webhook.teamcity.server.rest.web;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookRestApiDocsController extends BaseController {

    public static final String DOCS_URL = "/webhooks/rest-api/index.html";

    private final PluginDescriptor pluginDescriptor;

    public WebHookRestApiDocsController(
            SBuildServer server,
            PluginDescriptor pluginDescriptor,
            WebControllerManager webControllerManager) {
        super(server);
        this.pluginDescriptor = pluginDescriptor;
        webControllerManager.registerController(DOCS_URL, this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        HashMap<String, Object> params = new HashMap<>();
        params.put("serverRootUrl", stripTrailingSlash(myServer.getRootUrl()));
        params.put("specUrl", pluginDescriptor.getPluginResourcesPath("WebHookRestApi/docs/swagger.json"));

        return new ModelAndView(pluginDescriptor.getPluginResourcesPath("WebHookRestApi/docs/restApiDocs.jsp"), params);
    }

    private String stripTrailingSlash(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.endsWith("/") ? input.substring(0, input.length() - 1) : input;
    }
}
