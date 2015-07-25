package webhook.teamcity.docs.rest;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Authentication.SendSuccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RamlFileServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	File baseRamlPath; 
	public RamlFileServlet(File file) {
		this.baseRamlPath = file;
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		RamlFile ramlFile = null;
		if (request.getPathInfo() != null){
			System.out.println("GET - File :: " + request.getPathInfo());
			File file = new File(WebHookApiServer.RAML_FILE_LOCATION + request.getPathInfo());
			if (file.isFile()){
				ramlFile = new RamlFile(file);
				try {
					ramlFile.loadContents();
				} catch (IOException io){
					response.sendError(500);
					return;
				}
			}
		}
		
		if (ramlFile != null){
			Gson gson = new GsonBuilder()
		    .excludeFieldsWithoutExposeAnnotation()
		    .create();
			response.setContentType("application/json");
			response.getWriter().print(gson.toJson(ramlFile));
		} else {
			response.sendError(404);
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("POST - File");
		response.setContentType("application/json");
	}
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("PUT - File");
		response.setContentType("application/json");
	}
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("DELETE - File");
		response.setContentType("application/json");
	}

}
