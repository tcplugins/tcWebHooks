package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameter;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameters;

public class WebHookParametersRequestReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testParametersEndpointRespondsJson() {
        Response response = target(WebHookParametersRequest.API_PARAMETERS_URL + "/_Root")
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testParametersEndpointRespondsXml() {
        Response response = target(WebHookParametersRequest.API_PARAMETERS_URL + "/_Root")
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testParametersEndpointTypedResponseHasExpectedShape() {
        Response response = target(WebHookParametersRequest.API_PARAMETERS_URL + "/_Root")
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        ProjectWebhookParameters parameters = response.readEntity(ProjectWebhookParameters.class);
        assertNotNull(parameters);
        assertNotNull(parameters.getProjectId());
        assertFalse(parameters.getProjectId().trim().isEmpty());
        assertNotNull(parameters.getHref());
        assertFalse(parameters.getHref().trim().isEmpty());
        assertTrue(parameters.getHref().contains("/parameters/" + parameters.getProjectId()));
        assertNotNull(parameters.getParameters());
        assertEquals(parameters.getParameters().size(), (int) parameters.getCount());

        for (ProjectWebhookParameter parameter : parameters.getParameters()) {
            assertNotNull(parameter.getId());
            assertNotNull(parameter.getName());
            assertNotNull(parameter.getHref());
        }
    }

    @Test
    public void testSingleParameterEndpointRespondsForListedParameter() {
        Response listResponse = target(WebHookParametersRequest.API_PARAMETERS_URL + "/_Root")
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, listResponse);

        ProjectWebhookParameters parameters = listResponse.readEntity(ProjectWebhookParameters.class);
        assertNotNull(parameters);
        assertNotNull(parameters.getParameters());

        if (parameters.getParameters().isEmpty()) {
            assertEquals(0, (int) parameters.getCount());
            return;
        }

        ProjectWebhookParameter listed = parameters.getParameters().get(0);
        assertNotNull(listed.getId());
        assertNotNull(parameters.getProjectId());

        Response singleResponse = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + parameters.getProjectId() + "/id:" + listed.getId())
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, singleResponse);

        ProjectWebhookParameter single = singleResponse.readEntity(ProjectWebhookParameter.class);
        assertNotNull(single);
        assertEquals(listed.getId(), single.getId());
        assertEquals(listed.getName(), single.getName());
        assertNotNull(single.getHref());
    }
}

