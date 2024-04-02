package webhook.teamcity.server.rest.data;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectable;
import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import org.springframework.stereotype.Component;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.settings.project.WebHookParameterStore;

@Component
@JerseyInjectable
public class WebHookParameterDataProvider extends DataProvider {

	@NotNull private final WebHookParameterFinder myWebHookParameterFinder;
	@NotNull private final  WebHookParameterStore webHookParameterStore;
	
	public WebHookParameterDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			ProjectManager projectManager,
			ProjectIdResolver projectIdResolver,
			SecurityContext securityContext,
			WebHookParameterFinder webHookParameterFinder,
			WebHookParameterStore webHookParameterStore
			) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myWebHookParameterFinder = webHookParameterFinder;
		this.webHookParameterStore = webHookParameterStore;
	}
	
	public WebHookParameterFinder getWebHookParameterFinder() {
		return myWebHookParameterFinder;
	}
	
	public WebHookParameterStore getWebHookParameterStore() {
		return webHookParameterStore;
	}

}
