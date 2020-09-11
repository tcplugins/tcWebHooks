package webhook.teamcity.server.rest.data;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

public class TemplateDataProvider extends DataProvider {

	@NotNull private final WebHookTemplateManager myTemplateManager;
	@NotNull private final WebHookPayloadManager myPayloadManager;
	@NotNull private final TemplateFinder myTemplateFinder;
	@NotNull private final WebHookFinder myWebHookFinder;
	
	public TemplateDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			WebHookPayloadManager payloadManager,
			WebHookTemplateManager templateManager, 
			TemplateFinder templateFinder, 
			ProjectManager projectManager,
			WebHookFinder webHookFinder, 
			ProjectIdResolver projectIdResolver,
			SecurityContext securityContext) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myWebHookFinder = webHookFinder;
		this.myTemplateManager = templateManager;
		this.myPayloadManager = payloadManager;
		this.myTemplateFinder = templateFinder;
	}
	
	public List<WebHookTemplateConfigWrapper> getWebHookTemplates(){
		List<WebHookTemplateConfigWrapper> templates = new ArrayList<>();
		for (WebHookTemplateConfig template : this.myTemplateManager.getRegisteredPermissionedTemplateConfigs()){
			templates.add(new WebHookTemplateConfigWrapper(template,
														   myProjectIdResolver.getExternalProjectId(template.getProjectInternalId()),
														   this.myTemplateManager.getTemplateState(template.getId(), TemplateState.BEST),
														   WebHookTemplateStates.build(template)
														  )
						 );
		}
		return templates;
	}
	
	public WebHookFinder getWebHookFinder() {
		return myWebHookFinder;
	}
	public WebHookTemplateConfig getWebHookTemplate(String id){
		return this.myTemplateManager.getTemplate(id).getAsConfig();
	}

	public WebHookPayloadManager getPayloadManager() {
		return this.myPayloadManager;
	}
	
	public WebHookTemplateManager getTemplateManager() {
		return this.myTemplateManager;
	}
	
	public TemplateFinder getTemplateFinder() {
		return this.myTemplateFinder;
	}
}
