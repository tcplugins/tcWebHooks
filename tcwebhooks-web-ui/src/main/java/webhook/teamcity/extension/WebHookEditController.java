package webhook.teamcity.extension;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.extension.bean.ErrorResult;
import webhook.teamcity.extension.util.WebHookConfigurationValidator;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.WebHookUpdateResult;
import webhook.teamcity.json.WebHookConfigurationGsonBuilder;
import webhook.teamcity.json.WebHookConfigurationJson;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookEditController extends BaseController {

	private static final String EDIT_WEB_HOOK_JSP = "WebHook/editWebHook.jsp";
	private static final String CONTENT = "content";
	private static final String PARAM_ACTION = "action";
	private final String myPluginPath;
	private final WebHookConfigurationValidator myWebHookConfigurationValidator;
	private final WebHookSettingsManager myWebHookSettingsManager;
	private final ProjectIdResolver myProjectIdResolver;
	private final BuildTypeIdResolver myBuildTypeIdResolver;

	public WebHookEditController(SBuildServer server,
			WebHookConfigurationValidator webHookConfigurationValidator,
			PluginDescriptor pluginDescriptor,
			WebControllerManager webManager,
			WebHookSettingsManager webHookSettingsManager,
			ProjectIdResolver projectIdResolver,
			BuildTypeIdResolver buildTypeIdResolver) {
		super(server);
		myWebHookConfigurationValidator = webHookConfigurationValidator;
		myWebHookSettingsManager = webHookSettingsManager;
		myProjectIdResolver = projectIdResolver;
		myBuildTypeIdResolver = buildTypeIdResolver;
		myPluginPath = pluginDescriptor.getPluginResourcesPath();
		webManager.registerController("/webhooks/edit.html", this);
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {

			Gson gson = WebHookConfigurationGsonBuilder.gsonBuilder();
	
			WebHookConfigurationJson webHookSaveRequest = gson.fromJson(
					request.getReader(),
					WebHookConfigurationJson.class
				);
			HashMap<String,Object> params = new HashMap<>();
			
			if (isGet(request) && request.getParameter("webHookId") != null && request.getParameter("projectId") != null) {
				WebHookConfig webHookConfig = this.myWebHookSettingsManager.getSettings(this.myProjectIdResolver.getInternalProjectId(request.getParameter("projectId")))
					.getWebHooksConfigs().stream()
						.filter(whc -> request.getParameter("webHookId").equals(whc.getUniqueKey()))
						.findFirst().orElse(null);
				if (webHookConfig != null) {
					params.put(CONTENT, gson.toJson(WebHookConfigurationJson.fromWebHookConfig(webHookConfig, myProjectIdResolver, myBuildTypeIdResolver)));
					return new ModelAndView(myPluginPath + EDIT_WEB_HOOK_JSP, params);
				}
			} else if (isPost(request)) {



			ErrorResult result = new ErrorResult();

			// Check for errors and that the user has EDIT_PROJECT permission.
			
			if (request.getParameter(PARAM_ACTION) != null && request.getParameter(PARAM_ACTION).equals("add")) {
				myWebHookConfigurationValidator.validateNewWebHook(webHookSaveRequest.getProjectId(), webHookSaveRequest, result);
			} else {
				myWebHookConfigurationValidator.validateUpdatedWebHook(webHookSaveRequest.getProjectId(), webHookSaveRequest, result);
			}

			if (result.isErrored()) {
				params.put(CONTENT, gson.toJson(result));
				return new ModelAndView(myPluginPath + EDIT_WEB_HOOK_JSP, params);
			}

			
			WebHookConfig webHookConfig = webHookSaveRequest.toWebHookConfig(myProjectIdResolver, myBuildTypeIdResolver);
			WebHookUpdateResult saveResult = null;
			
			if (request.getParameter(PARAM_ACTION) != null && request.getParameter(PARAM_ACTION).equals("delete")) {
				saveResult = this.myWebHookSettingsManager.deleteWebHook(this.myProjectIdResolver.getInternalProjectId(webHookSaveRequest.getProjectId()), webHookConfig);
			} else if (request.getParameter(PARAM_ACTION) != null && request.getParameter(PARAM_ACTION).equals("add")) {
				saveResult = this.myWebHookSettingsManager.addNewWebHook(this.myProjectIdResolver.getInternalProjectId(webHookSaveRequest.getProjectId()), webHookConfig);
			} else {
				saveResult = this.myWebHookSettingsManager.updateWebHook(this.myProjectIdResolver.getInternalProjectId(webHookSaveRequest.getProjectId()), webHookConfig);
			}

			if (saveResult.isUpdated()) {
				params.put(CONTENT, gson.toJson(WebHookConfigurationJson.fromWebHookConfig(saveResult.getWebHookConfig(), myProjectIdResolver, myBuildTypeIdResolver)));
			}	else {
				params.put(CONTENT, saveResult);
			}
			return new ModelAndView(myPluginPath + EDIT_WEB_HOOK_JSP, params);
		}

		return null;
	}

}
