package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;
import webhook.teamcity.testing.model.WebHookExecutionRequest;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookPreviewAndExecuteController extends BaseController {
	
	private final WebControllerManager myWebManager;
    private final String myPluginPath;
	private final WebHookUserRequestedExecutor myWebHookUserRequestedExecutor;
    
    public WebHookPreviewAndExecuteController(SBuildServer server, 
    		WebHookUserRequestedExecutor webHookUserRequestedExecutor,
    		PluginDescriptor pluginDescriptor, WebControllerManager webManager) {
        super(server);
        myWebManager = webManager;
        myPluginPath = pluginDescriptor.getPluginResourcesPath();
        myWebHookUserRequestedExecutor = webHookUserRequestedExecutor;
    }
    
    public void register(){
	      myWebManager.registerController("/webhooks/testWebHook.html", this);
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		if (isPost(request)) {
			
			HashMap<String,Object> params = new HashMap<>();
			Gson gson = new GsonBuilder().create();
			
			WebHookExecutionRequest webHookExecutionRequest = gson.fromJson(
					request.getReader(), 
					WebHookExecutionRequest.class
				);
			
			if (request.getParameter("action") != null && request.getParameter("action").equals("preview")) {
				
				params.put("templateRendering", 
						gson.toJson(
								myWebHookUserRequestedExecutor.requestWebHookPreview(webHookExecutionRequest))
						);
			} else if (request.getParameter("action") != null && request.getParameter("action").equals("execute")) {
				
				params.put("templateRendering", 
						gson.toJson(
								myWebHookUserRequestedExecutor.requestWebHookExecution(webHookExecutionRequest))
						);
			}
			return new ModelAndView(myPluginPath + "WebHook/templateRendering.jsp", params);
		}

		return null;
	}

}
