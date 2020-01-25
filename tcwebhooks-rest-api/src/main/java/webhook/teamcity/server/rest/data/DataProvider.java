package webhook.teamcity.server.rest.data;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.server.rest.util.webhook.WebHookManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

public class DataProvider {

	@NotNull private final SBuildServer myServer;
	@NotNull private final RootUrlHolder myRootUrlHolder;
	@NotNull private final PermissionChecker myPermissionChecker;
	@NotNull private final WebHookTemplateManager myTemplateManager;
	@NotNull private final WebHookPayloadManager myPayloadManager;
	@NotNull private final TemplateFinder myTemplateFinder;
	@NotNull private final ProjectManager myProjectManager;
	@NotNull private final WebHookManager myWebHookManager;
	@NotNull private final WebHookFinder myWebHookFinder;
	@NotNull private final ProjectIdResolver myProjectIdResolver;

	public DataProvider(@NotNull final SBuildServer server,
						@NotNull final RootUrlHolder rootUrlHolder,
						@NotNull final PermissionChecker permissionChecker,
						@NotNull final WebHookPayloadManager payloadManager,
						@NotNull final WebHookTemplateManager templateManager,
						@NotNull final TemplateFinder templateFinder,
						@NotNull final ProjectManager projectManager, 
						@NotNull final WebHookManager webHookManager,
						@NotNull final WebHookFinder webHookFinder,
						@NotNull final ProjectIdResolver projectIdResolver){

		this.myServer = server;
		this.myRootUrlHolder = rootUrlHolder;
		this.myPermissionChecker = permissionChecker;
		this.myTemplateManager = templateManager;
		this.myPayloadManager = payloadManager;
		this.myTemplateFinder = templateFinder;
		this.myProjectManager = projectManager;
		this.myWebHookManager = webHookManager;
		this.myWebHookFinder = webHookFinder;
		this.myProjectIdResolver = projectIdResolver;

	}
	
	public List<WebHookTemplateConfigWrapper> getWebHookTemplates(){
		List<WebHookTemplateConfigWrapper> templates = new ArrayList<>();
		for (WebHookTemplateConfig template : this.myTemplateManager.getRegisteredTemplateConfigs()){
			templates.add(new WebHookTemplateConfigWrapper(template,
														   myProjectIdResolver.getExternalProjectId(template.getProjectInternalId()),
														   this.myTemplateManager.getTemplateState(template.getId(), TemplateState.BEST),
														   WebHookTemplateStates.build(template)
														  )
						 );
		}
		return templates;
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
	
	public ProjectManager getProjectManager() {
		return this.myProjectManager;
	}
	
	public WebHookManager getWebHookManager() {
		return myWebHookManager;
	}
	
	public WebHookFinder getWebHookFinder() {
		return myWebHookFinder;
	}
	
	public ProjectIdResolver getProjectIdResolver() {
		return myProjectIdResolver;
	}
}
