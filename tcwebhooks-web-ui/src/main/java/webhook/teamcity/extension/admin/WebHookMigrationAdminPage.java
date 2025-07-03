package webhook.teamcity.extension.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import com.intellij.util.containers.hash.LinkedHashMap;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import webhook.teamcity.migration.WebHookMigrationController;
import webhook.teamcity.migration.WebHookMigrationController.WebHookTriple;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.WebHookFeaturesStore;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.converter.PluginSettingsToProjectFeaturesMigrator;

public class WebHookMigrationAdminPage extends AdminPage {
	public static final String ADMIN_ID = "tcWebHooksV3Migration";
	private final WebHookSettingsManager myWebHookSettingsManager;
	private @NotNull PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator;
	private @NotNull WebHookFeaturesStore myWebHookFeaturesStore;
	private ProjectManager myProjectManager;


	public WebHookMigrationAdminPage(@NotNull PagePlaces pagePlaces, 
								  @NotNull PluginDescriptor descriptor,
								  @NotNull PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator, 
								  @NotNull WebHookFeaturesStore webHookFeaturesStore,
								  @NotNull WebHookSettingsManager webHookSettingsManager,
								  @NotNull ProjectManager projectManager
								  ) {
		super(pagePlaces);
		this.myWebHookSettingsManager = webHookSettingsManager;
		this.pluginSettingsToProjectFeaturesMigrator = pluginSettingsToProjectFeaturesMigrator;
		this.myWebHookFeaturesStore = webHookFeaturesStore;
		this.myProjectManager = projectManager;
		setPluginName(ADMIN_ID);
		setIncludeUrl(descriptor.getPluginResourcesPath("WebHook/migrationAdminTab.jsp"));
        addCssFile(descriptor.getPluginResourcesPath("WebHook/css/styles.css"));
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
		Map<SProject,Map<String, WebHookTriple>> migrationData = new LinkedHashMap<>();
		Map<SProject,String> reasons = new LinkedHashMap<>();
		Map<SProject,VcsStatuses> vcsStatuses = new LinkedHashMap<>();
		for (SProject myProject : myProjectManager.getActiveProjects()) {
			Pair<String, List<WebHookConfig>> candidates = pluginSettingsToProjectFeaturesMigrator.getCandidates(myProject);
			WebHookProjectSettings migrated = myWebHookFeaturesStore.getWebHookConfigs(myProject);
			List<WebHookConfigEnhanced> cached = myWebHookSettingsManager.getWebHooksForProject(myProject);
			
			vcsStatuses.put(myProject, new VcsStatuses()
					.withIsKotlin(pluginSettingsToProjectFeaturesMigrator.checkIfProjectIsKotlin(myProject))
					.withVcsAndSyncEnabled(pluginSettingsToProjectFeaturesMigrator.checkIfProjectHasVcsEnabledAndSyncEnabled(myProject))
					.withVcsEnabled(pluginSettingsToProjectFeaturesMigrator.checkIfProjectHasVcsEnabled(myProject)));
			
			Map<String, WebHookTriple> webhooks = new LinkedHashMap<>();
			if (candidates.getLeft() != null) {
				reasons.put(myProject, candidates.getLeft());
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
			migrationData.put(myProject, webhooks);
		}
		model.put("reasons", reasons);
		model.put("migrationData", migrationData);
		model.put("vcsStatuses", vcsStatuses);
	}
	
	@Data @AllArgsConstructor @NoArgsConstructor
    public static class VcsStatuses {
		@With
		Boolean vcsEnabled;
		@With
		Boolean vcsAndSyncEnabled;
		@With
		Boolean isKotlin;
		
	}
}