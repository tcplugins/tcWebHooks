package webhook.teamcity.migration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusReport;
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope;
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory;
import jetbrains.buildServer.serverSide.healthStatus.ItemSeverity;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import jetbrains.buildServer.web.util.SessionUser;
import lombok.RequiredArgsConstructor;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.converter.PluginSettingsToProjectFeaturesMigrator;
import webhook.teamcity.settings.converter.WebHookMigrationStatusService;

@RequiredArgsConstructor
public class WebHookMigrationHealthReports extends HealthStatusReport {

    private static final String CATEGORY_ID = "WebHookMigrationErrors"; 
    private static final String CATEGORY_ID_PROJECT = "WebHookProjectMigrationErrors"; 
	private static final String CATEGORY_NAME = "WebHook Migrations";

    // migrationIncompleteHealthStatus.jsp
    @NotNull private final ItemCategory myCategory = new ItemCategory(CATEGORY_ID, CATEGORY_NAME, ItemSeverity.WARN);
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
        new MigrationNotComplete(pagePlaces, pluginDescriptor);
        new MigrationProjectError(pagePlaces, pluginDescriptor);
    }

    @Override
    public String getType() {
        return CATEGORY_ID;
    }

    @Override
    public String getDisplayName() {
        return CATEGORY_NAME;
    }

    @Override
    public Collection<ItemCategory> getCategories() {
        return ImmutableList.of(myCategory, myProjectCategory); 
    }

    @Override
    public boolean canReportItemsFor(HealthStatusScope scope) {
        return scope.isItemWithSeverityAccepted(myCategory.getSeverity()) 
            || scope.isItemWithSeverityAccepted(myProjectCategory.getSeverity());
    }

    @Override
    public void report(HealthStatusScope scope, HealthStatusItemConsumer resultConsumer) {
        if (!canReportItemsFor(scope)) {
            return;
        }
        if (scope.isItemWithSeverityAccepted(myCategory.getSeverity()) && !webHookMigrationStatusService.isMigrationCompleted()) {
            final HealthStatusItem item = new HealthStatusItem("webhook-migration-failure", myCategory, 
                ImmutableMap.of("Status", webHookMigrationStatusService.getMigrationStatus()));
            resultConsumer.consumeGlobal(item);
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

    public class MigrationNotComplete extends HealthStatusItemPageExtension {

        public MigrationNotComplete(PagePlaces pagePlaces, PluginDescriptor pluginDescriptor) {
            super(CATEGORY_ID, pagePlaces);
            setIncludeUrl(pluginDescriptor.getPluginResourcesPath("WebHook/healthStatuses/migrationIncompleteHealthStatus.jsp")); 
            setVisibleOutsideAdminArea(false); 
            register(); 
        }

        @Override
        public boolean isAvailable(HttpServletRequest request) {
            //SUser myUser = SessionUser.getUser(request);
            //return (myUser.isPermissionGrantedGlobally(Permission.CHANGE_SERVER_SETTINGS) && 
            return !webHookMigrationStatusService.isMigrationCompleted();
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
            return !webHookMigrationStatusService.isMigrationCompleted();
        }
        
    }
}
