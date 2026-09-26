package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

public class WebHooksIndexRequestBootstrapTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testJersey2ContainerBootstraps() {
        // Smoke check: prove Jersey + HK2 container starts.
        Response response = target("/phase1-bootstrap-health").request(MediaType.APPLICATION_JSON_TYPE).get();
        assertHttpStatus(Status.NOT_FOUND, response);
    }

    @Test
    public void testIndexEndpointResponds() {
        Response response = target(Constants.API_URL).request(MediaType.APPLICATION_JSON_TYPE).get();
        assertHttpStatus(Status.OK, response);
        String responseBody = response.readEntity(String.class);
        assertTrue(responseBody.contains("configurations"));
        assertTrue(responseBody.contains("test-core-version"));
        assertTrue(responseBody.contains("test-rest-version"));
    }

    @Test
    public void testIndexEndpointTypedResponseContainsExpectedLinks() {
        Response response = target(Constants.API_URL).request(MediaType.APPLICATION_JSON_TYPE).get();
        assertHttpStatus(Status.OK, response);

        WebHooksIndexRequest.WebHooksIndexItems index = response.readEntity(WebHooksIndexRequest.WebHooksIndexItems.class);
        assertNotNull(index);
        assertEquals("test-core-version", index.getTcWebHooksVersion());
        assertEquals("test-rest-version", index.getTcWebHooksRestApiVersion());

        assertNotNull(index.getConfigurations());
        assertNotNull(index.getParameters());
        assertNotNull(index.getTemplates());
        assertTrue(index.getConfigurations().getHref().contains("/configurations"));
        assertTrue(index.getParameters().getHref().contains("/parameters"));
        assertTrue(index.getTemplates().getHref().contains("/templates"));
    }

    @Test
    public void testIndexEndpointRespondsXmlWithExpectedSections() {
        Response response = target(Constants.API_URL).request(MediaType.APPLICATION_XML_TYPE).get();
        assertHttpStatus(Status.OK, response);
        String responseBody = response.readEntity(String.class);
        assertTrue(responseBody.contains("configurations"));
        assertTrue(responseBody.contains("parameters"));
        assertTrue(responseBody.contains("templates"));
        assertTrue(responseBody.contains("test-core-version"));
        assertTrue(responseBody.contains("test-rest-version"));
    }
}

