package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.extension.bean.WebHookTestHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookExecutionRequestGsonBuilder;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookPreviewAndExecuteController extends BaseController {
	
	private static final String PARAM_ACTION = "action";
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
			Gson gson = WebHookExecutionRequestGsonBuilder.gsonBuilder();
			
			WebHookExecutionRequest webHookExecutionRequest = gson.fromJson(
					request.getReader(), 
					WebHookExecutionRequest.class
				);
			
			if (request.getParameter(PARAM_ACTION) != null && request.getParameter(PARAM_ACTION).equals("preview")) {
				
				params.put("templateRendering", 
						gson.toJson(
								myWebHookUserRequestedExecutor.requestWebHookPreview(webHookExecutionRequest))
						);
			} else if (request.getParameter(PARAM_ACTION) != null && request.getParameter(PARAM_ACTION).equals("execute")) {
				
				WebHookHistoryItem webHookHistoryItem = myWebHookUserRequestedExecutor.requestWebHookExecution(webHookExecutionRequest);
				
				WebHookTestHistoryItem.ErrorStatus error = null;
		        if (webHookHistoryItem.getWebhookErrorStatus() != null) {
		        	error = new WebHookTestHistoryItem.ErrorStatus(webHookHistoryItem.getWebhookErrorStatus().getMessage(), webHookHistoryItem.getWebhookErrorStatus().getErrorCode());
		        }
				
				params.put("templateRendering", 
						gson.toJson(WebHookTestHistoryItem
								.builder()
								.dateTime(webHookHistoryItem.getTimestamp().toString())
								.trackingId(webHookHistoryItem.getWebHookExecutionStats().getTrackingIdAsString())
								.url(getUrl(webHookHistoryItem))
								.executionTime(String.valueOf(webHookHistoryItem.getWebHookExecutionStats().getTotalExecutionTime()) + " ms")
								.statusCode(webHookHistoryItem.getWebHookExecutionStats().getStatusCode())
								.statusReason(webHookHistoryItem.getWebHookExecutionStats().getStatusReason())
								.error(error)
								.build())					
					);
			}
			return new ModelAndView(myPluginPath + "WebHook/templateRendering.jsp", params);
		}

		return null;
	}
	
	private String getUrl(WebHookHistoryItem webHookConfig) {
		if (webHookConfig != null &&
				webHookConfig.getWebHookExecutionStats() != null &&
				webHookConfig.getWebHookExecutionStats().getUrl() != null &&
				!webHookConfig.getWebHookExecutionStats().getUrl().trim().isEmpty() )
		{
			return webHookConfig.getWebHookExecutionStats().getUrl();
			
		} else if (webHookConfig != null &&
				webHookConfig.getUrl() != null &&
				webHookConfig.getUrl().trim().isEmpty()) {
			return webHookConfig.getUrl();
		}
		return "";
	}
	
}
