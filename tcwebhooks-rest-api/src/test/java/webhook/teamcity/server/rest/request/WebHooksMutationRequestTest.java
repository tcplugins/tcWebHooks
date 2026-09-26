package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;
import org.springframework.web.context.ContextLoader;

import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameter;
import webhook.teamcity.server.rest.model.webhook.ProjectWebHookFilter;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhook;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class WebHooksMutationRequestTest extends WebHookAbstractSpringAwareJerseyTest {

    private static final String PROJECT_EXTERNAL_ID = "TestProject";

    @Test
    public void testUpdateParametersAddsNewParameter() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        ProjectWebhook webhook = getSingleWebhook(PROJECT_EXTERNAL_ID);
        assertNotNull(webhook.getParameters());
        assertEquals(2, (int) webhook.getParameters().getCount());

        ProjectWebhookParameter newParameter = new ProjectWebhookParameter();
        newParameter.setName("myNewName");
        newParameter.setValue("myNewValue");
        webhook.getParameters().getParameters().add(newParameter);

        Response putResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + PROJECT_EXTERNAL_ID + "/id:" + webhook.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(webhook, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, putResponse);

        ProjectWebhook updated = putResponse.readEntity(ProjectWebhook.class);
        assertNotNull(updated);
        assertNotNull(updated.getParameters());
        assertEquals(3, (int) updated.getParameters().getCount());
    }

    @Test
    public void testDeleteParameterByReplacingWebhook() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        ProjectWebhook webhook = getSingleWebhook(PROJECT_EXTERNAL_ID);
        assertNotNull(webhook.getParameters());
        assertEquals(2, (int) webhook.getParameters().getCount());

        webhook.getParameters().getParameters().remove(1);

        Response putResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + PROJECT_EXTERNAL_ID + "/id:" + webhook.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(webhook, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, putResponse);

        ProjectWebhook updated = putResponse.readEntity(ProjectWebhook.class);
        assertNotNull(updated);
        assertNotNull(updated.getParameters());
        assertEquals(1, (int) updated.getParameters().getCount());
    }

    @Test
    public void testDeleteAllParametersByReplacingWebhook() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        ProjectWebhook webhook = getSingleWebhook(PROJECT_EXTERNAL_ID);
        assertNotNull(webhook.getParameters());
        assertEquals(2, (int) webhook.getParameters().getCount());

        webhook.getParameters().setParameters(new ArrayList<ProjectWebhookParameter>());

        Response putResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + PROJECT_EXTERNAL_ID + "/id:" + webhook.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(webhook, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, putResponse);

        ProjectWebhook updated = putResponse.readEntity(ProjectWebhook.class);
        assertNotNull(updated);
        assertNotNull(updated.getParameters());
        assertEquals(0, (int) updated.getParameters().getCount());
    }

    @Test
    public void testUpdateFiltersAddsNewFilter() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        ProjectWebhook webhook = getSingleWebhook(PROJECT_EXTERNAL_ID);
        assertNotNull(webhook.getFilters());
        assertEquals(2, (int) webhook.getFilters().getCount());

        ProjectWebHookFilter newFilter = new ProjectWebHookFilter();
        newFilter.setEnabled(true);
        newFilter.setRegex(".*.*");
        newFilter.setValue("yes");
        webhook.getFilters().getFilters().add(newFilter);

        Response putResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + PROJECT_EXTERNAL_ID + "/id:" + webhook.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(webhook, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, putResponse);

        ProjectWebhook updated = putResponse.readEntity(ProjectWebhook.class);
        assertNotNull(updated);
        assertNotNull(updated.getFilters());
        assertEquals(3, (int) updated.getFilters().getCount());
    }

    @Test
    public void testAddTwoFiltersToEmptyWebhook() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-bearer-auth.xml");

        ProjectWebhook webhook = getSingleWebhook(PROJECT_EXTERNAL_ID);
        assertNotNull(webhook.getFilters());
        assertEquals(0, (int) webhook.getFilters().getCount());

        ProjectWebHookFilter first = new ProjectWebHookFilter();
        first.setEnabled(true);
        first.setRegex(".*.*");
        first.setValue("yes");

        ProjectWebHookFilter second = new ProjectWebHookFilter();
        second.setEnabled(true);
        second.setRegex(".*.*");
        second.setValue("yes");

        webhook.getFilters().getFilters().add(first);
        webhook.getFilters().getFilters().add(second);

        Response putResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + PROJECT_EXTERNAL_ID + "/id:" + webhook.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(webhook, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, putResponse);

        ProjectWebhook updated = putResponse.readEntity(ProjectWebhook.class);
        assertNotNull(updated);
        assertNotNull(updated.getFilters());
        assertEquals(2, (int) updated.getFilters().getCount());
    }

    @Test
    public void testDeleteFilterByReplacingWebhook() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        ProjectWebhook webhook = getSingleWebhook(PROJECT_EXTERNAL_ID);
        assertNotNull(webhook.getFilters());
        assertEquals(2, (int) webhook.getFilters().getCount());

        webhook.getFilters().getFilters().remove(1);

        Response putResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + PROJECT_EXTERNAL_ID + "/id:" + webhook.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(webhook, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, putResponse);

        ProjectWebhook updated = putResponse.readEntity(ProjectWebhook.class);
        assertNotNull(updated);
        assertNotNull(updated.getFilters());
        assertEquals(1, (int) updated.getFilters().getCount());
    }

    @Test
    public void testDeleteAllFiltersByReplacingWebhook() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        ProjectWebhook webhook = getSingleWebhook(PROJECT_EXTERNAL_ID);
        assertNotNull(webhook.getFilters());
        assertEquals(2, (int) webhook.getFilters().getCount());

        webhook.getFilters().setFilters(new ArrayList<ProjectWebHookFilter>());

        Response putResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + PROJECT_EXTERNAL_ID + "/id:" + webhook.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(webhook, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, putResponse);

        ProjectWebhook updated = putResponse.readEntity(ProjectWebhook.class);
        assertNotNull(updated);
        assertNotNull(updated.getFilters());
        assertEquals(0, (int) updated.getFilters().getCount());
    }

    @Test
    public void testUpdateFiltersMalformedReturns422() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        ProjectWebhook webhook = getSingleWebhook(PROJECT_EXTERNAL_ID);
        assertNotNull(webhook.getFilters());
        assertEquals(2, (int) webhook.getFilters().getCount());

        ProjectWebHookFilter malformedFilter = new ProjectWebHookFilter();
        malformedFilter.setEnabled(true);
        malformedFilter.setRegex(".*.*");
        webhook.getFilters().getFilters().add(malformedFilter);

        Response putResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + PROJECT_EXTERNAL_ID + "/id:" + webhook.getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(webhook, MediaType.APPLICATION_JSON_TYPE));
        assertEquals(422, putResponse.getStatus());
    }

    private ProjectWebhook getSingleWebhook(String projectExternalId) {
        Response listResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + projectExternalId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, listResponse);

        ProjectWebhooks listed = listResponse.readEntity(ProjectWebhooks.class);
        assertNotNull(listed);
        assertNotNull(listed.getWebhooks());
        assertEquals(1, listed.getWebhooks().size());

        Response singleResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/" + projectExternalId + "/id:" + listed.getWebhooks().get(0).getId())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, singleResponse);
        return singleResponse.readEntity(ProjectWebhook.class);
    }

    private void loadProjectSettingsFromConfig(String configPath) throws JAXBException, IOException, JDOMException {
        SortedMap<String, String> map = new TreeMap<>();
        ExtraParameters extraParameters = new ExtraParameters(map);

        WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
        framework.loadWebHookProjectSettingsFromConfigXml(new File(configPath));

        Element webhooksParent = new Element("webhooks");
        framework.getWebHookProjectSettings().writeTo(webhooksParent);

        ProjectSettingsManager projectSettingsManager = ContextLoader.getCurrentWebApplicationContext()
                .getBean(ProjectSettingsManager.class);
        projectSettingsManager.readFrom(webhooksParent, PROJECT_EXTERNAL_ID);
    }
}

