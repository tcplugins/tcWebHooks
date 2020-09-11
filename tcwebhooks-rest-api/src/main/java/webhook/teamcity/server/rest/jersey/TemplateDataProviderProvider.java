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
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.data.TemplateDataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.WebHookFinder;

@Provider
@SuppressWarnings("squid:S1191")
public class TemplateDataProviderProvider implements InjectableProvider<Context, Type>, Injectable<TemplateDataProvider> {
  private final TemplateDataProvider dataProvider;

  public TemplateDataProviderProvider(
		  	@NotNull final SBuildServer sBuildServer,
			@NotNull final RootUrlHolder rootUrlHolder,
			@NotNull final PermissionChecker permissionChecker,
			@NotNull final WebHookPayloadManager payloadManager,
			@NotNull final TemplateFinder templateFinder,
			@NotNull final WebHookTemplateManager templateManager,
			@NotNull final ProjectManager projectManager,
			@NotNull final WebHookFinder webHookFinder,
			@NotNull final ProjectIdResolver projectIdResolver,
			@NotNull final SecurityContext securityContext
	) 
  {
	  dataProvider = new TemplateDataProvider(sBuildServer, rootUrlHolder, permissionChecker, payloadManager, templateManager, templateFinder, projectManager, webHookFinder, projectIdResolver, securityContext);
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<TemplateDataProvider> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(TemplateDataProvider.class)) {
      return this;
    }
    return null;
  }

  public TemplateDataProvider getValue() {
    return dataProvider;
  }

}