package webhook.teamcity.server.rest.web;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import lombok.Data;
import webhook.teamcity.server.WebHookTeamCityRestApiZipPluginFixer;
import webhook.teamcity.server.pluginfixer.JarReport;

public class WebHookRestApiAdminPage extends AdminPage {
	private static final Logger LOG = Logger.getInstance(WebHookRestApiAdminPage.class.getName());
	public static final String TC_WEB_HOOK_REST_API_ADMIN_ID = "tcWebHooksRestApi";
	private final WebHookTeamCityRestApiZipPluginFixer myPluginFixer;

	public WebHookRestApiAdminPage(
						@NotNull SBuildServer server,
						@NotNull PagePlaces pagePlaces, 
						@NotNull PluginDescriptor descriptor,
						@NotNull WebHookTeamCityRestApiZipPluginFixer webHookTeamCityRestApiZipPluginFixer
					) {
		super(pagePlaces);
		myPluginFixer = webHookTeamCityRestApiZipPluginFixer;
		setPluginName(TC_WEB_HOOK_REST_API_ADMIN_ID);
		setIncludeUrl(descriptor.getPluginResourcesPath("WebHookRestApi/adminTab.jsp"));
        addCssFile(descriptor.getPluginResourcesPath("WebHookRestApi/css/tcWebHooksApi.css"));
        addJsFile(descriptor.getPluginResourcesPath("WebHookRestApi/restApiHealthStatus.js"));
        if (isRestartable(server)) {
        	addJsFile("/js/bs/serverRestart.js");
        } else {
        	addJsFile(descriptor.getPluginResourcesPath("WebHookRestApi/serverNoRestart.js"));
        }
		setTabTitle("WebHooks REST API");
		setPosition(PositionConstraint.after("clouds", "email", "jabber", "plugins", "tcWebHooks"));
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
		Map<String, PluginResultBean> fileResults = new TreeMap<>();
		
		for (Entry<Path, JarReport> e : myPluginFixer.getJarReports().entrySet()) {
			updatePluginBean(fileResults, e.getKey(), e.getValue());
		}
		
		model.put("hasFoundIssues", myPluginFixer.foundApiZipFilesContainingJaxbJars());
		model.put("fileResults", fileResults);
		model.put("restartRequired", myPluginFixer.isHaveFilesBeenCleanedSinceBoot());
	}
	
	private void updatePluginBean(Map<String, PluginResultBean> resultsMap, Path path, JarReport jarReport) {
		PluginResultBean bean = new PluginResultBean();
		bean.setPath(path);
		bean.setJarReport(jarReport);
		resultsMap.put(path.toString(), bean);
	}
	
	@Data
	public static class PluginResultBean {
		Path path;
		JarReport jarReport;
		public int getFileListSize() {
			return jarReport.getJarsInZipFile().size() + jarReport.getJarsInUnpackedLocation().size();
		}
	}

	private boolean isRestartable(SBuildServer server) {
		LOG.debug("WebHookRestApiAdminPage :: Server Major Version is: " + server.getServerMajorVersion());
		LOG.debug("WebHookRestApiAdminPage :: Server Minor Version is: " + server.getServerMinorVersion());
		LOG.debug("WebHookRestApiAdminPage :: Server Version is: " + server.getFullServerVersion());
		boolean isRestartable = (server.getServerMajorVersion() == 17 && server.getServerMinorVersion() >= 2) || server.getServerMajorVersion() > 17;
		LOG.info("Server is restartable: " + isRestartable);
		return isRestartable;
	}

}