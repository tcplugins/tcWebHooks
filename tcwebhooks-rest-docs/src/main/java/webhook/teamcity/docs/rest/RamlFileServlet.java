package webhook.teamcity.docs.rest;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RamlFileServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	File baseRamlPath; 
	public RamlFileServlet(File file) {
		this.baseRamlPath = file;
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("GET - File");
		response.setContentType("application/json");
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
