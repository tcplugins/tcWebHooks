package webhook.teamcity.extension;

import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.AllArgsConstructor;
import lombok.Data;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookJavascriptTestsController extends BaseController {

	private PluginDescriptor myPluginDescriptor;

	public WebHookJavascriptTestsController(
			SBuildServer server, 
			WebControllerManager webControllerManager,
    		PluginDescriptor pluginDescriptor) {
		super(server);
		this.myPluginDescriptor = pluginDescriptor;
		webControllerManager.registerController("/webhooks/js/tests.html", this);
    }

    @Nullable
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HashMap<String,Object> params = new HashMap<>();
        params.put("jspHome", myPluginDescriptor.getPluginResourcesPath());
        params.put("buildList", Arrays.asList(
        		new BuildInfo("Project_MyBuildId01", "My Build 01"), 
        		new BuildInfo("Project_MyBuildId02", "My Build 02"), 
        		new BuildInfo("Project_MyBuildId03", "My Build 03")));
        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/webhookJavascriptTests.jsp", params);
    }
    
    @Data
    @AllArgsConstructor
    public static class BuildInfo {
        private String externalId;
        private String name;
    }

}
