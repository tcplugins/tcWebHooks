package webhook.teamcity.server.rest.request;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.web.context.ContextLoaderListener;

import com.riffpie.common.testing.AbstractSpringAwareJerseyTest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;

/**
 * Base for unit-integration testing of REST web services based on Jersey (v. 1.9).
 *
 * What it does:
 * <ul>
 *  <li>Configures Jersey logging to log detailed messages into /tmp/kodee-jersey-test.log
 *  <li>Starts Jersey with detailed request matching logs returned to the caller in the
 *    response headers (see {@link ClientResponse#getHeaders()})
 * </ul>
 *
 * <h3>Usage</h3>
 * Typically you would use something like this:
 * <pre><code>{@code
 * ClientResponse resp = resource().path("users").path(userId)
        .type(MediaType.APPLICATION_JSON)
        .delete(ClientResponse.class);
 * }</code></pre>
 *
 * @see #assertHttpStatus(Status, ClientResponse)
 * @see JerseyTest#resource()
 */
public abstract class AbstractJerseyTest extends AbstractSpringAwareJerseyTest {

    // not really important as long as resource() is used instead of constructing the URL manually
    protected static final String CONTEXT_PATH = "testing";

    /** The package containing the Jersey Resources (or semicolon-separated packages). */
    protected static final String REST_SERVICES_PACKAGE = "webhook.teamcity.test.jerseyprovider:webhook.teamcity.server.rest.request:webhook.teamcity.server.rest.model:webhook.teamcity.settings";

    private static int port;

    @BeforeClass
    public static void initDetailedJerseyLoggingIntoFile() throws Exception {
        String tmpFileDirectory = System.getProperty("java.io.tmpdir");
        File logFile = new File(tmpFileDirectory, "my-jersey-test.log");
        Logger.getLogger("").addHandler(new FileHandler(logFile.getPath()));
        Logger.getLogger("com.sun.jersey").setLevel(Level.FINEST);
        System.out.println(AbstractJerseyTest.class.getSimpleName() + ": Jersey logs in " + logFile);
    }
    
	public AbstractJerseyTest() {
		super(new WebAppDescriptor.Builder("webhook.teamcity.test.jerseyprovider",  "webhook.teamcity.server.rest.request", "webhook.teamcity.server.rest.model", "webhook.teamcity.settings")
        .contextPath(CONTEXT_PATH)
        .contextParam("contextConfigLocation", "classpath:/TestSpringContext.xml")
        .contextListenerClass(ContextLoaderListener.class)
        .initParam("com.sun.jersey.config.feature.Trace", "true")
        .initParam("com.sun.jersey.api.json.POJOMappingFeature", "true")
        .build());
		
		
        System.out.println("FYI: The Jersey Resources are available at " +
                UriBuilder.fromUri(getBaseURI()).path(CONTEXT_PATH).build().toASCIIString());
	}
    

    /**
     * Make sure that {@link JerseyTest#setUp()} is called by preventing
     * subclasses from overriding it without callin it.
     */
    @Before
    @Override
    public final void setUp() throws Exception {
        super.setUp();
    }

    /**
     * To do: Find a free port to be used for the server to avoid possible
     * conflicts. The port class field must be set before the constructor is called.
     * <p>
     * Ex.: <code>{@code new ServerSocket(0).getLocalPort(); // and close it...; perhaps call setReuseAddress(true)}</code>
     *
     * @see #getPort(int)
     * @see <a href="http://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java">StackOverflow: Sockets: Discover port availability using Java</a>
     */
    @BeforeClass
    public static void findFreePort() {
        port = 9998; // Use some cleverness to detect a free port ...
    }

    /**
     * Override to force JerseyTest to use the port we want instead of the default one.
     */
    @Override
    protected final int getPort(int defaultPort) {
        return port;
    }

    /**
     * Verify that the response's status is as expected, provide useful error message if not, including
     * the response body (that might carry some details of the failure).
     * @param expectedStatus (required) What status you expected; example: {@link Status#OK}
     * @param actualResponse (required) Response from <code>{@code resource().path("exampleResource").get(ClientResponse.class);}</code>
     */
    public static void assertHttpStatus(final Status expectedStatus, final ClientResponse actualResponse) {
        int actualStatus = actualResponse.getStatus();
        if (actualStatus != expectedStatus.getStatusCode()) {
            String responseBody = actualResponse.getEntity(String.class);
            fail("Expected status " + expectedStatus.getStatusCode() + " but got " + actualStatus
                    + "; response body: '" + responseBody + "'" + "\nheaders:" + actualResponse.getHeaders());
        }

    }
}