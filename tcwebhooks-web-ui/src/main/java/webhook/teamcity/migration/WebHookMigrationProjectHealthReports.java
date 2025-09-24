package webhook.teamcity.migration;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusReport;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope;
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory;
import jetbrains.buildServer.serverSide.healthStatus.ItemSeverity;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import lombok.RequiredArgsConstructor;
import webhook.teamcity.Loggers;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.converter.PluginSettingsToProjectFeaturesMigrator;
import webhook.teamcity.settings.converter.WebHookMigrationStatusService;

@RequiredArgsConstructor
public class WebHookMigrationProjectHealthReports extends HealthStatusReport {

    private static final String CATEGORY_ID_PROJECT = "WebHookProjectMigrationErrors"; 
	private static final String CATEGORY_NAME = "WebHook Migrations";

    // migrationIncompleteHealthStatus.jsp
    @NotNull private final ItemCategory myProjectCategory = new ItemCategory(CATEGORY_ID_PROJECT, CATEGORY_NAME, ItemSeverity.ERROR);
    @NotNull private final PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator;
    @NotNull private final PluginDescriptor pluginDescriptor;
    @NotNull private final PagePlaces pagePlaces;
    @NotNull private final ProjectManager projectManager;
    @NotNull private final WebHookMigrationStatusService webHookMigrationStatusService;

    /**
     * Called by spring from bean
     */
    public void init() {
        new MigrationProjectError(pagePlaces, pluginDescriptor);
    }

    @Override
    public String getType() {
        return CATEGORY_ID_PROJECT;
    }

    @Override
    public String getDisplayName() {
        return CATEGORY_NAME;
    }

    @Override
    public Collection<ItemCategory> getCategories() {
        return ImmutableList.of(myProjectCategory); 
    }

    @Override
    public boolean canReportItemsFor(HealthStatusScope scope) {
        return scope.isItemWithSeverityAccepted(myProjectCategory.getSeverity());
    }

    @Override
    public void report(HealthStatusScope scope, HealthStatusItemConsumer resultConsumer) {
        if (!canReportItemsFor(scope)) {
            return;
        }
        if (scope.isItemWithSeverityAccepted(myProjectCategory.getSeverity())){
            projectManager.getActiveProjects().forEach(p -> {
                List<WebHookConfig> configs = webHookMigrationStatusService.getUnMigratedWebHooks(p);
                if (!configs.isEmpty()) {
                    resultConsumer.consumeForProject(p, 
                        new HealthStatusItem("webhook-migration-failure-" + p.getExternalId(), myProjectCategory, 
                            ImmutableMap.of(
                                "Project", p,
                                "WebHookCount", configs.size())));
                }
            });
        }
    }

    public class MigrationProjectError extends HealthStatusItemPageExtension {

        public MigrationProjectError(PagePlaces pagePlaces, PluginDescriptor pluginDescriptor) {
            super(CATEGORY_ID_PROJECT, pagePlaces);
            setIncludeUrl(pluginDescriptor.getPluginResourcesPath("WebHook/healthStatuses/migrationIncompleteHealthStatus.jsp")); 
            setVisibleOutsideAdminArea(true); 
            register(); 
        }

        @Override
        public boolean isAvailable(HttpServletRequest request) {
            return super.isAvailable(request) && !webHookMigrationStatusService.isMigrationCompleted();
        }
    }
}
