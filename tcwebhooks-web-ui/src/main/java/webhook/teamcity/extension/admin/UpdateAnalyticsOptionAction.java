package webhook.teamcity.extension.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;

import jetbrains.buildServer.controllers.ActionMessages;
import jetbrains.buildServer.serverSide.impl.MainConfigManager;
import jetbrains.buildServer.web.openapi.ControllerAction;
import webhook.teamcity.settings.WebHookMainSettings;

public class UpdateAnalyticsOptionAction extends AbstractWebHookAdminAction implements ControllerAction {
	
	private WebHookMainSettings myWebHookMainSettings;
	private MainConfigManager myMainConfigManager;

	public UpdateAnalyticsOptionAction(
			WebHookStatisticsConfigurationActionController webHookStatisticsConfigurationActionController,
			WebHookMainSettings webHookMainSettings,
			MainConfigManager mainConfigManager) {
		webHookStatisticsConfigurationActionController.registerAction(this);
		myWebHookMainSettings = webHookMainSettings;
		myMainConfigManager = mainConfigManager;
	}

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, Element ajaxResponse) {
		if (!isAdmin(request)) {
			ajaxResponse.setAttribute("error", "Server Administrator permission required to change settings.");
		} else {
			try {
				boolean newValue = this.getParameterIsEnabled(request, AbstractWebHookAdminAction.UPDATE_ANALYTICS, "Expected a 'enabled' or 'disabled' value for " + AbstractWebHookAdminAction.UPDATE_ANALYTICS);
				this.myWebHookMainSettings.getWebHookMainConfig().setReportStatistics(newValue);
				this.myMainConfigManager.persistConfiguration();
				ajaxResponse.setAttribute("status", "OK");
				ActionMessages.getOrCreateMessages(request).addMessage("updateStatisticsResult", newValue ? "Analytics sharing enabled" : "Analytics sharing disabled");
			} catch (UnexpectedActionException | UnexpectedActionValueException e) {
				ajaxResponse.setAttribute("error", e.getMessage());
			}
		}
	}

	@Override
	public String getAction() {
		return AbstractWebHookAdminAction.UPDATE_ANALYTICS;
	}


}
