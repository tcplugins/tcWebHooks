/*******************************************************************************
 *
 *  Copyright 2016 Net Wolf UK
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
package webhook.teamcity.extension.admin;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.BaseAjaxActionController;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

/**
 * This class simply holds the actions available at  "/admin/manageWebHookStatistics.html"
 * Actions need to inject this class and register themselves.
 */
public class WebHookStatisticsConfigurationActionController extends BaseAjaxActionController {
	
	public static final String ACTION_TYPE = "action";
    public static final String DEBREPO_UUID = "debrepo.uuid";
    public static final String DEBREPO_NAME = "debrepo.name";
    public static final String DEBREPO_RESTRICTED = "debrepo.restricted";
    public static final String DEBREPO_PROJECT_ID = "debrepo.project.id";
    public static final String DEBREPO_FILTER_ID = "debrepo.filter.id";
    public static final String DEBREPO_FILTER_REGEX = "debrepo.filter.regex";
    public static final String DEBREPO_FILTER_DIST = "debrepo.filter.dist";
    public static final String DEBREPO_FILTER_COMPONENT = "debrepo.filter.component";
    public static final String DEBREPO_FILTER_BUILD_TYPE_ID = "debrepo.filter.buildtypeid";
    
  public WebHookStatisticsConfigurationActionController(@NotNull final PluginDescriptor pluginDescriptor,
                                        	   @NotNull final WebControllerManager controllerManager) {
    super(controllerManager);
    controllerManager.registerController("/admin/manageWebHookStatistics.html", this);
  }
    
}