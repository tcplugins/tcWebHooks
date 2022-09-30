package webhook.teamcity.test.jerseyprovider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.springframework.web.context.ContextLoader;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SecurityContextEx;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.data.WebHookStatisticsDataProvider;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.statistics.StatisticsManager;
import webhook.teamcity.statistics.StatisticsReportAssembler;
import webhook.teamcity.test.jerseyprovider.WebHookDataProviderTestContextProvider.TestUrlHolder;
import webhook.teamcity.test.springmock.MockProjectManager;

@Provider
public class WebHookStatisticsDataProviderTestProvider
		implements InjectableProvider<Context, Type>, Injectable<WebHookStatisticsDataProvider> {
	private WebHookStatisticsDataProvider webHookStatisticsDataProvider;
	private WebHookPayloadManager webHookPayloadManager;
	private WebHookTemplateManager webHookTemplateManager;
	private ProjectIdResolver projectIdResolver;
	private StatisticsManager statisticsManager;
	private StatisticsReportAssembler statisticsReportAssembler;
	private SecurityContextEx securityContext;
	private AuthorityHolder authorityHolder;
	private PermissionChecker permissionChecker;
	private SBuildServer sBuildServer;
	private MockProjectManager projectManager;

	public WebHookStatisticsDataProviderTestProvider() {
		System.out.println("We are here: Trying to provide a testable WebHookStatisticsDataProvider instance");
		sBuildServer = mock(SBuildServer.class);
		permissionChecker = mock(PermissionChecker.class);
		projectManager = new MockProjectManager();

		projectIdResolver = mock(ProjectIdResolver.class);
		securityContext = mock(SecurityContextEx.class);
		authorityHolder = mock(AuthorityHolder.class);
		statisticsManager = mock(StatisticsManager.class);
		statisticsReportAssembler = mock(StatisticsReportAssembler.class);
		when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder);
	}

	public ComponentScope getScope() {
		return ComponentScope.Singleton;
	}

	public Injectable<WebHookStatisticsDataProvider> getInjectable(final ComponentContext ic, final Context context,
			final Type type) {
		if (type.equals(WebHookStatisticsDataProvider.class)) {
			System.out.println("WebHookPayloadManagerTestProvider: Providing injectable");
			return this;
		}
		return null;
	}

	@Override
	public WebHookStatisticsDataProvider getValue() {
		//System.out.println("WebHookPayloadManagerTestProvider: Providing value " + statisticsReportAssembler.toString());
		
		webHookPayloadManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookPayloadManager.class);
		webHookTemplateManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookTemplateManager.class);
		//statisticsReportAssembler = ContextLoader.getCurrentWebApplicationContext().getBean(StatisticsReportAssembler.class);
		return new WebHookStatisticsDataProvider(sBuildServer, new TestUrlHolder(), permissionChecker, projectManager, projectIdResolver, securityContext, statisticsManager, statisticsReportAssembler);
	}

}