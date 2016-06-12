package webhook.teamcity.endpoint;

import java.io.BufferedReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.Loggers;

public class WebHookEndPointController extends BaseController {
	
	
	public static final String MY_URL = "/webhooks/endpoint.html";
	private final WebHookEndPointContentStore endPointContentStore;
	private final WebControllerManager myWebManager;
	
	public WebHookEndPointController(	SBuildServer server,
								WebHookEndPointContentStore endPointContentStore, 
								PluginDescriptor pluginDescriptor, 
								WebControllerManager webControllerManager,
								AuthorizationInterceptor authorizationInterceptor) {
		super(server);
		this.endPointContentStore = endPointContentStore;
		this.myWebManager = webControllerManager;
		this.myWebManager.registerController(MY_URL, this);
		authorizationInterceptor.addPathNotRequiringAuth(MY_URL);
		Loggers.SERVER.info("EndPointController:: Registering");
	}

    @Nullable
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	if (request.getMethod().equalsIgnoreCase("post")){
			
			// Read from request
			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
			buffer.append(line);
			Loggers.SERVER.info(line);
			}
			
			Map<String, String> headers = new TreeMap<String, String>();
			
			//request.getH
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			headers.put(key, value);
			}
			
			StringBuffer url = request.getRequestURL();
			if (request.getQueryString() != null){
				url.append("?")
				   .append(request.getQueryString());
			}
			
			WebHookEndPointPayload payload = WebHookEndPointPayload.builder()
			.date(new Date())
			.contentType(request.getContentType())
			.payload(buffer.toString())
			.headers(headers)
			.url(url.toString())
			.build();
			
			endPointContentStore.put(payload);
			
			
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    		
    	} else if (request.getMethod().equalsIgnoreCase("get")){
    		response.sendRedirect(WebHookEndPointViewerController.MY_URL);
    	}
    	
    	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    	return null;
    }
}
