package webhook;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestingServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Integer response; 
	Logger logger = LoggerFactory.getLogger(TestingServlet.class);

	public TestingServlet(Integer response) {
		this.response = response;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/plain");
		response.setStatus(this.response);
		switch (this.response) {
			case  HttpServletResponse.SC_OK:
				//response.getWriter().println("<h1>Hello SimpleServlet</h1>");
				this.printParams(request, response);
				break;
			case HttpServletResponse.SC_MOVED_TEMPORARILY:
				response.sendRedirect("/200");
				break;
			default:
				response.getWriter().println("<h1>Hello from defaultt</h1>");
				break;
		}
		System.out.println("Handling Web request for " + ((Request) request).getUri().toString());
	}

	private void printParams(HttpServletRequest request, HttpServletResponse response){
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("remoteUser:" + request.getRemoteUser());
		
		Enumeration<String> paramNames = request.getParameterNames();
	    while(paramNames.hasMoreElements()) {
	      String paramName = paramNames.nextElement();
	      out.print(paramName + " :: ");
	      String[] paramValues = request.getParameterValues(paramName);
	      if (paramValues.length == 1) {
	        String paramValue = paramValues[0];
	        if (paramValue.length() == 0)
	          out.println("No Value");
	        else
	          out.println(paramValue);
	      } else {
	    	  out.println();
	        for(int i=0; i<paramValues.length; i++) {
	          out.println(" ->  " + paramValues[i]);
	        }
	      }
	    }

	}
}