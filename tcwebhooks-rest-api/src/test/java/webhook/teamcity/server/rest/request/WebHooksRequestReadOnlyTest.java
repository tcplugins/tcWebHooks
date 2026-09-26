package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertFalse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

public class WebHooksRequestReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testProjectWebHooksEndpointRespondsJson() {
        Response response = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testProjectWebHooksEndpointRespondsXml() {
        Response response = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }
}

