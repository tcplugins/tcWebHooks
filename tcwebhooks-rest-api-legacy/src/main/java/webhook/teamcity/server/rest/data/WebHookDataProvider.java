package webhook.teamcity.server.rest.data;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.ProjectIdResolver;

public class WebHookDataProvider extends DataProvider {

	private final WebHookFinder myWebHookFinder;
	private final BuildTypeIdResolver myBuildTypeIdResolver;
	
	public WebHookDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			ProjectManager projectManager,
			WebHookFinder webHookFinder, 
			ProjectIdResolver projectIdResolver,
			BuildTypeIdResolver buildTypeIdResolver,
			SecurityContext securityContext) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myWebHookFinder = webHookFinder;
		this.myBuildTypeIdResolver = buildTypeIdResolver;
	}

	public WebHookFinder getWebHookFinder() {
		return myWebHookFinder;
	}

	
	public BuildTypeIdResolver getBuildTypeIdResolver() {
		return myBuildTypeIdResolver;
	}

}
