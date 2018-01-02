/*******************************************************************************
 *
 *  Copyright 2017 Net Wolf UK
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  
 *******************************************************************************/
package webhook.teamcity.server.rest.web.action;

import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import jetbrains.buildServer.controllers.ActionMessages;
import jetbrains.buildServer.web.openapi.ControllerAction;
import webhook.teamcity.server.WebHookTeamCityRestApiZipPluginFixer;
import webhook.teamcity.server.pluginfixer.JarReport;
import webhook.teamcity.server.rest.web.WebHookRestApiActionController;

public class FixPluginZipFileAction extends WebHooksApiAction implements ControllerAction {

	private final WebHookTeamCityRestApiZipPluginFixer myPluginFixer;
	private final static String CLEAN_API_ZIPFILE_OR_UNPACKED_DIR = "apiZipFix";
	private final static String API_ZIP_FILE = "apiZipFile";

	public FixPluginZipFileAction(@NotNull final WebHookTeamCityRestApiZipPluginFixer pluginFixer,
								   @NotNull final WebHookRestApiActionController controller) {

		myPluginFixer = pluginFixer;
		controller.registerAction(this);
	}
	
	@Override
	public String getApiAction() {
		return CLEAN_API_ZIPFILE_OR_UNPACKED_DIR;
	}

	public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response,
			@Nullable final Element ajaxResponse) {
		String path;
		try {
			path = getParameterAsStringOrNull(request, API_ZIP_FILE, "Please supply an API ZIP file path.");
		} catch (MissingPathException e) {
			ajaxResponse.setAttribute("error", e.getMessage());
			ActionMessages.getOrCreateMessages(request).addMessage("apiFixResult", e.getMessage());
			return;
		}

		boolean hasDoneCleanup = false;
		boolean errored = false;
		
		for (Path p : myPluginFixer.getFoundApiZipFiles()) {
			if (p.toString().equals(path)) {
				JarReport report = myPluginFixer.fixRestApiZipPlugin(p);
				if (report.isErrored()) {
					StringBuilder sb = new StringBuilder();
					for (String message : report.getFailureMessageList()) {
						sb.append(message).append("\n");
					}
					ajaxResponse.setAttribute("error", sb.toString());
					errored = true;
				}
				hasDoneCleanup = true;
			}
		}

		myPluginFixer.findRestApiZipPlugins();
		
		if (errored) {
			ActionMessages.getOrCreateMessages(request).addMessage("apiFixResult", ajaxResponse.getAttribute("error").getValue());
			return;
		}
		
		if (! hasDoneCleanup) {
			String errorMsg = "The file you asked to clean does not appear to be in error. No cleaning was attemtped";
			ajaxResponse.setAttribute("error", errorMsg);
			ActionMessages.getOrCreateMessages(request).addMessage("apiFixResult", errorMsg);
			return;
		}
		
		
		
		
		String errorMsg = "The file you asked to clean does not appear to have been successfully cleaned. Please see the GitHub issue linked on this page for more information.";
		for (Path p : myPluginFixer.getFoundApiZipFilesContainingJaxbJars()) {
			if (p.toString().equals(path)) {
				ajaxResponse.setAttribute("error", errorMsg);
				ActionMessages.getOrCreateMessages(request).addMessage("apiFixResult", errorMsg);
				return;
			}
		}
		
		ActionMessages.getOrCreateMessages(request).addMessage("apiFixResult", "API ZIP and/or unpacked jars cleaned. You MUST now restart TeamCity");
		ajaxResponse.setAttribute("status", "OK");
		
	}

}