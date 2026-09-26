package webhook.teamcity.test.jerseyprovider;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.function.Supplier;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.mockito.Mockito;
import org.springframework.web.context.WebApplicationContext;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.ApiUrlBuilder;
import jetbrains.buildServer.server.rest.PathTransformer;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SecurityContextEx;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.auth.bearer.BearerAuthenticatorFactory;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.WebHookWebLinks;
import webhook.teamcity.server.rest.request.Constants;
import webhook.teamcity.server.rest.data.TemplateDataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.TemplateValidator;
import webhook.teamcity.server.rest.data.WebHookConfigurationValidator;
import webhook.teamcity.server.rest.data.WebHookDataProvider;
import webhook.teamcity.server.rest.data.WebHookManager;
import webhook.teamcity.server.rest.data.WebHookParameterDataProvider;
import webhook.teamcity.server.rest.data.WebHookParameterFinder;
import webhook.teamcity.server.rest.data.WebHookParameterValidator;
import webhook.teamcity.server.rest.data.WebHookStatisticsDataProvider;
import webhook.teamcity.server.rest.util.mainconfig.MainConfigManager;
import webhook.teamcity.server.rest.util.WebHookBeanContext;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;
import webhook.teamcity.statistics.StatisticsManager;
import webhook.teamcity.statistics.StatisticsReport;
import webhook.teamcity.statistics.StatisticsReportAssembler;
import webhook.teamcity.test.springmock.MockProjectManager;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;

/**
 * Test binder that prefers direct HK2/Spring wiring over legacy Jersey1 provider classes.
 */
public class WebHookTestBinder extends AbstractBinder {

    private final WebApplicationContext springContext;

    public WebHookTestBinder(WebApplicationContext springContext) {
        this.springContext = springContext;
    }

    @Override
    protected void configure() {
        bindIfPresent(getSafely(() -> springContext.getBean(MainConfigManager.class)), MainConfigManager.class);
        bindIfPresent(getSafely(this::createWebHookBeanContext), WebHookBeanContext.class);
        bind(Mockito.mock(HttpServletRequest.class)).to(HttpServletRequest.class);
        bindIfPresent(getSafely(() -> springContext.getBean(WebHookUserRequestedExecutor.class)), WebHookUserRequestedExecutor.class);
        bind(Mockito.mock(PermissionChecker.class)).to(PermissionChecker.class);
        bind(Mockito.mock(ServiceLocator.class)).to(ServiceLocator.class);
        bindIfPresent(getSafely(this::createTemplateDataProvider), TemplateDataProvider.class);
        bindIfPresent(getSafely(this::createTemplateValidator), TemplateValidator.class);
        bind(createApiUrlBuilder()).to(ApiUrlBuilder.class);
        bindIfPresent(getSafely(this::createWebHookConfigurationValidator), WebHookConfigurationValidator.class);
        bindIfPresent(getSafely(this::createWebHookDataProvider), WebHookDataProvider.class);
        bindIfPresent(getSafely(this::createWebHookParameterDataProvider), WebHookParameterDataProvider.class);
        bindIfPresent(getSafely(() -> springContext.getBean(WebHookParameterStoreFactory.class).getWebHookParameterStore()), WebHookParameterStore.class);
        bindIfPresent(getSafely(this::createWebHookParameterValidator), WebHookParameterValidator.class);
        bindIfPresent(getSafely(() -> springContext.getBean(WebHookPayloadManager.class)), WebHookPayloadManager.class);
        bind(createPluginDataResolverMock()).to(WebHookPluginDataResolver.class);
        bind(new WebHookWebLinks(createRootUrlHolder())).to(WebHookWebLinks.class);
        bindIfPresent(getSafely(this::createWebHookStatisticsDataProvider), WebHookStatisticsDataProvider.class);
        bindIfPresent(getSafely(() -> springContext.getBean(WebHookTemplateManager.class)), WebHookTemplateManager.class);
    }

    private WebHookBeanContext createWebHookBeanContext() {
        return new WebHookBeanContext(
                new WebHookWebLinks(createRootUrlHolder()),
                new WebHookApiUrlBuilder(path -> path));
    }

    private ApiUrlBuilder createApiUrlBuilder() {
        return new ApiUrlBuilder(new PathTransformer() {
            @Override
            public String transform(String path) {
                if (path == null || path.isEmpty()) {
                    return Constants.API_URL;
                }
                if (path.startsWith(Constants.API_URL)) {
                    return path;
                }
                return path.startsWith("/") ? Constants.API_URL + path : Constants.API_URL + "/" + path;
            }
        });
    }

    private TemplateValidator createTemplateValidator() {
        SProject rootProject = Mockito.mock(SProject.class);
        SProject testProject = Mockito.mock(SProject.class);
        Mockito.when(rootProject.getProjectId()).thenReturn("project01");
        Mockito.when(testProject.getProjectId()).thenReturn("project1");

        PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
        ProjectManager projectManager = Mockito.mock(ProjectManager.class);
        Mockito.when(projectManager.findProjectByExternalId(Mockito.eq("_Root"))).thenReturn(rootProject);
        Mockito.when(projectManager.findProjectByExternalId(Mockito.eq("TestProject"))).thenReturn(testProject);
        Mockito.when(projectManager.findProjectById("project01")).thenReturn(rootProject);
        Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project01")).thenReturn(true);
        Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project1")).thenReturn(true);

        return new TemplateValidator(Mockito.mock(WebHookTemplateManager.class), permissionChecker, projectManager);
    }

    private WebHookConfigurationValidator createWebHookConfigurationValidator() {
        SProject rootProject = Mockito.mock(SProject.class);
        SProject testProject = Mockito.mock(SProject.class);
        Mockito.when(rootProject.getProjectId()).thenReturn("project01");
        Mockito.when(testProject.getProjectId()).thenReturn("project1");

        PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
        ProjectManager projectManager = Mockito.mock(ProjectManager.class);
        Mockito.when(projectManager.findProjectByExternalId(Mockito.eq("_Root"))).thenReturn(rootProject);
        Mockito.when(projectManager.findProjectByExternalId(Mockito.eq("TestProject"))).thenReturn(testProject);
        Mockito.when(projectManager.findProjectById("project01")).thenReturn(rootProject);
        Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project01")).thenReturn(true);
        Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project1")).thenReturn(true);

        WebHookAuthenticatorProvider authProvider = new WebHookAuthenticatorProvider();
        authProvider.registerAuthType(new BearerAuthenticatorFactory(authProvider));
        return new WebHookConfigurationValidator(permissionChecker, projectManager, authProvider);
    }

    private WebHookParameterValidator createWebHookParameterValidator() {
        PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
        Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project01")).thenReturn(true);
        Mockito.when(permissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, "project1")).thenReturn(true);
        return new WebHookParameterValidator(
                Mockito.mock(WebHookParameterStore.class),
                permissionChecker,
                new MockProjectManager());
    }

    private WebHookDataProvider createWebHookDataProvider() {
        SecurityContextEx securityContext = Mockito.mock(SecurityContextEx.class);
        AuthorityHolder authorityHolder = Mockito.mock(AuthorityHolder.class);
        Mockito.when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder);
        Mockito.when(authorityHolder.isPermissionGrantedForAnyProject(Mockito.eq(Permission.EDIT_PROJECT))).thenReturn(true);
        Mockito.when(authorityHolder.isPermissionGrantedForAnyProject(Mockito.eq(Permission.VIEW_PROJECT))).thenReturn(true);

        PermissionChecker permissionChecker = Mockito.mock(PermissionChecker.class);
        Mockito.when(permissionChecker.isPermissionGranted(Mockito.any(Permission.class), Mockito.anyString()))
            .thenReturn(true);
        Mockito.when(permissionChecker.isPermissionGranted(Mockito.any(Permission.class), Mockito.isNull()))
            .thenReturn(true);
        Mockito.when(permissionChecker.isPermissionGranted(Mockito.eq(Permission.EDIT_PROJECT), Mockito.anyString()))
            .thenReturn(true);
        Mockito.when(permissionChecker.isPermissionGranted(Mockito.eq(Permission.EDIT_PROJECT), Mockito.isNull()))
            .thenReturn(true);
        Mockito.when(permissionChecker.isPermissionGranted(Mockito.eq(Permission.VIEW_PROJECT), Mockito.anyString()))
            .thenReturn(true);
        Mockito.when(permissionChecker.isPermissionGranted(Mockito.eq(Permission.VIEW_PROJECT), Mockito.isNull()))
            .thenReturn(true);

        return new WebHookDataProvider(
                Mockito.mock(SBuildServer.class),
                createRootUrlHolder(),
            permissionChecker,
                new MockProjectManager(),
                springContext.getBean(WebHookManager.class),
                Mockito.mock(ProjectIdResolver.class),
                Mockito.mock(BuildTypeIdResolver.class),
                securityContext);
    }

    private TemplateDataProvider createTemplateDataProvider() {
        SecurityContextEx securityContext = Mockito.mock(SecurityContextEx.class);
        AuthorityHolder authorityHolder = Mockito.mock(AuthorityHolder.class);
        Mockito.when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder);
        Mockito.when(authorityHolder.isPermissionGrantedForAnyProject(Mockito.eq(Permission.EDIT_PROJECT))).thenReturn(true);

        return new TemplateDataProvider(
                Mockito.mock(SBuildServer.class),
                createRootUrlHolder(),
                Mockito.mock(PermissionChecker.class),
                springContext.getBean(WebHookPayloadManager.class),
                springContext.getBean(WebHookTemplateManager.class),
                springContext.getBean(WebHookUserRequestedExecutor.class),
                springContext.getBean(TemplateFinder.class),
                new MockProjectManager(),
                springContext.getBean(WebHookManager.class),
                Mockito.mock(ProjectIdResolver.class),
                securityContext);
    }

    private WebHookParameterDataProvider createWebHookParameterDataProvider() {
        SecurityContextEx securityContext = Mockito.mock(SecurityContextEx.class);
        AuthorityHolder authorityHolder = Mockito.mock(AuthorityHolder.class);
        Mockito.when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder);
        Mockito.when(authorityHolder.isPermissionGrantedForAnyProject(Mockito.eq(Permission.EDIT_PROJECT))).thenReturn(true);

        return new WebHookParameterDataProvider(
                Mockito.mock(SBuildServer.class),
                createRootUrlHolder(),
                Mockito.mock(PermissionChecker.class),
                springContext.getBean(ProjectManager.class),
                Mockito.mock(ProjectIdResolver.class),
                securityContext,
                springContext.getBean(WebHookParameterFinder.class),
                springContext.getBean(WebHookParameterStoreFactory.class).getWebHookParameterStore());
    }

    private WebHookStatisticsDataProvider createWebHookStatisticsDataProvider() {
        SecurityContextEx securityContext = Mockito.mock(SecurityContextEx.class);
        AuthorityHolder authorityHolder = Mockito.mock(AuthorityHolder.class);
        Mockito.when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder);

        StatisticsManager statisticsManager = Mockito.mock(StatisticsManager.class);
        Mockito.when(statisticsManager.getHistoricalStatistics(Mockito.any(), Mockito.any()))
            .thenReturn(Collections.emptyList());

        StatisticsReportAssembler reportAssembler = Mockito.mock(StatisticsReportAssembler.class);
        Mockito.when(reportAssembler.assembleStatisticsReports(Mockito.any(), Mockito.anyList()))
            .thenReturn(new StatisticsReport());

        return new WebHookStatisticsDataProvider(
                Mockito.mock(SBuildServer.class),
                createRootUrlHolder(),
                Mockito.mock(PermissionChecker.class),
                new MockProjectManager(),
                Mockito.mock(ProjectIdResolver.class),
                securityContext,
            statisticsManager,
            reportAssembler);
    }

    private RootUrlHolder createRootUrlHolder() {
        return new RootUrlHolder() {
            private String url = Constants.API_URL;

            @Override
            public void setRootUrl(String rootUrl) {
                this.url = rootUrl;
            }

            @Override
            public String getRootUrl() {
                return url;
            }
        };
    }

    private WebHookPluginDataResolver createPluginDataResolverMock() {
        WebHookPluginDataResolver resolver = Mockito.mock(WebHookPluginDataResolver.class);
        Mockito.when(resolver.getWebHooksCoreVersion()).thenReturn("test-core-version");
        Mockito.when(resolver.getWebHooksRestApiVersion()).thenReturn("test-rest-version");
        return resolver;
    }

    private <T> T getSafely(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private <T> void bindIfPresent(T value, Class<T> contractClass) {
        if (value != null) {
            bind(value).to(contractClass);
        }
    }
}
