package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.extension.bean.template.TemplateRenderingBean;
import webhook.teamcity.extension.bean.template.TemplateRenderingBeanJsonSerialiser;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.settings.WebHookProjectSettings;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class WebHookTemplateRenderingController extends BaseController {
	
	
	private final WebControllerManager myWebManager;
    private SBuildServer myServer;
    private ProjectSettingsManager mySettings;
    private final String myPluginPath;
    private final WebHookPayloadManager myPayloadManager;
	private final WebHookTemplateResolver myTemplateResolver;
    
    public WebHookTemplateRenderingController(SBuildServer server, WebControllerManager webManager, 
    		ProjectSettingsManager settings, WebHookProjectSettings whSettings, WebHookPayloadManager payloadManager,
    		WebHookTemplateResolver templateResolver, PluginDescriptor pluginDescriptor) {
        super(server);
        myWebManager = webManager;
        myServer = server;
        mySettings = settings;
        myPluginPath = pluginDescriptor.getPluginResourcesPath();
        myPayloadManager = payloadManager;
        myTemplateResolver = templateResolver;
    }
    
    public void register(){
	      myWebManager.registerController("/webhooks/renderTemplate.html", this);
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		if (	request.getParameter("buildState") != null
			&&	request.getParameter("payloadTemplate") != null
			&&	request.getParameter("payloadFormat") != null
			&&	request.getParameter("projectId") != null){
			
			SProject myproject = this.myServer.getProjectManager().findProjectByExternalId(request.getParameter("projectId"));
			String buildState = request.getParameter("buildState");
			String payloadFormat = request.getParameter("payloadFormat");
			String payloadTemplate = request.getParameter("payloadTemplate");
			
			WebHookTemplateContent content =  myTemplateResolver.findWebHookBranchOrNonBranchTemplate(buildState, myproject, payloadFormat, payloadTemplate);
			
			WebHookStringRenderer renderer = myPayloadManager.getFormat(payloadFormat).getWebHookStringRenderer();
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("templateRendering", TemplateRenderingBeanJsonSerialiser.serialise(
											TemplateRenderingBean.build( 
															myproject.getExternalId(), 
															buildState.replace("Branch", ""), 
															payloadFormat, 
															renderer.render(content.getTemplateText()), 
															renderer.render(content.getTemplateText())  // TODO: need to resolve actual build info here
														)
											)
						);
			return new ModelAndView(myPluginPath + "WebHook/templateRendering.jsp", params);
			
			
		}
		return null;
	}

}
