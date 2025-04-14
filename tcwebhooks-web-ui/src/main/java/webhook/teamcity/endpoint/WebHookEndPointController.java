package webhook.teamcity.endpoint;

import java.io.BufferedReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;
import webhook.teamcity.payload.util.StringUtils;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookEndPointController extends BaseController {
	private static final Logger LOG = Logger.getInstance(WebHookEndPointController.class.getName());

	public static final String MY_URL = "/webhooks/endpoint.html";
	private final WebHookEndPointContentStore endPointContentStore;
	private final WebControllerManager myWebManager;
	
	public WebHookEndPointController(	SBuildServer server,
								WebHookEndPointContentStore endPointContentStore, 
								WebControllerManager webControllerManager,
								AuthorizationInterceptor authorizationInterceptor) {
		super(server);
		this.endPointContentStore = endPointContentStore;
		this.myWebManager = webControllerManager;
		this.myWebManager.registerController(MY_URL, this);
		authorizationInterceptor.addPathNotRequiringAuth(MY_URL);
		LOG.debug("WebHookEndPointController:: Registering");
	}

    @Nullable
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	if (isPost(request)) {
    		boolean debug = LOG.isDebugEnabled();
    		
    		if (debug) { 
    			LOG.debug(WebHookEndPointController.this.getClass().getName() + ":: Showing received content.");
    		}
			
			// Read from request
			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				if (debug) LOG.debug(line);
			}
			
			Map<String, String> headers = new TreeMap<>();
			
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String key = headerNames.nextElement();
				String value = request.getHeader(key);
				headers.put(key, value);
			}
			
			StringBuffer url = request.getRequestURL();
			if (request.getQueryString() != null){
				url.append("?")
				   .append(request.getQueryString());
			}
			
			WebHookEndPointPayload payload;
			
			
			if (request.getContentType().toLowerCase().startsWith(WebHookPayloadNameValuePairs.FORMAT_CONTENT_TYPE)){
				
				payload = WebHookEndPointPayload.builder()
						.date(new Date())
						.contentType(request.getContentType())
						.parameters(request.getParameterMap())
						.headers(headers)
						.url(url.toString())
						.build().generateHash();
				
			} else {
			
				payload = WebHookEndPointPayload.builder()
							.date(new Date())
							.contentType(request.getContentType())
							.payload(buffer.toString())
							.headers(headers)
							.url(url.toString())
							.build().generateHash();
			}
			
			endPointContentStore.put(payload);
			
			
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
    		
    	} else if (request.getMethod().equalsIgnoreCase("get")){
    		response.sendRedirect(StringUtils.stripTrailingSlash(myServer.getRootUrl()) + WebHookEndPointViewerController.MY_URL);
    	}
    	
    	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    	return null;
    }
}
