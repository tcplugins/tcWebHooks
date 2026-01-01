package webhook.teamcity.extension.admin;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import webhook.teamcity.TeamCityCoreFacade;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.WebHookFeaturesStore;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.converter.PluginSettingsToProjectFeaturesMigrator;
import webhook.teamcity.settings.converter.ProjectFeaturesMigrationAnalyser;
import webhook.teamcity.settings.converter.ProjectFeaturesMigrationAnalyser.ProjectMigrationStatus;
import webhook.teamcity.settings.converter.ProjectFeaturesMigrationAnalyser.WebHookTriple;
import webhook.teamcity.settings.converter.WebHookMigrationStatusService;

public class WebHookMigrationAdminPage extends AdminPage {
	private static final String FILENAME_REGEX = "^tcWebHooks-ProjectFeatures-migration-report-\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.txt$";
	private static final String QUERY_STRING_VIEW_REPORT = "viewReport";
	public static final String ADMIN_ID = "tcWebHooksV3Migration";
	private static final DateTimeFormatter reportFriendlyTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault());

	private final WebHookSettingsManager myWebHookSettingsManager;
	private @NotNull PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator;
	private @NotNull WebHookFeaturesStore myWebHookFeaturesStore;
	private TeamCityCoreFacade myTeamCityCoreFacade;
	private @NotNull ProjectFeaturesMigrationAnalyser myProjectFeaturesMigrationAnalyser;
	private @NotNull WebHookMigrationStatusService myWebHookMigrationStatusService;


	public WebHookMigrationAdminPage(@NotNull PagePlaces pagePlaces, 
								  @NotNull PluginDescriptor descriptor,
								  @NotNull PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator, 
								  @NotNull WebHookFeaturesStore webHookFeaturesStore,
								  @NotNull WebHookSettingsManager webHookSettingsManager,
								  @NotNull TeamCityCoreFacade teamCityCoreFacade,
								  @NotNull ProjectFeaturesMigrationAnalyser projectFeaturesMigrationAnalyser,
								  @NotNull WebHookMigrationStatusService webHookMigrationStatusService
								  ) {
		super(pagePlaces);
		this.myWebHookSettingsManager = webHookSettingsManager;
		this.pluginSettingsToProjectFeaturesMigrator = pluginSettingsToProjectFeaturesMigrator;
		this.myWebHookFeaturesStore = webHookFeaturesStore;
		this.myTeamCityCoreFacade = teamCityCoreFacade;
		this.myProjectFeaturesMigrationAnalyser = projectFeaturesMigrationAnalyser;
		this.myWebHookMigrationStatusService = webHookMigrationStatusService;
		setPluginName(ADMIN_ID);
		setIncludeUrl(descriptor.getPluginResourcesPath("WebHook/migrationAdminTab.jsp"));
        addCssFile(descriptor.getPluginResourcesPath("WebHook/css/styles.css"));
        addJsFile(descriptor.getPluginResourcesPath("WebHook/js/3rd-party/highlight/highlight.min.js"));
		setTabTitle("WebHooks Migration");
		setPosition(PositionConstraint.after("plugins", "tcWebHooks"));
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
		if(request.getParameter(QUERY_STRING_VIEW_REPORT) != null && request.getParameter(QUERY_STRING_VIEW_REPORT).matches(FILENAME_REGEX)) {
			model.put("migrationReport", this.pluginSettingsToProjectFeaturesMigrator.getReport(request.getParameter(QUERY_STRING_VIEW_REPORT)));
		}
		List<ProjectMigrationStatus> migrationStatuses = new ArrayList<>();
		model.put("overallStatus", this.myWebHookMigrationStatusService.getMigrationStatus());
		model.put("reportFileNames", this.pluginSettingsToProjectFeaturesMigrator.getPreviousReports().stream().map(p -> Pair.of(p.getLeft(), p.getRight().format(reportFriendlyTimeFormatter))).collect(Collectors.toList()));

		for (SProject myProject : myTeamCityCoreFacade.getActiveProjects()) {
			ProjectMigrationStatus migrationStatus = new ProjectMigrationStatus().withProject(myProject);
			Pair<String, List<WebHookConfig>> candidates = pluginSettingsToProjectFeaturesMigrator.getCandidates(myProject);
			WebHookProjectSettings migrated = myWebHookFeaturesStore.getWebHookConfigs(myProject);
			List<WebHookConfigEnhanced> cached = myWebHookSettingsManager.getWebHooksForProject(myProject);
			
			migrationStatus.setProjectVcsStatus(myTeamCityCoreFacade.getProjectVcsStatus(myProject));
			Map<String, WebHookTriple> webhooks = new LinkedHashMap<>();
			if (candidates.getLeft() != null) {
				migrationStatus.setReason(candidates.getLeft());
			}
			if (candidates.getRight() != null) {
				candidates.getRight().forEach(w -> {
					if (webhooks.containsKey(w.getUniqueKey())) {
						webhooks.put(w.getUniqueKey(), webhooks.get(w.getUniqueKey()).withCandidate(w));
					} else {
						webhooks.put(w.getUniqueKey(), new WebHookTriple().withCandidate(w));
					}
				});
			}
			migrated.getWebHooksAsList().forEach(w -> {
				if (webhooks.containsKey(w.getUniqueKey())) {
					webhooks.put(w.getUniqueKey(), webhooks.get(w.getUniqueKey()).withMigrated(w));
				} else {
					webhooks.put(w.getUniqueKey(), new WebHookTriple().withMigrated(w));
				}
			});
			if (cached != null) {
				cached.forEach(w -> {
					if (webhooks.containsKey(w.getWebHookConfig().getUniqueKey())) {
						webhooks.put(w.getWebHookConfig().getUniqueKey(), webhooks.get(w.getWebHookConfig().getUniqueKey()).withCached(w));
					} else {
						webhooks.put(w.getWebHookConfig().getUniqueKey(), new WebHookTriple().withCached(w));
					}
				});
			}
			migrationStatuses.add(this.myProjectFeaturesMigrationAnalyser.populateMigrationStatusDetail(migrationStatus.withWebhooks(webhooks)));
		}
		model.put("migrationStatuses", migrationStatuses);
	}
	
}