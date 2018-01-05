/*******************************************************************************
 * Copyright 2017 Net Wolf UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package webhook.teamcity.server.rest.web;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import lombok.Data;
import webhook.teamcity.server.WebHookTeamCityRestApiZipPluginFixer;
import webhook.teamcity.server.pluginfixer.JarReport;

public class WebHookRestApiAdminPage extends AdminPage {
	public static final String TC_WEB_HOOK_REST_API_ADMIN_ID = "tcWebHooksRestApi";
	private final WebHookTeamCityRestApiZipPluginFixer myPluginFixer;

	public WebHookRestApiAdminPage(@NotNull PagePlaces pagePlaces, 
								  @NotNull PluginDescriptor descriptor,
								  @NotNull WebHookTeamCityRestApiZipPluginFixer webHookTeamCityRestApiZipPluginFixer
								  ) {
		super(pagePlaces);
		myPluginFixer = webHookTeamCityRestApiZipPluginFixer;
		setPluginName(TC_WEB_HOOK_REST_API_ADMIN_ID);
		setIncludeUrl(descriptor.getPluginResourcesPath("WebHookRestApi/adminTab.jsp"));
        addCssFile(descriptor.getPluginResourcesPath("WebHookRestApi/css/tcWebHooksApi.css"));
        addJsFile(descriptor.getPluginResourcesPath("WebHookRestApi/restApiHealthStatus.js"));
        addJsFile("/js/bs/serverRestart.js");
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
		Set<Path> allPaths = new TreeSet<>();
		
		for (Entry<Path, JarReport> e : myPluginFixer.getJarReports().entrySet()) {
			updatePluginBean(fileResults, e.getKey(), e.getValue());
		}
//		allPaths.addAll(myPluginFixer.getFoundApiZipFilesContainingJaxbJars());
//		allPaths.addAll(myPluginFixer.getFoundUnpackedApiZipFilesContainingJaxbJars());
//		allPaths.addAll(myPluginFixer.getFoundApiZipFilesNotContainingJaxbJars());
//		allPaths.addAll(myPluginFixer.getFoundUnpackedApiZipFilesNotContainingJaxbJars());
//		for (Path p : allPaths) {
//			updatePluginBean(fileResults, p, 
//					! myPluginFixer.getFoundApiZipFilesNotContainingJaxbJars().contains(p),
//					! myPluginFixer.getFoundUnpackedApiZipFilesNotContainingJaxbJars().contains(p));
//		}
//		for (Path p : allPaths) {
//			updatePluginBean(fileResults, p, 
//					myPluginFixer.getFoundApiZipFilesContainingJaxbJars().contains(p),
//					myPluginFixer.getFoundUnpackedApiZipFilesContainingJaxbJars().contains(p));
//		}
		
		model.put("hasFoundIssues", myPluginFixer.foundApiZipFilesContainingJaxbJars());
		model.put("fileResults", fileResults);
		model.put("restartRequired", myPluginFixer.isHaveFilesBeenCleanedSinceBoot());
//		model.put("apiZipFilesContainingJars", myPluginFixer.getFoundApiZipFilesContainingJaxbJars());
//		model.put("unpackedApiFilesContainingJars", myPluginFixer.getFoundUnpackedApiZipFilesContainingJaxbJars());
//		model.put("apiZipFilesNotContainingJars", myPluginFixer.getFoundApiZipFilesNotContainingJaxbJars());
//		model.put("unpackedApiFilesNotContainingJars", myPluginFixer.getFoundUnpackedApiZipFilesNotContainingJaxbJars());
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

}