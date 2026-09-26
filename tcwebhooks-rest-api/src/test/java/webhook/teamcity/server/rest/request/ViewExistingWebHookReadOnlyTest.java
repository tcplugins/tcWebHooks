package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

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
import webhook.teamcity.server.rest.model.webhook.ProjectWebHookFilters;
import webhook.teamcity.server.rest.model.webhook.ProjectWebHookParameters;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhook;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class ViewExistingWebHookReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testShortJsonWebHooksRequestHidesAuthentication() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml");

        Response response = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
                .queryParam("fields", "$short")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        ProjectWebhooks projectWebhooks = response.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());

        ProjectWebhook firstWebhook = projectWebhooks.getWebhooks().get(0);
        assertNotNull(firstWebhook.getUrl());
        assertNull(firstWebhook.getAuthentication());
    }

    @Test
    public void testJsonWebHooksRequestIncludesFiltersAndParameters() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        Response response = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        ProjectWebhooks projectWebhooks = response.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());

        ProjectWebhook firstWebhook = projectWebhooks.getWebhooks().get(0);
        assertNotNull(firstWebhook.getParameters());
        assertNotNull(firstWebhook.getFilters());
    }

    @Test
    public void testShortJsonWebHooksRequestHidesAuthenticationFiltersAndParameters() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        Response response = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
                .queryParam("fields", "$short")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        ProjectWebhooks projectWebhooks = response.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());

        ProjectWebhook firstWebhook = projectWebhooks.getWebhooks().get(0);
        assertNotNull(firstWebhook.getUrl());
        assertNull(firstWebhook.getAuthentication());
        assertNull(firstWebhook.getParameters());
        assertNull(firstWebhook.getFilters());
    }

        @Test
        public void testWebHookByIdEndpointRespondsForLoadedConfig() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        Response listResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, listResponse);
        ProjectWebhooks projectWebhooks = listResponse.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());

        ProjectWebhook listed = projectWebhooks.getWebhooks().get(0);
        assertNotNull(listed.getId());

        Response singleResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject/id:" + listed.getId())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, singleResponse);

        ProjectWebhook single = singleResponse.readEntity(ProjectWebhook.class);
        assertNotNull(single);
        assertEquals(listed.getId(), single.getId());
        assertNotNull(single.getUrl());
        }

        @Test
        public void testWebHookChildParametersAndFiltersEndpointsRespond() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        Response listResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, listResponse);
        ProjectWebhooks projectWebhooks = listResponse.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());

        String webhookId = projectWebhooks.getWebhooks().get(0).getId();
        assertNotNull(webhookId);

        Response parametersResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject/id:" + webhookId + "/parameters")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, parametersResponse);
        ProjectWebHookParameters parameters = parametersResponse.readEntity(ProjectWebHookParameters.class);
        assertNotNull(parameters);
        assertNotNull(parameters.getParameters());
        assertEquals(parameters.getParameters().size(), (int) parameters.getCount());

        Response filtersResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject/id:" + webhookId + "/filters")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, filtersResponse);
        ProjectWebHookFilters filters = filtersResponse.readEntity(ProjectWebHookFilters.class);
        assertNotNull(filters);
        assertNotNull(filters.getFilters());
        assertEquals(filters.getFilters().size(), (int) filters.getCount());
        }

        @Test
        public void testWebHookSingleFilterEndpointResponds() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        Response listResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, listResponse);
        ProjectWebhooks projectWebhooks = listResponse.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());

        String webhookId = projectWebhooks.getWebhooks().get(0).getId();
        assertNotNull(webhookId);

        Response filterResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject/id:" + webhookId + "/filters/id:1")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, filterResponse);

        ProjectWebHookFilter filter = filterResponse.readEntity(ProjectWebHookFilter.class);
        assertNotNull(filter);
        assertEquals(Integer.valueOf(1), filter.getId());
        assertNotNull(filter.getHref());
        }

        @Test
        public void testWebHookSingleParameterEndpointResponds() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        Response listResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, listResponse);
        ProjectWebhooks projectWebhooks = listResponse.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());

        String webhookId = projectWebhooks.getWebhooks().get(0).getId();
        assertNotNull(webhookId);

        Response parameterResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject/id:" + webhookId + "/parameters/id:1")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, parameterResponse);

        ProjectWebhookParameter parameter = parameterResponse.readEntity(ProjectWebhookParameter.class);
        assertNotNull(parameter);
        assertEquals("1", parameter.getId());
        assertNotNull(parameter.getName());
        assertNotNull(parameter.getHref());
        }

    @Test
    public void testWebHooksSearchEndpointRespondsWithProjectLocator() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        Response response = target(WebHooksRequest.API_WEBHOOKS_URL)
                .queryParam("locator", "project:testProject")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        ProjectWebhooks projectWebhooks = response.readEntity(ProjectWebhooks.class);
        assertNotNull(projectWebhooks);
        assertNotNull(projectWebhooks.getWebhooks());
        assertEquals(projectWebhooks.getWebhooks().size(), (int) projectWebhooks.getCount());
        if (!projectWebhooks.getWebhooks().isEmpty()) {
            assertNotNull(projectWebhooks.getWebhooks().get(0).getId());
        }
    }

    @Test
    public void testWebHooksSearchEndpointRespondsWithIdLocator() throws JAXBException, IOException, JDOMException {
        loadProjectSettingsFromConfig("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml");

        Response listResponse = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, listResponse);
        ProjectWebhooks listed = listResponse.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) listed.getCount());

        String webhookId = listed.getWebhooks().get(0).getId();
        assertNotNull(webhookId);

        Response searchResponse = target(WebHooksRequest.API_WEBHOOKS_URL)
                .queryParam("locator", "id:" + webhookId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, searchResponse);

        ProjectWebhooks found = searchResponse.readEntity(ProjectWebhooks.class);
        assertNotNull(found);
        assertNotNull(found.getWebhooks());
        assertEquals(found.getWebhooks().size(), (int) found.getCount());
        assertTrue(found.getCount() >= 0);
        if (!found.getWebhooks().isEmpty()) {
            assertEquals(webhookId, found.getWebhooks().get(0).getId());
        }
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
        projectSettingsManager.readFrom(webhooksParent, "testProject");
    }
}

