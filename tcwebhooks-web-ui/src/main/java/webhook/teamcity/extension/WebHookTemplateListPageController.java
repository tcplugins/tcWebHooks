package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookSettingsManager;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookTemplateListPageController extends WebHookTemplateBasePageController {

		private final WebHookPayloadManager myPayloadManager;

		@SuppressWarnings("squid:S00107")
		public WebHookTemplateListPageController(SBuildServer server, WebControllerManager webManager,
				PluginDescriptor pluginDescriptor, WebHookPayloadManager payloadManager,
				WebHookPluginDataResolver webHookPluginDataResolver, WebHookTemplateManager webHookTemplateManager,
				WebHookSettingsManager webHookSettingsManager, ProjectIdResolver projectIdResolver) {
			super(server, webManager, pluginDescriptor, webHookPluginDataResolver, webHookTemplateManager, webHookSettingsManager, projectIdResolver);
			this.myPayloadManager = payloadManager;
		}

		@Override
		protected String getUrl() {
			return "/webhooks/templates.html";
		}

		@Nullable
		protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {

			HashMap<String,Object> params = new HashMap<>();
			addBaseParams(params);


			List<WebHookPayloadTemplate> templates;

			if(request.getParameter("projectId") != null){
				SProject project = TeamCityIdResolver.findProjectById(this.myServer.getProjectManager(), request.getParameter("projectId"));
				if (project != null) {
					templates = myTemplateManager.getRegisteredPermissionedTemplatesForProject(project);
					params.put("project", project);
				} else {
					templates = new ArrayList<>();
					params.put("error", "Project ID was not found");
				}
			} else {
				templates = myTemplateManager.getRegisteredPermissionedTemplates();
			}

			params.put("payloadFormats", myPayloadManager.getTemplatedFormats());
			params.put("webHookTemplates", RegisteredWebHookTemplateBean.build(myTemplateManager, templates,
					myPayloadManager.getRegisteredFormats(), myWebHookSettingsManager, myServer.getProjectManager()).getTemplateList());
			return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/templateList.jsp", params);
		}


}
