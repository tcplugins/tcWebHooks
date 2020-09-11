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
import webhook.teamcity.server.rest.data.WebHookParameterValidator;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.test.springmock.MockProjectManager;

@Provider
public class WebHookParameterValidatorTestContextProvider implements InjectableProvider<Context, Type>, Injectable<WebHookParameterValidator> {
	
	private final WebHookParameterValidator webhookParameterValidator;
	
	public WebHookParameterValidatorTestContextProvider() {
		
		System.out.println("We are here: Trying to provide a testable TemplateValidator instance");
		SProject sProject = Mockito.mock(SProject.class);
		SProject testProject = Mockito.mock(SProject.class);
		Mockito.when(sProject.getProjectId()).thenReturn("project01");
		Mockito.when(sProject.getProjectId()).thenReturn("project01");
		Mockito.when(sProject.getExternalId()).thenReturn("_Root");
		Mockito.when(testProject.getProjectId()).thenReturn("project1");
		Mockito.when(testProject.getExternalId()).thenReturn("TestProject");
		WebHookParameterStore webhookParameterStore = Mockito.mock(WebHookParameterStore.class);
		PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
		ProjectManager projectManager = new MockProjectManager();
//		Mockito.when(projectManager.findProjectByExternalId(Mockito.eq("_Root"))).thenReturn(sProject);
//		Mockito.when(projectManager.findProjectByExternalId(Mockito.eq("TestProject"))).thenReturn(testProject);
//		Mockito.when(projectManager.findProjectById("project01")).thenReturn(sProject);
		Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project01")).thenReturn(true);
		Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project1")).thenReturn(true);
		this.webhookParameterValidator = new WebHookParameterValidator(webhookParameterStore, permissionChecker, projectManager);
	}
	
	  public ComponentScope getScope() {
	    return ComponentScope.Singleton;
	  }

	  public Injectable<WebHookParameterValidator> getInjectable(final ComponentContext ic, final Context context, final Type type) {
	    if (type.equals(WebHookParameterValidator.class)) {
	      return this;
	    }
	    return null;
	  }

	  public WebHookParameterValidator getValue() {
	    return webhookParameterValidator;
	  }

}