package webhook.teamcity.server.rest.data;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import webhook.teamcity.ProjectIdResolver;

public class DataProvider {

	@NotNull private final SBuildServer myServer;
	@NotNull private final RootUrlHolder myRootUrlHolder;
	@NotNull private final PermissionChecker myPermissionChecker;
	@NotNull private final ProjectManager myProjectManager;
	@NotNull
	protected final ProjectIdResolver myProjectIdResolver;
	@NotNull private final SecurityContext mySecurityContext;

	public DataProvider(@NotNull final SBuildServer server,
						@NotNull final RootUrlHolder rootUrlHolder,
						@NotNull final PermissionChecker permissionChecker,
						@NotNull final ProjectManager projectManager, 
						@NotNull final ProjectIdResolver projectIdResolver,
						@NotNull final SecurityContext securityContext
						){

		this.myServer = server;
		this.myRootUrlHolder = rootUrlHolder;
		this.myPermissionChecker = permissionChecker;
		this.myProjectManager = projectManager;
		this.myProjectIdResolver = projectIdResolver;
		this.mySecurityContext = securityContext;

	}
	
	public ProjectManager getProjectManager() {
		return this.myProjectManager;
	}
	
	public ProjectIdResolver getProjectIdResolver() {
		return myProjectIdResolver;
	}
	
	public SecurityContext getSecurityContext() {
		return mySecurityContext;
	}
}
