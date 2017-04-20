package webhook.teamcity.server.rest.data;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

import com.intellij.openapi.diagnostic.Logger;

public class DataProvider {
	private static final Logger LOG = Logger.getInstance(DataProvider.class.getName());

	@NotNull private final SBuildServer myServer;
	@NotNull private final RootUrlHolder myRootUrlHolder;
	@NotNull private final PermissionChecker myPermissionChecker;
	@NotNull private final WebHookTemplateManager myTemplateManager;
	@NotNull private final WebHookPayloadManager myPayloadManager;
	@NotNull private final TemplateFinder myTemplateFinder;

	public DataProvider(@NotNull final SBuildServer server,
						@NotNull final RootUrlHolder rootUrlHolder,
						@NotNull final PermissionChecker permissionChecker,
						@NotNull final WebHookPayloadManager payloadManager,
						@NotNull final WebHookTemplateManager templateManager,
						@NotNull final TemplateFinder templateFinder){

		this.myServer = server;
		this.myRootUrlHolder = rootUrlHolder;
		this.myPermissionChecker = permissionChecker;
		this.myTemplateManager = templateManager;
		this.myPayloadManager = payloadManager;
		this.myTemplateFinder = templateFinder;

	}
	
	public List<WebHookTemplateConfigWrapper> getWebHookTemplates(){
		List<WebHookTemplateConfigWrapper> templates = new ArrayList<>();
		for (WebHookTemplateConfig template : this.myTemplateManager.getRegisteredTemplateConfigs()){
			templates.add(new WebHookTemplateConfigWrapper(template, 
														   this.myTemplateManager.getTemplateState(template.getName())
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
	
}
