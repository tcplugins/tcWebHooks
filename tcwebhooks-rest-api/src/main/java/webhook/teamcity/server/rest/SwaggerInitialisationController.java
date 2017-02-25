package webhook.teamcity.server.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import io.swagger.jaxrs.config.BeanConfig;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;

public class SwaggerInitialisationController extends BaseController {
	
	public SwaggerInitialisationController(final SBuildServer server) {
		super(server);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.2");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8002");
        beanConfig.setBasePath("/api/rest/webhooks");
        beanConfig.setResourcePackage("io.swagger.resources");
        beanConfig.setScan(true);
        Loggers.SERVER.info("tcWebHooksAPI:: Starting SwaggerInitialisationController");
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}