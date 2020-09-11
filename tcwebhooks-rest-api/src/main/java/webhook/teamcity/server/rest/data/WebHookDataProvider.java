package webhook.teamcity.server.rest.data;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.server.rest.util.webhook.WebHookManager;

public class WebHookDataProvider extends DataProvider {

	private final WebHookFinder myWebHookFinder;
	private final WebHookManager myWebHookManager;
	
	public WebHookDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			ProjectManager projectManager,
			WebHookFinder webHookFinder, 
			WebHookManager webHookManager,
			ProjectIdResolver projectIdResolver,
			SecurityContext securityContext) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myWebHookFinder = webHookFinder;
		this.myWebHookManager = webHookManager;
	}

	public WebHookFinder getWebHookFinder() {
		return myWebHookFinder;
	}

	public WebHookManager getWebHookManager() {
		return myWebHookManager;
	}

}
