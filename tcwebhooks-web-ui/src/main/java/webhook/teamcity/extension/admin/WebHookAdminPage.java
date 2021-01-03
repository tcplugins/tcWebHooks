package webhook.teamcity.extension.admin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import webhook.teamcity.extension.bean.StatisticsChartBean;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookSearchFilter;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.statistics.StatisticsManager;

public class WebHookAdminPage extends AdminPage {
	public static final String TC_WEB_HOOK_REST_API_ADMIN_ID = "tcWebHooks";
	private final WebHookHistoryRepository myWebHookHistoryRepository;
	private final WebHookTemplateManager myWebHookTemplateManager;
	private final WebHookSettingsManager myWebHookSettingsManager;
	private final StatisticsManager myWebHookStatisticsManager;

	public WebHookAdminPage(@NotNull PagePlaces pagePlaces, 
								  @NotNull PluginDescriptor descriptor,
								  @NotNull WebHookHistoryRepository webHookHistoryRepository,
								  @NotNull WebHookTemplateManager webHookTemplateManager,
								  @NotNull WebHookSettingsManager webHookSettingsManager,
								  @NotNull StatisticsManager webHookStatisticsManager
								  ) {
		super(pagePlaces);
		this.myWebHookHistoryRepository = webHookHistoryRepository;
		this.myWebHookTemplateManager = webHookTemplateManager;
		this.myWebHookSettingsManager = webHookSettingsManager;
		this.myWebHookStatisticsManager = webHookStatisticsManager;
		setPluginName(TC_WEB_HOOK_REST_API_ADMIN_ID);
		setIncludeUrl(descriptor.getPluginResourcesPath("WebHook/adminTab.jsp"));
        addCssFile(descriptor.getPluginResourcesPath("WebHook/css/styles.css"));
        addCssFile(descriptor.getPluginResourcesPath("WebHook/css/graph-colours.css"));
        addJsFile(descriptor.getPluginResourcesPath("WebHook/3rd-party/Chart.bundle.min.js"));
        addJsFile(descriptor.getPluginResourcesPath("WebHook/js/admin-chart.js"));
		setTabTitle("WebHooks");
		setPosition(PositionConstraint.after("clouds", "email", "jabber", "plugins", "tcDebRepository", "tcChatBot"));
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
		model.put("statistics", StatisticsChartBean.assemble(LocalDate.now().minusDays(60), LocalDate.now().plusDays(1), myWebHookStatisticsManager.getHistoricalStatistics(LocalDate.now().minusDays(30), LocalDate.now().plusDays(1))).toJson());
		model.put("webHooksCount", myWebHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().show("all").build()).size());
		model.put("webHookTemplatesCount", myWebHookTemplateManager.getRegisteredTemplates().size());
		model.put("errorCount", myWebHookHistoryRepository.getErroredCount());
		model.put("okCount", myWebHookHistoryRepository.getOkCount());
		model.put("skippedCount", myWebHookHistoryRepository.getDisabledCount());
		model.put("totalCount", myWebHookHistoryRepository.getTotalCount());
		model.put("history", myWebHookHistoryRepository.findHistoryErroredItems(1, 20).getItems());
	}
}