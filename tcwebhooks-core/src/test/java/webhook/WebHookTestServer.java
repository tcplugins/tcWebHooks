package webhook;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebHookTestServer implements ResponseEvent {

		Server server;
		public int lastResponseCode = -1;
		public String lastRequestBody = null;
		
		
		public Server getServer() {
			return server;
		}

		public WebHookTestServer (String host, Integer port) {
		
			//1. Creating the server
			server = new Server(port);
			
			//2. Creating the WebAppContext for the created content
			WebAppContext ctx = new WebAppContext();
			ctx.setResourceBase("src/test/webapp");
			ctx.setContextPath("/");
			
			
			ctx.addServlet(new ServletHolder(new TestingServlet(HttpServletResponse.SC_OK, this)), "/200");
			ctx.addServlet(new ServletHolder(new TestingServlet(HttpServletResponse.SC_MOVED_TEMPORARILY, this)), "/302");
			ctx.addServlet(new ServletHolder(new TestingServlet(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, this)), "/500");
			
			ctx.addServlet(new ServletHolder(new TestingServlet(HttpServletResponse.SC_OK, this)), "/auth/200");
			ctx.addServlet(new ServletHolder(new TestingServlet(HttpServletResponse.SC_MOVED_TEMPORARILY, this)), "/auth/302");
			ctx.addServlet(new ServletHolder(new TestingServlet(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, this)), "/auth/500");
			
			ctx.addFilter(new FilterHolder(new ResponseCodeFilter(this)), "/*" , EnumSet.of(DispatcherType.REQUEST));
	
			//3. Creating the LoginService for the realm
			HashLoginService loginService = new HashLoginService("TestRealm");
			
			//4. Setting the realm configuration there the users, passwords and roles reside
			loginService.setConfig("src/test/resources/testrealm.txt");
	
			//5. Appending the loginService to the Server
			server.addBean(loginService);
			
			//6. Setting the handler
			server.setHandler(ctx);

		}
		
		@Override
		public int getReponseCode(){
			synchronized (server) {
				return this.lastResponseCode;
			}
		}

		@Override
		public void updateResponseCode(int responseCode) {
			synchronized (server) {
				this.lastResponseCode = responseCode;
			}
		}

		@Override
		public String getRequestBody() {
			return this.lastRequestBody;
		}

		@Override
		public void updateRequestBody(String requestBody) {
			this.lastRequestBody = requestBody;
		}
		

}