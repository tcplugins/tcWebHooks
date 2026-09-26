package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import webhook.teamcity.server.rest.model.mainconfig.Webhooks;

public class WebHooksConfigRequestReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testServerConfigEndpointRespondsJson() {
        Response response = target(WebHooksConfigRequest.getUri())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testServerConfigEndpointTypedResponse() {
        Response response = target(WebHooksConfigRequest.getUri())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        Webhooks webhooks = response.readEntity(Webhooks.class);
        assertNotNull(webhooks);
    }
}

