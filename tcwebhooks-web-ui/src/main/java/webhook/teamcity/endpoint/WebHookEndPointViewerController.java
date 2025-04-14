package webhook.teamcity.endpoint;

import static webhook.teamcity.payload.util.StringUtils.stripTrailingSlash;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookEndPointViewerController extends BaseController {
	private static final Logger LOG = Logger.getInstance(WebHookEndPointViewerController.class.getName());
	
	public static final String MY_URL = "/webhooks/endpoint-viewer.html";
	private final WebHookEndPointContentStore endPointContentStore;
	private final WebControllerManager myWebManager;
	private String myPluginPath;
	
	public WebHookEndPointViewerController(	SBuildServer server,
								WebHookEndPointContentStore endPointContentStore, 
								PluginDescriptor pluginDescriptor, 
								WebControllerManager webControllerManager) {
		super(server);
		this.endPointContentStore = endPointContentStore;
		this.myPluginPath = pluginDescriptor.getPluginResourcesPath();
		this.myWebManager = webControllerManager;
		this.myWebManager.registerController(MY_URL, this);
		LOG.debug("WebHookEndPointViewerController:: Registering");
	}

    @Nullable
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	if (request.getMethod().equalsIgnoreCase("post") && 
    			request.getParameter("delete") != null){
    		endPointContentStore.store.clear();
    		response.sendRedirect(stripTrailingSlash(myServer.getRootUrl()) + WebHookEndPointViewerController.MY_URL);
    		return null;
    		
    	} else if (request.getMethod().equalsIgnoreCase("post")){
    		
    		final PrintWriter writer = response.getWriter();
    		  writer.write("Please don't POST here. POST test webhook requests to " + stripTrailingSlash(myServer.getRootUrl()) + WebHookEndPointController.MY_URL);
    		  writer.write("\r\n"); 
    		
    	} else if (request.getMethod().equalsIgnoreCase("get")){
    		
    		HashMap<String,Object> params = new HashMap<>();
    		params.put("jspHome",this.myPluginPath);
    		params.put("postURL", stripTrailingSlash(myServer.getRootUrl()) + WebHookEndPointController.MY_URL);
    		params.put("count", endPointContentStore.store.size());
    		params.put("storeItems", endPointContentStore.getAll());
    		
    	    return new ModelAndView(myPluginPath + "WebHook/endpointRequests.jsp", params); 
    		
    	}
    	
    	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    	return null;
    }
    
}
