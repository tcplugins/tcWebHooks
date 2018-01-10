package webhook.teamcity.extension;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.extension.bean.ProjectWebHooksBeanGsonSerialiser;
import webhook.teamcity.extension.util.ProjectHistoryResolver;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookProjectHistoryController extends BaseController {
	
	private ProjectManager myProjectManager;
	private String myPluginPath;
	private WebControllerManager myWebControllerManager;

	public WebHookProjectHistoryController(
			ProjectManager projectManager,
			PluginDescriptor pluginDescriptor,
			WebControllerManager webControllerManager
			) {
		
		myProjectManager = projectManager;
		myPluginPath = pluginDescriptor.getPluginResourcesPath();
		myWebControllerManager = webControllerManager;
	}
	
    public void register(){
    	myWebControllerManager.registerController("/webhooks/ajax/projectHistory.html", this);
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Map<String,Object> params = new HashMap<>();
		
		if (request.getParameter("projectId") != null) {
			params.put("historyRendering",
					ProjectWebHooksBeanGsonSerialiser.serialise(
							ProjectHistoryResolver.getProjectHistory(
									myProjectManager.findProjectByExternalId(request.getParameter("projectId"))
									)
							)
					);
		} else if (request.getParameter("buildTypeId") != null) {
			params.put("historyRendering",
					ProjectWebHooksBeanGsonSerialiser.serialise(
							ProjectHistoryResolver.getBuildHistory(
									myProjectManager.findBuildTypeByExternalId(request.getParameter("buildTypeId"))
									)
							)
					);
		}
		return new ModelAndView(myPluginPath + "WebHook/projectHistoryRendering.jsp", params);
	}

}
