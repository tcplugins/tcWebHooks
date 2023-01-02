package webhook;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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
	private ResponseEvent callback;

	public TestingServlet(Integer response, ResponseEvent callback) {
		this.response = response;
		this.callback = callback;
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType(request.getContentType());
		response.setStatus(this.response);
		switch (this.response) {
			case  HttpServletResponse.SC_OK:
				if (request.getContentType() != null && request.getContentType().startsWith("application/x-www-form-urlencoded")) {
					response.setContentType("text/plain");
					this.printParams(request, response);
				} else {
					String requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8.name());
					if (callback != null){
						callback.updateRequestBody(requestBody);
					}
					response.getWriter().println(requestBody);
				}
				break;
			case HttpServletResponse.SC_MOVED_TEMPORARILY:
				response.sendRedirect("/200");
				break;
			default:
				response.getWriter().println("<h1>Hello from default</h1>");
				break;
		}
		System.out.println("Handling Web request for " + ((Request) request).getRequestURL().toString());
	}

	@SuppressWarnings("null")
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