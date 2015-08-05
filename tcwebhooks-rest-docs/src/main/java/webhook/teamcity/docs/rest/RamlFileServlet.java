package webhook.teamcity.docs.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Response;

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
		if (request.getPathInfo() != null){
			getFile(request, response);
		} else {
			getFileList(response); 
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		createNewFile(request, response);
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		updateExistingFile(request, response);
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		deleteExistingFile(request, response);
	}
	
	
	private void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
		RamlFile ramlFile = null;
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
	
	private void getFileList(HttpServletResponse response) throws IOException {
		System.out.println("GET - Files");
		RamlFile rootDir = new RamlFile(baseRamlPath);
		if (baseRamlPath.isDirectory()){
			rootDir.setChildren(iterateOverChildren(baseRamlPath));
		}
		
		rootDir.path = "/";
		rootDir.name = "/";
		
		Gson gson = new GsonBuilder()
		.excludeFieldsWithoutExposeAnnotation()
		.create();
		response.setContentType("application/json");
		response.getWriter().print(gson.toJson(rootDir));
	}
	
	private List<RamlFile> iterateOverChildren(File directory){
		List<RamlFile> dir = new ArrayList<RamlFile>();
		File[] listOfFiles = directory.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	dir.add(new RamlFile(listOfFiles[i]));
		      } else if (listOfFiles[i].isDirectory()) {
		    	  RamlFile subDir = new RamlFile(listOfFiles[i]);
		    	  subDir.setChildren(iterateOverChildren(listOfFiles[i]));
		    	  dir.add(subDir);
		      }
		}
		return dir;
	}
	
	private void createNewFile(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		System.out.println("POST - File");
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		
		Gson gson = new GsonBuilder().create();
		
		final RamlFile ramlFileFromRequest = gson.fromJson(br, RamlFile.class);
		
		File file = new File(WebHookApiServer.RAML_FILE_LOCATION + ramlFileFromRequest.path);
		if (!file.isFile()){
			try {
				FileUtils.writeStringToFile(file, URLDecoder.decode(ramlFileFromRequest.contents, "UTF-8"));
				final RamlFile responseRamlFile = new RamlFile(file);
				try {
					responseRamlFile.loadContents();
				} catch (IOException io){
					response.sendError(500);
					return;
				}
				
				Gson gsonOut = new GsonBuilder()
			    .excludeFieldsWithoutExposeAnnotation()
			    .create();
				response.setContentType("application/json");
				response.getWriter().print(gsonOut.toJson(responseRamlFile));
				
			} catch (IOException ioException){
				response.sendError(Response.SC_INTERNAL_SERVER_ERROR); 
				return;
			}
		} else {
			response.sendError(Response.SC_METHOD_NOT_ALLOWED);
			return;
		}
	}
	
	private void updateExistingFile(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		System.out.println("PUT - File");
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		
		Gson gson = new GsonBuilder()
	    .create();
		
		final RamlFile ramlFileFromRequest = gson.fromJson(br, RamlFile.class);
		
		File file = new File(WebHookApiServer.RAML_FILE_LOCATION + ramlFileFromRequest.path);
		if (file.isFile()){
			try {
				FileUtils.writeStringToFile(file, URLDecoder.decode(ramlFileFromRequest.contents, "UTF-8"));
			} catch (IOException ioException){
				response.sendError(Response.SC_INTERNAL_SERVER_ERROR); 
			}
		} else {
			response.sendError(Response.SC_NOT_FOUND);
		}
	}
	
	private void deleteExistingFile(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (request.getPathInfo() != null){
			System.out.println("DELETE - File :: " + request.getPathInfo());
			File file = new File(WebHookApiServer.RAML_FILE_LOCATION + request.getPathInfo());
			if (file.isFile() || file.isDirectory()){
				try { 
					file.delete();
					response.sendError(Response.SC_NO_CONTENT);
				} catch (Exception ex){
					response.sendError(Response.SC_INTERNAL_SERVER_ERROR);
					return;
				}
			} else {
				response.sendError(Response.SC_METHOD_NOT_ALLOWED);
			}
		} else {
			response.sendError(Response.SC_NOT_FOUND);
		}
	}

}
