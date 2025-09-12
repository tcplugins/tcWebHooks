package webhook.teamcity.extension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
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
import webhook.teamcity.TeamCityCoreFacade;
import webhook.teamcity.TeamCityCoreFacade.ProjectVcsStatus;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.WebHookFeaturesStore;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.WebHookSettingsManager;
import webhook.teamcity.settings.converter.PluginSettingsToProjectFeaturesMigrator;
import webhook.teamcity.settings.converter.WebHookConfigToKotlinDslRenderer;
import webhook.teamcity.settings.converter.WebHookConfigToProjectFeatureXmlRenderer;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookViewConfigurationController extends BaseController {
	
	
	public static final String MY_URL = "/webhooks/view-configuration.html";
	private final WebControllerManager myWebManager;
	private String myPluginPath;
	private PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator;
	private TeamCityCoreFacade myTeamCityCoreFacade;
	private WebHookFeaturesStore myWebHookFeaturesStore;
	private WebHookSettingsManager myWebHookSettingsManager;
	private WebHookConfigToKotlinDslRenderer myWebHookConfigToKotlinDslRenderer;
	private WebHookConfigToProjectFeatureXmlRenderer myWebHookConfigToProjectFeatureXmlRenderer;
	
	public WebHookViewConfigurationController(
								SBuildServer server,
								PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator, 
								WebHookFeaturesStore webHookFeaturesStore,
								WebHookSettingsManager webHookSettingsManager,
								PluginDescriptor pluginDescriptor, 
								TeamCityCoreFacade teamCityCoreFacade,
								WebHookConfigToKotlinDslRenderer webHookConfigToKotlinDslRenderer,
								WebHookConfigToProjectFeatureXmlRenderer webHookConfigToProjectFeatureXmlRenderer,
								WebControllerManager webControllerManager) {
		super(server);
		this.pluginSettingsToProjectFeaturesMigrator = pluginSettingsToProjectFeaturesMigrator;
		this.myPluginPath = pluginDescriptor.getPluginResourcesPath();
		this.myWebHookFeaturesStore = webHookFeaturesStore;
		this.myWebHookSettingsManager = webHookSettingsManager;
		this.myTeamCityCoreFacade = teamCityCoreFacade;
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
    		Map<SProject,ProjectVcsStatus> vcsStatuses = new LinkedHashMap<>();
    		
    		model.put("jspHome",this.myPluginPath);
    		
    		if (request.getParameter("project") != null) {
    			SProject myProject = this.myTeamCityCoreFacade.findProjectByExtId(request.getParameter("project"));
    			if (myUser.isPermissionGrantedForProject(myProject.getProjectId(), Permission.EDIT_PROJECT)) {
    				//Pair<String, List<WebHookConfig>> candidates = pluginSettingsToProjectFeaturesMigrator.getCandidates(myProject);
    				WebHookProjectSettings migrated = myWebHookFeaturesStore.getWebHookConfigs(myProject);
    				List<WebHookConfigEnhanced> cached = myWebHookSettingsManager.getWebHooksForProject(myProject);
    				
    				vcsStatuses.put(myProject, myTeamCityCoreFacade.getProjectVcsStatus(myProject));
    				
    				Map<String, WebHookTriple> webhooks = new LinkedHashMap<>();
//    				if (candidates.getLeft() != null) {
//    					reasons.put(myProject, candidates.getLeft());
//    				}
//    				if (candidates.getRight() != null) {
//    					candidates.getRight().forEach(w -> {
//    						if (webhooks.containsKey(w.getUniqueKey())) {
//    							webhooks.put(w.getUniqueKey(), webhooks.get(w.getUniqueKey()).withCandidate(w));
//    						} else {
//    							webhooks.put(w.getUniqueKey(), new WebHookTriple().withCandidate(w));
//    						}
//    					});
//    				}
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
    					List<WebHookConfig> webhookConfigs = migrated.getWebHooksConfigs();
    					if (webhookConfigs != null && ! webhookConfigs.isEmpty()) {
	    					for(WebHookConfig c : webhookConfigs) {
	    						if (vcsStatuses.get(myProject).isKotlin()) {
	    							webhooks.put(c.getUniqueKey(), webhooks.get(c.getUniqueKey()).withFormat("kotlin").withCode(myWebHookConfigToKotlinDslRenderer.renderAsKotlinDsl(c)));
	    						} else {
	    							webhooks.put(c.getUniqueKey(), webhooks.get(c.getUniqueKey()).withFormat("xml").withCode(myWebHookConfigToProjectFeatureXmlRenderer.renderAsXml(c)));
	    						}
	    					}
    					}
    					migrationData.put(myProject, webhooks);
    				}
    			}

	    		model.put("reasons", reasons);
	    		model.put("migrationData", migrationData);
	    		model.put("vcsStatuses", vcsStatuses);
	    		
	    		return new ModelAndView(myPluginPath + "WebHook/viewConfiguration.jsp", model); 
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
    	@With
    	String code;
    	@With
    	String format;
    }
}
