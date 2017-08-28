package webhook.teamcity.server;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusReport;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope;
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory;
import jetbrains.buildServer.serverSide.healthStatus.ItemSeverity;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;

public class RestApiJarHealthReport extends HealthStatusReport {
	
	private static final String CATEGORY_ID = "webhookRestApiConflictingJars"; 
	private static final String CATEGORY_NAME = "WebHook REST API Jar Conflict";
	
	@NotNull 
	private final ItemCategory myCategory; 
	 
	@NotNull 
	private final WebHookTeamCityRestApiZipPluginFixer myWebHookTeamCityRestApiZipPluginFixer; 
	
	public RestApiJarHealthReport(
								  @NotNull final PagePlaces pagePlaces,
								  @NotNull final PluginDescriptor pluginDescriptor,
								  @NotNull final WebHookTeamCityRestApiZipPluginFixer webHookTeamCityRestApiZipPluginFixer) {
		myWebHookTeamCityRestApiZipPluginFixer = webHookTeamCityRestApiZipPluginFixer; 
	    myCategory = new ItemCategory(CATEGORY_ID, CATEGORY_NAME, ItemSeverity.WARN); 
	    final HealthStatusItemPageExtension myPEx = new HealthStatusItemPageExtension(CATEGORY_ID, pagePlaces) { 
			@Override 
			public boolean isAvailable(@NotNull final HttpServletRequest request) { 
				String pageUrl = (String)request.getAttribute("pageUrl");
				return //myWebHookTeamCityRestApiZipPluginFixer.foundApiZipFilesContainingJaxbJars()
						myWebHookTeamCityRestApiZipPluginFixer.getFoundApiZipFiles().size() > 0
					  //&& isAdminPageOrTemplateEditPage(pageUrl)
					  && super.isAvailable(request); 
			}

			private boolean isAdminPageOrTemplateEditPage(String pageUrl) {
				return pageUrl.contains("/webhooks/template.html") || pageUrl.contains("/admin/");
				
			} 
	    }; 
	    myPEx.setIncludeUrl(pluginDescriptor.getPluginResourcesPath("WebHookRestApi/restApiHealthStatus.jsp")); 
	    //myPEx.addJsFile(pluginDescriptor.getPluginResourcesPath("/js/QueueStateActions.js")); 
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
//		if (myWebHookTeamCityRestApiZipPluginFixer.foundApiZipFilesContainingJaxbJars()) {
//			for (Path p : myWebHookTeamCityRestApiZipPluginFixer.getFoundApiZipFilesContainingJaxbJars()) {
//				
//			}
//		}
	    final HealthStatusItem item = new HealthStatusItem("blah", myCategory, Collections.<String, Object>singletonMap("test", myWebHookTeamCityRestApiZipPluginFixer.getFoundApiZipFiles()));
	    resultConsumer.consumeGlobal(item);
	}

}
