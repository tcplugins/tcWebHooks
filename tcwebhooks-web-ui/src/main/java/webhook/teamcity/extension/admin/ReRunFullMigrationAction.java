package webhook.teamcity.extension.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;

import jetbrains.buildServer.web.openapi.ControllerAction;
import webhook.teamcity.settings.converter.PluginSettingsToProjectFeaturesMigrator;

/**
 * Re-runs the full migration, but with stop on fail disabled.
 *
 */
public class ReRunFullMigrationAction extends AbstractWebHookAdminAction implements ControllerAction {
	
	private PluginSettingsToProjectFeaturesMigrator myPluginSettingsToProjectFeaturesMigrator;

	public ReRunFullMigrationAction(
			WebHookStatisticsConfigurationActionController webHookStatisticsConfigurationActionController,
			PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator) {
		webHookStatisticsConfigurationActionController.registerAction(this);
		myPluginSettingsToProjectFeaturesMigrator = pluginSettingsToProjectFeaturesMigrator;
	}

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, Element ajaxResponse) {
		if (!isAdmin(request)) {
			ajaxResponse.setAttribute("error", "Server Administrator permission required to refresh statistics.");
		} else {
			boolean isStarted = this.myPluginSettingsToProjectFeaturesMigrator.runMigrationTask();
			if (isStarted) {
				ajaxResponse.setAttribute("status", "OK");
			} else {
				ajaxResponse.setAttribute("error", "Failed to start task. It may already be running");
			}
		}
	}

	@Override
	public String getAction() {
		return AbstractWebHookAdminAction.RERUN_FULL_MIGRATION;
	}


}
