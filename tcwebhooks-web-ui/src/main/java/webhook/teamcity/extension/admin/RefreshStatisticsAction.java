package webhook.teamcity.extension.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.joda.time.LocalDateTime;

import jetbrains.buildServer.web.openapi.ControllerAction;
import webhook.teamcity.exception.StatisticsFileOperationException;
import webhook.teamcity.statistics.StatisticsManager;

/**
 * Updates the statistics and writes the updates to the filesystem.
 * May trigger an analytics submission if it's the first run after midnight.
 *
 */
public class RefreshStatisticsAction extends AbstractWebHookAdminAction implements ControllerAction {
	
	private StatisticsManager myStatisticsManager;

	public RefreshStatisticsAction(
			WebHookStatisticsConfigurationActionController webHookStatisticsConfigurationActionController,
			StatisticsManager statisticsManager) {
		webHookStatisticsConfigurationActionController.registerAction(this);
		myStatisticsManager = statisticsManager;
	}

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, Element ajaxResponse) {
		if (!isAdmin(request)) {
			ajaxResponse.setAttribute("error", "Server Administrator permission required to refresh statistics.");
		} else {
			try {
				this.myStatisticsManager.updateStatistics(LocalDateTime.now());
				ajaxResponse.setAttribute("status", "OK");
			} catch (StatisticsFileOperationException e) {
				ajaxResponse.setAttribute("error", e.getMessage());
			}
		}
	}

	@Override
	public String getAction() {
		return AbstractWebHookAdminAction.REFRESH_STATISTICS;
	}


}
