package webhook.teamcity.docs.rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RamlFilesServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	File baseRamlPath; 
	public RamlFilesServlet(File ramlBaseDir) {
		this.baseRamlPath = ramlBaseDir;
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("GET - Files");
		File[] listOfFiles = this.baseRamlPath.listFiles();
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
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("POST - Files");
		response.setContentType("application/json");
	}
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("PUT - Files");
		response.setContentType("application/json");
	}
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("DELETE - Files");
		response.setContentType("application/json");
	}

}
