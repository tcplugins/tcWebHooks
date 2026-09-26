package webhook.teamcity.server.rest.request;

import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.test.jerseyprovider.WebHookTestBinder;

public abstract class WebHookAbstractSpringAwareJerseyTest extends JerseyTest {

    protected ProjectIdResolver projectIdResolver;

    private static final String[] JERSEY_CLASSES = new String[] {
            "webhook.teamcity.server.rest.request.StatisticsRequest",
            "webhook.teamcity.server.rest.request.TemplateRequest",
            "webhook.teamcity.server.rest.request.TemplateTestRequest",
            "webhook.teamcity.server.rest.request.WebHookParametersRequest",
            "webhook.teamcity.server.rest.request.WebHooksConfigRequest",
            "webhook.teamcity.server.rest.request.WebHooksIndexRequest",
            "webhook.teamcity.server.rest.request.WebHooksRequest",
            "webhook.teamcity.server.rest.errors.BadRequestExceptionMapper",
            "webhook.teamcity.server.rest.errors.JaxbClassCastExceptionMapper",
            "webhook.teamcity.server.rest.errors.OperationUnsupportedExceptionMapper",
            "webhook.teamcity.server.rest.errors.TemplateInUseExceptionMapper",
            "webhook.teamcity.server.rest.errors.TemplatePermissionExceptionMapper",
            "webhook.teamcity.server.rest.errors.UnprocessableEntityExceptionMapper",
            "webhook.teamcity.server.rest.errors.WebHookPermissionExceptionMapper",
            "webhook.teamcity.server.rest.jersey.HtmlErrorMessageBodyWriter",
                "org.glassfish.jersey.jackson.JacksonFeature",
                "webhook.teamcity.test.jerseyprovider.JacksonContextResolver",
                "webhook.teamcity.test.jerseyprovider.TestNotFoundExceptionMapper",
                "webhook.teamcity.test.jerseyprovider.TestThrowableExceptionMapper"
    };

    private WebApplicationContext springContext;

    @Override
    protected Application configure() {
        WebApplicationContext context = ensureSpringContext();
        ResourceConfig config = new ResourceConfig();
        for (String className : JERSEY_CLASSES) {
            config.register(loadClass(className));
        }
        config.register(new WebHookTestBinder(context));
        return config;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        projectIdResolver = new ProjectIdResolver() {
            @Override
            public String getInternalProjectId(String externalProjectId) {
                return "_Root";
            }

            @Override
            public String getExternalProjectId(String internalProjectId) {
                return "project0";
            }
        };
    }

    @After
    @Override
    public void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            clearSpringContext();
        }
    }

    public void prettyPrint(Object responseMsg) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(responseMsg));
    }

    public static void assertHttpStatus(final Status expectedStatus, final Response actualResponse) {
        int actualStatus = actualResponse.getStatus();
        if (actualStatus != expectedStatus.getStatusCode()) {
            String responseBody = actualResponse.readEntity(String.class);
            fail("Expected status " + expectedStatus.getStatusCode() + " but got " + actualStatus
                    + "; response body: '" + responseBody + "'" + "\nheaders:" + actualResponse.getHeaders());
        }
    }

    private synchronized WebApplicationContext ensureSpringContext() {
        XmlWebApplicationContext webContext = new XmlWebApplicationContext();
        webContext.setServletContext(new MockServletContext());
        webContext.setConfigLocation("classpath:/TestSpringContext.xml");
        webContext.refresh();

        setCurrentWebApplicationContext(webContext);
        springContext = webContext;
        return springContext;
    }

    private synchronized void clearSpringContext() {
        if (springContext instanceof XmlWebApplicationContext) {
            ((XmlWebApplicationContext) springContext).close();
        }
        springContext = null;
        setCurrentWebApplicationContext(null);
    }

    private static void setCurrentWebApplicationContext(WebApplicationContext context) {
        try {
            Method method = ContextLoader.class.getDeclaredMethod("setCurrentWebApplicationContext", WebApplicationContext.class);
            method.setAccessible(true);
            method.invoke(null, context);
            return;
        } catch (NoSuchMethodException ignored) {
            // Fall through to field-based fallback for older/newer Spring variants.
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to initialize Spring test WebApplicationContext", e);
        }

        try {
            Field currentContextField = ContextLoader.class.getDeclaredField("currentContext");
            currentContextField.setAccessible(true);
            currentContextField.set(null, context);

            try {
                Field currentContextPerThreadField = ContextLoader.class.getDeclaredField("currentContextPerThread");
                currentContextPerThreadField.setAccessible(true);
                Object mapObj = currentContextPerThreadField.get(null);
                if (mapObj instanceof Map) {
                    @SuppressWarnings("rawtypes")
                    Map map = (Map) mapObj;
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    if (context == null) {
                        map.remove(classLoader);
                    } else {
                        map.put(classLoader, context);
                    }
                }
            } catch (NoSuchFieldException ignored) {
                // Not present on all Spring versions.
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to initialize Spring test WebApplicationContext", e);
        }
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load Jersey class: " + className, e);
        }
    }
}

