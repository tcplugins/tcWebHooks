package webhook.teamcity.migration;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import jetbrains.buildServer.serverSide.ProjectManager;
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
import webhook.teamcity.settings.converter.PluginSettingsToProjectFeaturesMigrator;
import webhook.teamcity.settings.converter.WebHookMigrationStatusService;

@RequiredArgsConstructor
public class WebHookMigrationHealthReports extends HealthStatusReport {

    private static final String CATEGORY_ID = "WebHookMigrationErrors"; 
	private static final String CATEGORY_NAME = "WebHook Migrations";

    // migrationIncompleteHealthStatus.jsp
    @NotNull private final ItemCategory myCategory = new ItemCategory(CATEGORY_ID, CATEGORY_NAME, ItemSeverity.ERROR);
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
        return ImmutableList.of(myCategory); 
    }

    @Override
    public boolean canReportItemsFor(HealthStatusScope scope) {
        return scope.isItemWithSeverityAccepted(myCategory.getSeverity());
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
    }

    public class MigrationNotComplete extends HealthStatusItemPageExtension {

        public MigrationNotComplete(PagePlaces pagePlaces, PluginDescriptor pluginDescriptor) {
            super(CATEGORY_ID, pagePlaces);
            setIncludeUrl(pluginDescriptor.getPluginResourcesPath("WebHook/healthStatuses/migrationIncompleteHealthStatus.jsp")); 
            setVisibleOutsideAdminArea(true); 
            register(); 
        }

        @Override
        public boolean isAvailable(HttpServletRequest request) {
            if (!super.isAvailable(request)) return false;
            SUser user = SessionUser.getUser(request);
            if (user == null) return false;
            return user.isPermissionGrantedGlobally(Permission.VIEW_SERVER_SETTINGS) && !webHookMigrationStatusService.isMigrationCompleted();
        }
    }
}
