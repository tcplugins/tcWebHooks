package webhook.teamcity.server.rest.request;

import static org.junit.Assert.fail;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.springframework.web.context.ContextLoaderListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.riffpie.common.testing.AbstractSpringAwareJerseyTest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

import webhook.teamcity.ProjectIdResolver;

public class WebHookAbstractSpringAwareJerseyTest extends AbstractSpringAwareJerseyTest {
	
	ProjectIdResolver projectIdResolver;
	
	public WebHookAbstractSpringAwareJerseyTest() {
		super(new WebAppDescriptor.Builder("webhook.teamcity.test.jerseyprovider",  "webhook.teamcity.server.rest.errors", "webhook.teamcity.server.rest.request", "webhook.teamcity.server.rest.model", "webhook.teamcity.settings")
        .contextPath("testing")
        .contextParam("contextConfigLocation", "classpath:/TestSpringContext.xml")
        .contextListenerClass(ContextLoaderListener.class)
        .build());
	}
	
	public void prettyPrint(Object responseMsg){
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	System.out.println(gson.toJson(responseMsg));
	}
	
    /**
     * Make sure that {@link JerseyTest#setUp()} is called by preventing
     * subclasses from overriding it without callin it.
     */
    @Before
    @Override
    public final void setUp() throws Exception {
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
