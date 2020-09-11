package webhook.teamcity.server.rest.data;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import webhook.teamcity.ProjectIdResolver;

public class WebHookParameterDataProvider extends DataProvider {

	@NotNull private final WebHookParameterFinder myWebHookParameterFinder;
	
	public WebHookParameterDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			ProjectManager projectManager,
			ProjectIdResolver projectIdResolver,
			WebHookParameterFinder webHookParameterFinder, 
			SecurityContext securityContext) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myWebHookParameterFinder = webHookParameterFinder;
	}
	
	public WebHookParameterFinder getWebHookParameterFinder() {
		return myWebHookParameterFinder;
	}
	
	

}
