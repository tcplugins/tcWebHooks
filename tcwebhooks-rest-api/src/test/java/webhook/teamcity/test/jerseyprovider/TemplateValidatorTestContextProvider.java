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
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.data.TemplateValidator;
import webhook.teamcity.server.rest.jersey.TemplateValidatorProvider;

@Provider
public class TemplateValidatorTestContextProvider extends TemplateValidatorProvider
		implements InjectableProvider<Context, Type>, Injectable<TemplateValidator> {
	
	private final TemplateValidator templateValidator;
	
	public TemplateValidatorTestContextProvider() {
		
		System.out.println("We are here: Trying to provide a testable TemplateValidator instance");
		WebHookTemplateManager templateManager = Mockito.mock(WebHookTemplateManager.class);
		PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
		ProjectManager projectManager = Mockito.mock(ProjectManager.class);
		this.templateValidator = new TemplateValidator(templateManager, permissionChecker, projectManager);
	}
	
	  public ComponentScope getScope() {
	    return ComponentScope.Singleton;
	  }

	  public Injectable<TemplateValidator> getInjectable(final ComponentContext ic, final Context context, final Type type) {
	    if (type.equals(TemplateValidator.class)) {
	      return this;
	    }
	    return null;
	  }

	  public TemplateValidator getValue() {
	    return templateValidator;
	  }

}