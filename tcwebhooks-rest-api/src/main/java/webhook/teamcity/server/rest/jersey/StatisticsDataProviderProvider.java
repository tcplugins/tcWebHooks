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
import webhook.teamcity.server.rest.data.WebHookDataProvider;
import webhook.teamcity.server.rest.data.WebHookStatisticsDataProvider;
import webhook.teamcity.statistics.StatisticsReportAssembler;

@Provider
@SuppressWarnings("squid:S1191")
public class StatisticsDataProviderProvider implements InjectableProvider<Context, Type>, Injectable<WebHookStatisticsDataProvider> {
  private final WebHookStatisticsDataProvider dataProvider;

  public StatisticsDataProviderProvider(
		  	@NotNull final SBuildServer sBuildServer,
			@NotNull final RootUrlHolder rootUrlHolder,
			@NotNull final PermissionChecker permissionChecker,
			@NotNull final ProjectManager projectManager,
			@NotNull final ProjectIdResolver projectIdResolver,
			@NotNull final SecurityContext securityContext,
			@NotNull final StatisticsReportAssembler statisticsReportAssembler
	) 
  {
	  dataProvider = new WebHookStatisticsDataProvider(sBuildServer, rootUrlHolder, permissionChecker, projectManager, projectIdResolver, securityContext, statisticsReportAssembler);
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookStatisticsDataProvider> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookStatisticsDataProvider.class)) {
      return this;
    }
    return null;
  }

  public WebHookStatisticsDataProvider getValue() {
    return dataProvider;
  }

}