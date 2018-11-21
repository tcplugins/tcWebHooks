package webhook.teamcity.server.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.Loggers;

@RestController
public class TestRestApi extends BaseController {
	
    private static final String template = "Hello, %s!";
    
    public TestRestApi(SBuildServer sBuildServer, WebControllerManager webControllerManager, 
			 @NotNull PluginDescriptor descriptor,
			 AuthorizationInterceptor authorizationInterceptor 
			 ) 
    {
    	super(sBuildServer);
		Loggers.SERVER.warn("#################################");
		Loggers.SERVER.warn("#################################");
		Loggers.SERVER.warn("#");
		Loggers.SERVER.warn("#");
		Loggers.SERVER.warn("# TestRestApi starting");
		Loggers.SERVER.warn("#");
		Loggers.SERVER.warn("#");
		Loggers.SERVER.warn("#################################");
		Loggers.SERVER.warn("#################################");
		webControllerManager.registerController("/app/test/webhooks/**", this);
	}

    @RequestMapping("/app/test/webhooks/greeting")
    public String greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return String.format(template, name);
    }

	@Override
	protected ModelAndView doHandle(HttpServletRequest arg0, HttpServletResponse arg1) throws Exception {
		// TODO Auto-generated method stub
		return new ModelAndView(greeting("name"));
	}
}
