package webhook.teamcity.docs.rest;


import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebHookApiServer {
	public static final String RAML_FILE_LOCATION = "./src/main/raml";
	Server server;

	public WebHookApiServer (String host, Integer port) {
		server = new Server(port);
		
		ContextHandler staticCtx = new ContextHandler();
		staticCtx.setContextPath("/");
		staticCtx.setResourceBase("./src/main/webcontent/");
		
		ResourceHandler resourceHandler = new ResourceHandler();
		staticCtx.setHandler(resourceHandler);
		
		ContextHandler servletCtx = new ServletContextHandler();
		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(new ServletHolder(new RamlFileServlet(new File(RAML_FILE_LOCATION))),"/raml/file/*");
		servletHandler.addServletWithMapping(new ServletHolder(new RamlFilesServlet(new File(RAML_FILE_LOCATION))),"/raml/files");
		servletCtx.setContextPath("/");
		servletCtx.setHandler(servletHandler);
		
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.addHandler(staticCtx);
		contexts.addHandler(servletCtx);
		
		server.setHandler(contexts);
		
	}
	
	public static void main(String[] args) throws Exception {
		WebHookApiServer apiServer = new WebHookApiServer("localhost", 1234);
		apiServer.server.start();
		apiServer.server.join();
	}
	
}
