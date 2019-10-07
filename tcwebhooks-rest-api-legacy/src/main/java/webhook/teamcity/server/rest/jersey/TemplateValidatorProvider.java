package webhook.teamcity.server.rest.jersey;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.data.TemplateValidator;

@Provider
@SuppressWarnings("squid:S1191")
public class TemplateValidatorProvider implements InjectableProvider<Context, Type>, Injectable<TemplateValidator> {
  private final TemplateValidator templateValidator;

  public TemplateValidatorProvider(
		  @NotNull final WebHookTemplateManager templateManager,
		  @NotNull final PermissionChecker permissionChecker,
		  @NotNull final ProjectManager projectManager
		)
  {
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