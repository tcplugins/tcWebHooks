package webhook.teamcity.extension.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;

import jetbrains.buildServer.controllers.ActionMessages;
import jetbrains.buildServer.serverSide.impl.MainConfigManager;
import jetbrains.buildServer.web.openapi.ControllerAction;
import webhook.teamcity.settings.WebHookMainSettings;

public class UpdateStatisticsOptionAction extends AbstractWebHookAdminAction implements ControllerAction {
	
	private WebHookMainSettings myWebHookMainSettings;
	private MainConfigManager myMainConfigManager;

	public UpdateStatisticsOptionAction(
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
				boolean newValue = this.getParameterIsEnabled(request, AbstractWebHookAdminAction.UPDATE_STATISTICS, "Expected a 'enabled' or 'disabled' value for " + AbstractWebHookAdminAction.UPDATE_STATISTICS);
				this.myWebHookMainSettings.getWebHookMainConfig().setAssembleStatistics(newValue);
				this.myMainConfigManager.persistConfiguration();
				ajaxResponse.setAttribute("status", "OK");
				ActionMessages.getOrCreateMessages(request).addMessage("updateStatisticsResult", newValue ? "Statistics collection enabled" : "Statistics collection disabled");
			} catch (UnexpectedActionException | UnexpectedActionValueException e) {
				ajaxResponse.setAttribute("error", e.getMessage());
			}
		}
	}

	@Override
	public String getAction() {
		return AbstractWebHookAdminAction.UPDATE_STATISTICS;
	}


}
