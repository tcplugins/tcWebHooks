package webhook.teamcity.extension.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import webhook.teamcity.WebHookHistoryRepository;

public class WebHookAdminPage extends AdminPage {
	public static final String TC_WEB_HOOK_REST_API_ADMIN_ID = "tcWebHooks";
	private final WebHookHistoryRepository myWebHookHistoryRepository;

	public WebHookAdminPage(@NotNull PagePlaces pagePlaces, 
								  @NotNull PluginDescriptor descriptor,
								  @NotNull WebHookHistoryRepository webHookHistoryRepository
								  ) {
		super(pagePlaces);
		this.myWebHookHistoryRepository = webHookHistoryRepository;
		setPluginName(TC_WEB_HOOK_REST_API_ADMIN_ID);
		setIncludeUrl(descriptor.getPluginResourcesPath("WebHook/adminTab.jsp"));
        addCssFile(descriptor.getPluginResourcesPath("WebHook/css/styles.css"));
        addJsFile(descriptor.getPluginResourcesPath("WebHook/restApiHealthStatus.js"));
		setTabTitle("WebHooks");
		setPosition(PositionConstraint.after("clouds", "email", "jabber"));
		register();
	}

	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request) {
		return super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
	}

	@NotNull
	public String getGroup() {
		return SERVER_RELATED_GROUP;
	}
	
	@Override
	public void fillModel(Map<String, Object> model, HttpServletRequest request) {
		model.put("history", myWebHookHistoryRepository.findHistoryItemsInError());
	}
}