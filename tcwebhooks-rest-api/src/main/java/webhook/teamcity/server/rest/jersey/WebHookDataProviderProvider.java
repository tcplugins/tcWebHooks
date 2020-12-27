package webhook.teamcity.server.rest.jersey;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.server.rest.data.WebHookDataProvider;
import webhook.teamcity.server.rest.data.WebHookFinder;
import webhook.teamcity.server.rest.util.webhook.WebHookManager;

@Provider
@SuppressWarnings("squid:S1191")
public class WebHookDataProviderProvider implements InjectableProvider<Context, Type>, Injectable<WebHookDataProvider> {
  private final WebHookDataProvider dataProvider;

  public WebHookDataProviderProvider(
		  	@NotNull final SBuildServer sBuildServer,
			@NotNull final RootUrlHolder rootUrlHolder,
			@NotNull final PermissionChecker permissionChecker,
			@NotNull final ProjectManager projectManager,
			@NotNull final WebHookFinder webHookFinder,
			@NotNull final WebHookManager webHookManager,
			@NotNull final ProjectIdResolver projectIdResolver,
			@NotNull final BuildTypeIdResolver buildTypeIdResolver,
			@NotNull final SecurityContext securityContext
	) 
  {
	  dataProvider = new WebHookDataProvider(sBuildServer, rootUrlHolder, permissionChecker, projectManager, webHookFinder, webHookManager, projectIdResolver, buildTypeIdResolver, securityContext);
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookDataProvider> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookDataProvider.class)) {
      return this;
    }
    return null;
  }

  public WebHookDataProvider getValue() {
    return dataProvider;
  }

}