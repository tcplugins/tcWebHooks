package webhook.teamcity.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static webhook.teamcity.payload.util.StringUtils.stripTrailingSlash;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusReport;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope;
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory;
import jetbrains.buildServer.serverSide.healthStatus.ItemSeverity;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import webhook.teamcity.server.rest.web.WebHookRestApiAdminPage;

public class RestApiJarHealthReport extends HealthStatusReport {
	
	private static final String CATEGORY_ID = "webhookRestApiConflictingJars"; 
	private static final String CATEGORY_NAME = "WebHook REST API Jar Conflict";
	
	@NotNull 
	private final SBuildServer mySBuildServer;
	
	@NotNull 
	private final ItemCategory myCategory; 
	 
	@NotNull 
	private final WebHookTeamCityRestApiZipPluginFixer myWebHookTeamCityRestApiZipPluginFixer; 
	
	public RestApiJarHealthReport(
								  @NotNull final SBuildServer buildServer,
								  @NotNull final PagePlaces pagePlaces,
								  @NotNull final PluginDescriptor pluginDescriptor,
								  @NotNull final WebHookTeamCityRestApiZipPluginFixer webHookTeamCityRestApiZipPluginFixer) {
		mySBuildServer = buildServer;
		myWebHookTeamCityRestApiZipPluginFixer = webHookTeamCityRestApiZipPluginFixer; 
	    myCategory = new ItemCategory(CATEGORY_ID, CATEGORY_NAME, ItemSeverity.WARN); 
	    final HealthStatusItemPageExtension myPEx = new HealthStatusItemPageExtension(CATEGORY_ID, pagePlaces) { 
			@Override 
			public boolean isAvailable(@NotNull final HttpServletRequest request) { 
				String pageUrl = (String)request.getAttribute("pageUrl");
				return myWebHookTeamCityRestApiZipPluginFixer.foundApiZipFilesContainingJaxbJars()
					  && ! myWebHookTeamCityRestApiZipPluginFixer.getFoundApiZipFiles().isEmpty()
					  && isAdminPageOrTemplateEditPage(pageUrl)
					  && super.isAvailable(request); 
			}

			private boolean isAdminPageOrTemplateEditPage(String pageUrl) {
				return pageUrl.contains("/webhooks/templates.html") || pageUrl.contains("/webhooks/template.html") || pageUrl.contains("/admin/");
				
			} 
	    }; 
	    myPEx.setIncludeUrl(pluginDescriptor.getPluginResourcesPath("WebHookRestApi/restApiHealthStatus.jsp")); 
	    myPEx.setVisibleOutsideAdminArea(true); 
	    myPEx.register(); 
	}

	@Override
	public String getType() {
		return CATEGORY_ID;
	}

	@Override
	public String getDisplayName() {
		return "WebHook REST API Jar Conflict Report";
	}

	@Override
	public Collection<ItemCategory> getCategories() {
		return Collections.singleton(myCategory);
	}

	@Override
	public boolean canReportItemsFor(HealthStatusScope scope) {
		return scope.isItemWithSeverityAccepted(myCategory.getSeverity());
	}

	@Override
	public void report(HealthStatusScope scope, HealthStatusItemConsumer resultConsumer) {
		Map<String,Object> params = new HashMap<>();
		params.put("possibleProblemFilesCount", myWebHookTeamCityRestApiZipPluginFixer.getFoundApiZipFilesContainingJaxbJars().size());
		params.put("adminUrl", stripTrailingSlash(mySBuildServer.getRootUrl()) + "/admin/admin.html?item=" + WebHookRestApiAdminPage.TC_WEB_HOOK_REST_API_ADMIN_ID);
	    final HealthStatusItem item = new HealthStatusItem("webhook-api-jar-conflict", myCategory, params);
	    resultConsumer.consumeGlobal(item);
	}

}
