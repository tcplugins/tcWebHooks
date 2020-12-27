package webhook.teamcity.test.jerseyprovider;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.mockito.Mockito;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.server.rest.data.WebHookConfigurationValidator;

@Provider
public class WebHookConfigurationValidatorTestContextProvider implements InjectableProvider<Context, Type>, Injectable<WebHookConfigurationValidator> {
	
	private final WebHookConfigurationValidator webHookConfigurationValidator;
	
	public WebHookConfigurationValidatorTestContextProvider() {
		
		System.out.println("We are here: Trying to provide a testable WebHookConfigurationValidator instance");
		SProject sProject = Mockito.mock(SProject.class);
		SProject testProject = Mockito.mock(SProject.class);
		Mockito.when(sProject.getProjectId()).thenReturn("project01");
		Mockito.when(testProject.getProjectId()).thenReturn("project1");
		PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
		ProjectManager projectManager = Mockito.mock(ProjectManager.class);
		Mockito.when(projectManager.findProjectByExternalId(Mockito.eq("_Root"))).thenReturn(sProject);
		Mockito.when(projectManager.findProjectByExternalId(Mockito.eq("TestProject"))).thenReturn(testProject);
		Mockito.when(projectManager.findProjectById("project01")).thenReturn(sProject);
		Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project01")).thenReturn(true);
		Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project1")).thenReturn(true);
		this.webHookConfigurationValidator = new WebHookConfigurationValidator(permissionChecker, projectManager);
	}
	
	  public ComponentScope getScope() {
	    return ComponentScope.Singleton;
	  }

	  public Injectable<WebHookConfigurationValidator> getInjectable(final ComponentContext ic, final Context context, final Type type) {
	    if (type.equals(WebHookConfigurationValidator.class)) {
	      return this;
	    }
	    return null;
	  }

	  public WebHookConfigurationValidator getValue() {
	    return webHookConfigurationValidator;
	  }

}