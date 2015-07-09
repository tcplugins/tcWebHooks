package webhook.teamcity.docs.rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

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
		List<RamlFile> ramlFiles = new ArrayList<RamlFile>();
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	ramlFiles.add(new RamlFile(listOfFiles[i]));  
		        //System.out.println("File " + listOfFiles[i].getName());
		      } else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }
		    }
		Gson gson = new Gson();
		response.setContentType("application/json");
		response.getWriter().print(gson.toJson(ramlFiles));  
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
