package webhook.teamcity.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import com.intellij.util.containers.hash.LinkedHashMap;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import webhook.teamcity.Loggers;
import webhook.teamcity.extension.admin.WebHookMigrationAdminPage.VcsStatuses;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.WebHookFeaturesStore;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.converter.PluginSettingsToProjectFeaturesMigrator;
import webhook.teamcity.settings.converter.WebHookConfigToKotlinDslRenderer;
import webhook.teamcity.settings.converter.WebHookConfigToProjectFeatureXmlRenderer;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookMigrationController extends BaseController {
	
	
	public static final String MY_URL = "/webhooks/migration.html";
	private final WebControllerManager myWebManager;
	private String myPluginPath;
	private PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator;
	private ProjectManager myProjectManager;
	private WebHookFeaturesStore myWebHookFeaturesStore;
	private WebHookSettingsManager myWebHookSettingsManager;
	private WebHookConfigToKotlinDslRenderer myWebHookConfigToKotlinDslRenderer;
	private WebHookConfigToProjectFeatureXmlRenderer myWebHookConfigToProjectFeatureXmlRenderer;
	
	public WebHookMigrationController(
								SBuildServer server,
								PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator, 
								WebHookFeaturesStore webHookFeaturesStore,
								WebHookSettingsManager webHookSettingsManager,
								PluginDescriptor pluginDescriptor, 
								ProjectManager projectManager,
								WebHookConfigToKotlinDslRenderer webHookConfigToKotlinDslRenderer,
								WebHookConfigToProjectFeatureXmlRenderer webHookConfigToProjectFeatureXmlRenderer,
								WebControllerManager webControllerManager) {
		super(server);
		this.pluginSettingsToProjectFeaturesMigrator = pluginSettingsToProjectFeaturesMigrator;
		this.myPluginPath = pluginDescriptor.getPluginResourcesPath();
		this.myWebHookFeaturesStore = webHookFeaturesStore;
		this.myWebHookSettingsManager = webHookSettingsManager;
		this.myProjectManager = projectManager;
		this.myWebManager = webControllerManager;
		this.myWebHookConfigToKotlinDslRenderer = webHookConfigToKotlinDslRenderer;
		this.myWebHookConfigToProjectFeatureXmlRenderer = webHookConfigToProjectFeatureXmlRenderer;
		this.myWebManager.registerController(MY_URL, this);
		Loggers.SERVER.debug("WebHookMigrationController:: Registering");
	}

    @Nullable
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
        SUser myUser = SessionUser.getUser(request);
    	
    	if (isGet(request)) {
    		HashMap<String,Object> model = new HashMap<>();
    		Map<SProject,Map<String, WebHookTriple>> migrationData = new LinkedHashMap<>();
    		Map<SProject,String> reasons = new LinkedHashMap<>();
    		Map<SProject,VcsStatuses> vcsStatuses = new LinkedHashMap<>();
    		
    		model.put("jspHome",this.myPluginPath);
    		
    		if (request.getParameter("project") != null) {
    			SProject myProject = this.myProjectManager.findProjectByExternalId(request.getParameter("project"));
    			if (myUser.isPermissionGrantedForProject(myProject.getProjectId(), Permission.EDIT_PROJECT)) {
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
    				//model.put("webhooks", webhooks);
    				if (vcsStatuses.get(myProject).getIsKotlin()) {
    					List<WebHookConfig> webhookConfigs = candidates.getRight();
    					StringBuilder sb = new StringBuilder();
    					if (webhookConfigs != null && ! webhookConfigs.isEmpty()) {
	    					for(WebHookConfig c : webhookConfigs) {
	    						sb
	    						.append(myWebHookConfigToKotlinDslRenderer.renderAsKotlinDsl(c))
	    						.append("\n\n");
	    					}
    					}
    					model.put("kotlinDsls", sb.toString());
    				} else if (vcsStatuses.get(myProject).getVcsEnabled() && !vcsStatuses.get(myProject).getVcsAndSyncEnabled()) {
    					List<WebHookConfig> webhookConfigs = candidates.getRight();
    					StringBuilder sb = new StringBuilder();
    					if (webhookConfigs != null && ! webhookConfigs.isEmpty()) {
    						sb.append(myWebHookConfigToProjectFeatureXmlRenderer.renderAsXml(webhookConfigs));
    					}
    					model.put("projectFeaturesXml", sb.toString());
    				}
    			}
    		}

    		model.put("reasons", reasons);
    		model.put("migrationData", migrationData);
    		model.put("vcsStatuses", vcsStatuses);
    		
    		return new ModelAndView(myPluginPath + "WebHook/migration.jsp", model); 

    	}
    	response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    	return null;
    }
    
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class WebHookTriple {
    	@With
    	String reason;
    	@With
    	WebHookConfig candidate;
    	@With
    	WebHookConfig migrated;
    	@With
    	WebHookConfigEnhanced cached;
    }
}
