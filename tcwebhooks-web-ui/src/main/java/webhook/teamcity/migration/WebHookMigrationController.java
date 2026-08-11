package webhook.teamcity.migration;

import java.io.StringWriter;
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
import webhook.teamcity.Loggers;
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
import webhook.teamcity.settings.converter.WebHookConfigToKotlinDslRenderer;
import webhook.teamcity.settings.converter.WebHookConfigToProjectFeatureXmlRenderer;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WebHookMigrationController extends BaseController {
	
	
	private static final String PROJECT_ATTR = "project";
	public static final String MY_URL = "/webhooks/migration.html";
	private static final String PARAM_ACTION = "action";

	private final WebControllerManager myWebManager;
	private String myPluginPath;
	private PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator;
	private TeamCityCoreFacade myTeamCityCoreFacade;
	private WebHookFeaturesStore myWebHookFeaturesStore;
	private WebHookSettingsManager myWebHookSettingsManager;
	private WebHookConfigToKotlinDslRenderer myWebHookConfigToKotlinDslRenderer;
	private WebHookConfigToProjectFeatureXmlRenderer myWebHookConfigToProjectFeatureXmlRenderer;
	private ProjectFeaturesMigrationAnalyser myProjectFeaturesMigrationAnalyser;
	
	public WebHookMigrationController(
								SBuildServer server,
								PluginSettingsToProjectFeaturesMigrator pluginSettingsToProjectFeaturesMigrator, 
								WebHookFeaturesStore webHookFeaturesStore,
								WebHookSettingsManager webHookSettingsManager,
								PluginDescriptor pluginDescriptor, 
								TeamCityCoreFacade teamCityCoreFacade,
								WebHookConfigToKotlinDslRenderer webHookConfigToKotlinDslRenderer,
								WebHookConfigToProjectFeatureXmlRenderer webHookConfigToProjectFeatureXmlRenderer,
								ProjectFeaturesMigrationAnalyser projectFeaturesMigrationAnalyser,
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
		this.myProjectFeaturesMigrationAnalyser = projectFeaturesMigrationAnalyser;
		this.myWebManager.registerController(MY_URL, this);
		Loggers.SERVER.debug("WebHookMigrationController:: Registering");
	}

    @Nullable
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
	    	if (!isGet(request) && !isPost(request)) {
	    		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	    		return null;
	    	}
	    	
	        SUser myUser = SessionUser.getUser(request);
	        HashMap<String,Object> model = new HashMap<>();
	    	
	        
	        if (isPost(request) && request.getParameter(PROJECT_ATTR) != null) {
    			SProject myProject = this.myTeamCityCoreFacade.findProjectByExtId(request.getParameter(PROJECT_ATTR));
    			if (myUser.isPermissionGrantedForProject(myProject.getProjectId(), Permission.EDIT_PROJECT)) {
    				StringWriter writer = new StringWriter();
    				if (request.getParameter(PARAM_ACTION) != null && request.getParameter(PARAM_ACTION).equals("runMigration")) {
    					this.pluginSettingsToProjectFeaturesMigrator.executeProjectMigration(myProject, writer);
    				}
    				model.put("output", writer.toString());
    			}
	    	}
        
        
    		Map<SProject,String> reasons = new LinkedHashMap<>();
    		ProjectMigrationStatus migrationStatus = new ProjectMigrationStatus();
    		
    		model.put("jspHome",this.myPluginPath);
    		
    		if (request.getParameter(PROJECT_ATTR) != null) {
    			SProject myProject = this.myTeamCityCoreFacade.findProjectByExtId(request.getParameter(PROJECT_ATTR));
    			model.put(PROJECT_ATTR, myProject);
    			migrationStatus.setProject(myProject);
    			if (myUser.isPermissionGrantedForProject(myProject.getProjectId(), Permission.EDIT_PROJECT)) {
    				
    				Pair<String, List<WebHookConfig>> candidates = pluginSettingsToProjectFeaturesMigrator.getCandidates(myProject);
    				WebHookProjectSettings migrated = myWebHookFeaturesStore.getWebHookConfigs(myProject);
    				List<WebHookConfigEnhanced> cached = myWebHookSettingsManager.getWebHooksForProject(myProject);
    				
    				migrationStatus.setProjectVcsStatus(myTeamCityCoreFacade.getProjectVcsStatus(myProject));
    				
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
    				migrationStatus.setWebhooks(webhooks);
    				if (migrationStatus.getProjectVcsStatus().isKotlin()) {
    					List<WebHookConfig> webhookConfigs = candidates.getRight();
    					StringBuilder sb = new StringBuilder();
    					if (webhookConfigs != null && ! webhookConfigs.isEmpty()) {
	    					for(WebHookConfig c : webhookConfigs) {
	    						sb
	    						.append(myWebHookConfigToKotlinDslRenderer.renderAsKotlinDsl(c))
	    						.append("\n\n");
	    					}
    					}
    					migrationStatus.setKotlinDsls(sb.toString());
    					webhookConfigs = migrated.getWebHooksAsList();
    					sb = new StringBuilder();
    					if (webhookConfigs != null && ! webhookConfigs.isEmpty()) {
    						for(WebHookConfig c : webhookConfigs) {
    							sb
    							.append(myWebHookConfigToKotlinDslRenderer.renderAsKotlinDsl(c))
    							.append("\n\n");
    						}
    					}
    					migrationStatus.setMigratedKotlinDsls(sb.toString());
    				} else { //if (migrationStatus.getProjectVcsStatus().isVcsEnabled() && !migrationStatus.getProjectVcsStatus().isVcsSyncEnabled()) {
    					List<WebHookConfig> webhookConfigs = candidates.getRight();
    					StringBuilder sb = new StringBuilder();
    					if (webhookConfigs != null && ! webhookConfigs.isEmpty()) {
    						sb.append(myWebHookConfigToProjectFeatureXmlRenderer.renderAsXml(webhookConfigs, myProject));
    					}
    					migrationStatus.setProjectFeaturesXml(sb.toString());
    					webhookConfigs = migrated.getWebHooksAsList();
    					sb = new StringBuilder();
    					if (webhookConfigs != null && ! webhookConfigs.isEmpty()) {
    						sb.append(myWebHookConfigToProjectFeatureXmlRenderer.renderAsXml(webhookConfigs, myProject));
    					}
    					migrationStatus.setMigratedProjectFeaturesXml(sb.toString());
    				}
    				this.myProjectFeaturesMigrationAnalyser.populateMigrationStatusDetail(migrationStatus);
    			}
    		}

    		model.put("reasons", reasons);
    		model.put("migrationStatus", migrationStatus);
    		
    		return new ModelAndView(myPluginPath + "WebHook/migration.jsp", model); 

    }
}
