package webhook.teamcity.endpoint;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

public class EndPointController extends BaseController {
	
	
	private static final String MY_URL = "/webhooks/endpoint.html";
	private final EndPointContentStore endPointContentStore;
	private final WebControllerManager myWebManager;
	
	public EndPointController(	SBuildServer server,
								EndPointContentStore endPointContentStore, 
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
    		EndPointPayload payload = new EndPointPayload();
    		payload.date = new Date();
    		payload.contentType = request.getContentType();
    		
    	    // Read from request
    	    StringBuilder buffer = new StringBuilder();
    	    BufferedReader reader = request.getReader();
    	    String line;
    	    while ((line = reader.readLine()) != null) {
    	        buffer.append(line);
    	        Loggers.SERVER.info(line);
    	    }
    	    payload.payload = buffer.toString();
    		
    		endPointContentStore.put(payload);
    		
    		
    		response.setStatus(HttpServletResponse.SC_CREATED);
    		return null;
    		
    	} else if (request.getMethod().equalsIgnoreCase("get")){
    	    response.setContentType("text/plain"); 
    	    response.setCharacterEncoding("utf-8"); 
    	    final PrintWriter writer = response.getWriter(); 
    	    final List<EndPointPayload> data = endPointContentStore.getAll(); 
    	    writer.write("Recently called " + data.size() + " webhook requests: "); 
    	    for (EndPointPayload req : data) { 
    	      writer.write(req.contentType); 
    	      writer.write("\r\n"); 
    	      writer.write(req.payload); 
    	      writer.write("\r\n"); 
    	      writer.write("\r\n"); 
    	    } 
    	    return null; 
    		
    	}
    	
    	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    	return null;
    }
}
