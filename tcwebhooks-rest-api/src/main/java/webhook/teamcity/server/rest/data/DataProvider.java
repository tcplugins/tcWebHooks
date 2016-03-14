package webhook.teamcity.server.rest.data;

import java.util.List;

import jetbrains.buildServer.BuildProject;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.Locator;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplate;
import webhook.teamcity.settings.entity.builder.WebHookTemplateEntityBuilder;

import com.intellij.openapi.diagnostic.Logger;

public class DataProvider {
	private static final Logger LOG = Logger.getInstance(DataProvider.class.getName());

	@NotNull private final SBuildServer myServer;
	@NotNull private final RootUrlHolder myRootUrlHolder;
	@NotNull private final PermissionChecker myPermissionChecker;
	@NotNull private final WebHookTemplateManager myTemplateManager;
	@NotNull private final WebHookPayloadManager myPayloadManager;

	public DataProvider(@NotNull final SBuildServer server,
						@NotNull final RootUrlHolder rootUrlHolder,
						@NotNull final PermissionChecker permissionChecker,
						@NotNull final WebHookPayloadManager payloadManager,
						@NotNull final WebHookTemplateManager templateManager) {

		this.myServer = server;
		this.myRootUrlHolder = rootUrlHolder;
		this.myPermissionChecker = permissionChecker;
		this.myTemplateManager = templateManager;
		this.myPayloadManager = payloadManager;

	}
	
	public List<WebHookTemplate> getWebHookTemplates(){
		return WebHookTemplateEntityBuilder.buildAll(this.myTemplateManager.getRegisteredTemplates());
	}
	
	public WebHookTemplate getWebHookTemplate(String id){
		return WebHookTemplateEntityBuilder.build(this.myTemplateManager.getTemplate(id));
	}

	public WebHookPayloadManager getPayloadManager() {
		return this.myPayloadManager;
	}
	
}
