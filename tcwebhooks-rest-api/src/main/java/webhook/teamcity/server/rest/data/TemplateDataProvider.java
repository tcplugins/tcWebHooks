package webhook.teamcity.server.rest.data;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectable;
import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import org.springframework.stereotype.Component;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;

@JerseyInjectable
@Component
public class TemplateDataProvider extends DataProvider {

	@NotNull private final WebHookTemplateManager myTemplateManager;
	@NotNull private final WebHookPayloadManager myPayloadManager;
	@NotNull private final TemplateFinder myTemplateFinder;
	@NotNull private final WebHookManager myWebHookManager;
	@NotNull private final WebHookUserRequestedExecutor myWebHookUserRequestedExecutor;
	
	public TemplateDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			WebHookPayloadManager payloadManager,
			WebHookTemplateManager templateManager,
			WebHookUserRequestedExecutor userRequestedExecutor,
			TemplateFinder templateFinder, 
			ProjectManager projectManager,
			WebHookManager webHookManager, 
			ProjectIdResolver projectIdResolver,
			SecurityContext securityContext) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myPayloadManager = payloadManager;
		this.myTemplateManager = templateManager;
		this.myTemplateFinder = templateFinder;
		this.myWebHookManager = webHookManager;
		this.myWebHookUserRequestedExecutor = userRequestedExecutor;
	}
	
	public List<WebHookTemplateConfigWrapper> getWebHookTemplates(){
		List<WebHookTemplateConfigWrapper> templates = new ArrayList<>();
		for (WebHookTemplateConfig template : this.myTemplateManager.getRegisteredPermissionedTemplateConfigs()){
			templates.add(new WebHookTemplateConfigWrapper(template,
														   this.getProjectIdResolver().getExternalProjectId(template.getProjectInternalId()),
														   this.myTemplateManager.getTemplateState(template.getId(), TemplateState.BEST),
														   WebHookTemplateStates.build(template)
														  )
						 );
		}
		return templates;
	}
	
	public WebHookManager getWebHookManager() {
		return myWebHookManager;
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
	
	public WebHookUserRequestedExecutor getWebHookUserRequestedExecutor() {
		return myWebHookUserRequestedExecutor;
	}
	
	public TemplateFinder getTemplateFinder() {
		return this.myTemplateFinder;
	}
}
