package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameter;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameters;

public class WebHookParametersMutationRequestTest extends WebHookAbstractSpringAwareJerseyTest {

    private static final String ROOT_PROJECT = "_Root";

    @Test
    public void testCreateAndDeleteParameterJson() {
        String name = "j2-param-" + System.nanoTime();

        int beforeCount = getParameterCount(ROOT_PROJECT);

        ProjectWebhookParameter parameter = new ProjectWebhookParameter();
        parameter.setName(name);
        parameter.setSecure(true);
        parameter.setIncludedInLegacyPayloads(true);
        parameter.setForceResolveTeamCityVariable(true);
        parameter.setValue("Some value");

        Response createResponse = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(parameter, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, createResponse);

        ProjectWebhookParameter created = createResponse.readEntity(ProjectWebhookParameter.class);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(name, created.getName());

        ProjectWebhookParameters afterCreate = listParameters(ROOT_PROJECT);
        assertEquals(beforeCount + 1, (int) afterCreate.getCount());
        assertTrue(containsParameter(afterCreate, created.getId(), name));

        Response deleteResponse = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT + "/id:" + created.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete();
        assertTrue(deleteResponse.getStatus() == 200 || deleteResponse.getStatus() == 204);

        ProjectWebhookParameters afterDelete = listParameters(ROOT_PROJECT);
        assertEquals(beforeCount, (int) afterDelete.getCount());
        assertFalse(containsParameter(afterDelete, created.getId(), name));
    }

    @Test
    public void testCreateDuplicateParameterNameReturns400() {
        String name = "j2-dup-param-" + System.nanoTime();

        ProjectWebhookParameter parameter = new ProjectWebhookParameter();
        parameter.setName(name);
        parameter.setSecure(false);
        parameter.setIncludedInLegacyPayloads(false);
        parameter.setForceResolveTeamCityVariable(false);
        parameter.setValue("v1");

        Response firstCreate = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(parameter, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, firstCreate);
        ProjectWebhookParameter created = firstCreate.readEntity(ProjectWebhookParameter.class);

        ProjectWebhookParameter duplicate = new ProjectWebhookParameter();
        duplicate.setName(name);
        duplicate.setValue("v2");

        Response duplicateCreate = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(duplicate, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(400, duplicateCreate.getStatus());

        Response cleanupDelete = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT + "/id:" + created.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete();
        assertTrue(cleanupDelete.getStatus() == 200 || cleanupDelete.getStatus() == 204);
    }

    @Test
    public void testCreateAndDeleteParameterXml() {
        String name = "j2-param-xml-" + System.nanoTime();

        int beforeCount = listParameters(ROOT_PROJECT, MediaType.APPLICATION_XML_TYPE).getCount();

        ProjectWebhookParameter parameter = new ProjectWebhookParameter();
        parameter.setName(name);
        parameter.setSecure(true);
        parameter.setIncludedInLegacyPayloads(true);
        parameter.setForceResolveTeamCityVariable(true);
        parameter.setValue("Some xml value");

        Response createResponse = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT)
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.entity(parameter, MediaType.APPLICATION_XML_TYPE));
        assertHttpStatus(Status.OK, createResponse);

        ProjectWebhookParameter created = createResponse.readEntity(ProjectWebhookParameter.class);
        assertNotNull(created);
        assertNotNull(created.getId());

        ProjectWebhookParameters afterCreate = listParameters(ROOT_PROJECT, MediaType.APPLICATION_XML_TYPE);
        assertEquals(beforeCount + 1, (int) afterCreate.getCount());

        Response deleteResponse = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT + "/id:" + created.getId())
                .request(MediaType.APPLICATION_XML_TYPE)
                .delete();
        assertTrue(deleteResponse.getStatus() == 200 || deleteResponse.getStatus() == 204);

        ProjectWebhookParameters afterDelete = listParameters(ROOT_PROJECT, MediaType.APPLICATION_XML_TYPE);
        assertEquals(beforeCount, (int) afterDelete.getCount());
        assertFalse(containsParameter(afterDelete, created.getId(), name));
    }

    @Test
    public void testUpdateParameterValueJson() {
        String name = "j2-param-update-" + System.nanoTime();

        ProjectWebhookParameter parameter = new ProjectWebhookParameter();
        parameter.setName(name);
        parameter.setSecure(false);
        parameter.setIncludedInLegacyPayloads(true);
        parameter.setForceResolveTeamCityVariable(false);
        parameter.setValue("before");

        Response createResponse = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(parameter, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, createResponse);
        ProjectWebhookParameter created = createResponse.readEntity(ProjectWebhookParameter.class);
        assertNotNull(created);
        assertNotNull(created.getId());

        ProjectWebhookParameter update = new ProjectWebhookParameter();
        update.setName(name);
        update.setSecure(false);
        update.setIncludedInLegacyPayloads(true);
        update.setForceResolveTeamCityVariable(false);
        update.setValue("after");

        // TODO: Align API semantics so PUT accepts the same id: locator style as GET/DELETE.
        Response updateResponse = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT + "/" + created.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(update, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, updateResponse);
        ProjectWebhookParameter updated = updateResponse.readEntity(ProjectWebhookParameter.class);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(name, updated.getName());

        ProjectWebhookParameters afterUpdateList = listParameters(ROOT_PROJECT);
        ProjectWebhookParameter persisted = findParameterById(afterUpdateList, created.getId());
        assertNotNull(persisted);
        assertEquals("after", persisted.getValue());

        Response cleanupDelete = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + ROOT_PROJECT + "/id:" + created.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete();
        assertTrue(cleanupDelete.getStatus() == 200 || cleanupDelete.getStatus() == 204);
    }

    private int getParameterCount(String projectExternalId) {
        return listParameters(projectExternalId).getCount();
    }

    private ProjectWebhookParameters listParameters(String projectExternalId) {
        return listParameters(projectExternalId, MediaType.APPLICATION_JSON_TYPE);
    }

    private ProjectWebhookParameters listParameters(String projectExternalId, MediaType mediaType) {
        Response response = target(WebHookParametersRequest.API_PARAMETERS_URL + "/" + projectExternalId)
                .queryParam("fields", "$long")
                .request(mediaType)
                .get();
        assertHttpStatus(Status.OK, response);

        ProjectWebhookParameters parameters = response.readEntity(ProjectWebhookParameters.class);
        assertNotNull(parameters);
        assertNotNull(parameters.getParameters());
        return parameters;
    }

    private boolean containsParameter(ProjectWebhookParameters parameters, String id, String name) {
        for (ProjectWebhookParameter parameter : parameters.getParameters()) {
            if (parameter == null) {
                continue;
            }
            if (id != null && id.equals(parameter.getId())) {
                return true;
            }
            if (name != null && name.equals(parameter.getName())) {
                return true;
            }
        }
        return false;
    }

    private ProjectWebhookParameter findParameterById(ProjectWebhookParameters parameters, String id) {
        for (ProjectWebhookParameter parameter : parameters.getParameters()) {
            if (parameter != null && id != null && id.equals(parameter.getId())) {
                return parameter;
            }
        }
        return null;
    }

}

