package webhook.teamcity.server.rest.data;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectable;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import org.springframework.stereotype.Component;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.ProjectIdResolver;

@Component
@JerseyInjectable
public class WebHookDataProvider extends DataProvider {

	private final WebHookManager myWebHookManager;
	private final BuildTypeIdResolver myBuildTypeIdResolver;
	
	public WebHookDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			ProjectManager projectManager,
			WebHookManager webHookManager, 
			ProjectIdResolver projectIdResolver,
			BuildTypeIdResolver buildTypeIdResolver,
			SecurityContext securityContext) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myWebHookManager = webHookManager;
		this.myBuildTypeIdResolver = buildTypeIdResolver;
	}

	public WebHookManager getWebHookManager() {
		return myWebHookManager;
	}

	
	public BuildTypeIdResolver getBuildTypeIdResolver() {
		return myBuildTypeIdResolver;
	}

}
