package webhook.teamcity.server.rest.jersey;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.PathTransformer;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.request.Constants;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class DataProviderProvider implements InjectableProvider<Context, Type>, Injectable<DataProvider> {
  private final DataProvider dataProvider;
  private final SBuildServer sBuildServer;
  private final PermissionChecker permissionChecker;
  private final WebHookPayloadManager payloadManager;
  private final WebHookTemplateManager templateManager;
  private final TemplateFinder templateFinder;
  
  public DataProviderProvider(
		  	@NotNull final SBuildServer server,
			@NotNull final RootUrlHolder rootUrlHolder,
			@NotNull final PermissionChecker permissionChecker,
			@NotNull final WebHookPayloadManager payloadManager,
			@NotNull final TemplateFinder templateFinder,
			@NotNull final WebHookTemplateManager templateManager
		  ) {
	  sBuildServer = server;
	  this.permissionChecker = permissionChecker;
	  this.payloadManager = payloadManager;
	  this.templateManager = templateManager;
	  this.templateFinder = templateFinder;
	  dataProvider = new DataProvider(sBuildServer, rootUrlHolder, permissionChecker, payloadManager, templateManager, templateFinder); 
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<DataProvider> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(DataProvider.class)) {
      return this;
    }
    return null;
  }

  public DataProvider getValue() {
    return dataProvider;
  }
 
}