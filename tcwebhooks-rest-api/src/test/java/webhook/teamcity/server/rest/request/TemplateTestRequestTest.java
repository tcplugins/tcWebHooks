package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.springframework.web.context.ContextLoader;

import webhook.WebHookExecutionStats;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.server.rest.model.template.TemplateTestExecutionRequest;
import webhook.teamcity.server.rest.model.template.TemplateTestHistoryItem;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;
import webhook.teamcity.testing.model.WebHookRenderResult;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public class TemplateTestRequestTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testExecuteTemplateRequestRejectsInvalidPayload() {
        TemplateTestExecutionRequest invalid = new TemplateTestExecutionRequest(
                null,
                "",
                "a test branch template",
                false,
                "12345",
                "project01",
                "http://localhost/somewhere",
                null,
                "buildStarted");

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/execute")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(javax.ws.rs.client.Entity.entity(invalid, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(422, response.getStatus());
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testPreviewTemplateRequestRejectsInvalidPayload() {
        TemplateTestExecutionRequest invalid = new TemplateTestExecutionRequest(
                null,
                "",
                "a test branch template",
                false,
                "12345",
                "project01",
                "http://localhost/somewhere",
                null,
                "buildStarted");

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/preview")
                .request(MediaType.TEXT_HTML_TYPE)
                .post(javax.ws.rs.client.Entity.entity(invalid, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(422, response.getStatus());
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testPreviewTemplateRequestRespondsForValidPayload() {
        WebHookUserRequestedExecutor executor = ContextLoader.getCurrentWebApplicationContext()
                .getBean(WebHookUserRequestedExecutor.class);
        when(executor.requestWebHookPreview(any(WebHookTemplateExecutionRequest.class)))
                .thenReturn(new WebHookRenderResult("<html>preview-ok</html>", "jsontemplate"));

        TemplateTestExecutionRequest valid = validRequest();

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/preview")
                .request(MediaType.TEXT_HTML_TYPE)
                .post(javax.ws.rs.client.Entity.entity(valid, MediaType.APPLICATION_JSON_TYPE));

        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertTrue(body.contains("preview-ok"));
    }

    @Test
    public void testPreviewTemplateRequestRespondsNotAcceptableWhenRenderErrored() {
        WebHookUserRequestedExecutor executor = ContextLoader.getCurrentWebApplicationContext()
                .getBean(WebHookUserRequestedExecutor.class);
        when(executor.requestWebHookPreview(any(WebHookTemplateExecutionRequest.class)))
                .thenReturn(new WebHookRenderResult("<html>preview-error</html>", new RuntimeException("render failed")));

        TemplateTestExecutionRequest valid = validRequest();

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/preview")
                .request(MediaType.TEXT_HTML_TYPE)
                .post(javax.ws.rs.client.Entity.entity(valid, MediaType.APPLICATION_JSON_TYPE));

        assertHttpStatus(Status.NOT_ACCEPTABLE, response);
        assertEquals("true", response.getHeaderString("x-tcwehooks-template-rendering-errored"));
        String body = response.readEntity(String.class);
        assertTrue(body.contains("preview-error"));
    }

    @Test
    public void testExecuteTemplateRequestRespondsForValidPayload() {
        WebHookUserRequestedExecutor executor = ContextLoader.getCurrentWebApplicationContext()
                .getBean(WebHookUserRequestedExecutor.class);

        WebHookExecutionStats stats = new WebHookExecutionStats("http://localhost/somewhere");
        stats.setRequestStarting();
        stats.setRequestCompleted(200, "OK");
        stats.setTeardownCompleted();

        WebHookHistoryItem historyItem = new WebHookHistoryItem(
                "projectId",
                "projectName",
                "buildTypeId",
                "buildTypeName",
                "buildTypeExternalId",
                12345L,
                null,
                stats,
                null,
                LocalDateTime.now(),
                null,
                true);

        when(executor.requestWebHookExecution(any(WebHookTemplateExecutionRequest.class))).thenReturn(historyItem);

        TemplateTestExecutionRequest valid = validRequest();

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/execute")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(javax.ws.rs.client.Entity.entity(valid, MediaType.APPLICATION_JSON_TYPE));

        assertHttpStatus(Status.OK, response);
        TemplateTestHistoryItem item = response.readEntity(TemplateTestHistoryItem.class);
        assertNotNull(item);
        assertEquals(200, item.getStatusCode());
        assertEquals("OK", item.getStatusReason());
        assertNotNull(item.getTrackingId());
    }

    @Test
    public void testExecuteTemplateRequestRespondsForWebhookIdWithoutUrl() {
        WebHookUserRequestedExecutor executor = ContextLoader.getCurrentWebApplicationContext()
                .getBean(WebHookUserRequestedExecutor.class);

        WebHookExecutionStats stats = new WebHookExecutionStats("http://localhost/somewhere");
        stats.setRequestStarting();
        stats.setRequestCompleted(202, "Accepted");
        stats.setTeardownCompleted();

        WebHookHistoryItem historyItem = new WebHookHistoryItem(
                "projectId",
                "projectName",
                "buildTypeId",
                "buildTypeName",
                "buildTypeExternalId",
                12345L,
                null,
                stats,
                null,
                LocalDateTime.now(),
                null,
                true);

        when(executor.requestWebHookExecution(any(WebHookTemplateExecutionRequest.class))).thenReturn(historyItem);

        TemplateTestExecutionRequest valid = new TemplateTestExecutionRequest(
                "jsontemplate",
                "a test main template",
                "a test branch template",
                false,
                "12345",
                "project01",
                null,
                "webhook01",
                "buildStarted");

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/execute")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(javax.ws.rs.client.Entity.entity(valid, MediaType.APPLICATION_JSON_TYPE));

        assertHttpStatus(Status.OK, response);
        TemplateTestHistoryItem item = response.readEntity(TemplateTestHistoryItem.class);
        assertNotNull(item);
        assertEquals(202, item.getStatusCode());
        assertEquals("Accepted", item.getStatusReason());
    }

    @Test
    public void testExecuteTemplateRequestRespondsForValidXmlPayload() {
        WebHookUserRequestedExecutor executor = ContextLoader.getCurrentWebApplicationContext()
                .getBean(WebHookUserRequestedExecutor.class);

        WebHookExecutionStats stats = new WebHookExecutionStats("http://localhost/somewhere");
        stats.setRequestStarting();
        stats.setRequestCompleted(201, "Created");
        stats.setTeardownCompleted();

        WebHookHistoryItem historyItem = new WebHookHistoryItem(
                "projectId",
                "projectName",
                "buildTypeId",
                "buildTypeName",
                "buildTypeExternalId",
                12345L,
                null,
                stats,
                null,
                LocalDateTime.now(),
                null,
                true);

        when(executor.requestWebHookExecution(any(WebHookTemplateExecutionRequest.class))).thenReturn(historyItem);

        TemplateTestExecutionRequest valid = validRequest();

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/execute")
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(javax.ws.rs.client.Entity.entity(valid, MediaType.APPLICATION_XML_TYPE));

        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testExecuteTemplateRequestRejectsInvalidXmlPayload() {
        TemplateTestExecutionRequest invalid = new TemplateTestExecutionRequest(
                null,
                "",
                "a test branch template",
                false,
                "12345",
                "project01",
                "http://localhost/somewhere",
                null,
                "buildStarted");

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/execute")
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(javax.ws.rs.client.Entity.entity(invalid, MediaType.APPLICATION_XML_TYPE));

        assertEquals(422, response.getStatus());
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testPreviewTemplateRequestRespondsForValidXmlPayload() {
        WebHookUserRequestedExecutor executor = ContextLoader.getCurrentWebApplicationContext()
                .getBean(WebHookUserRequestedExecutor.class);
        when(executor.requestWebHookPreview(any(WebHookTemplateExecutionRequest.class)))
                .thenReturn(new WebHookRenderResult("<html>preview-xml-ok</html>", "jsontemplate"));

        TemplateTestExecutionRequest valid = validRequest();

        Response response = target(TemplateTestRequest.API_TEMPLATE_TEST_URL + "/preview")
                .request(MediaType.TEXT_HTML_TYPE)
                .post(javax.ws.rs.client.Entity.entity(valid, MediaType.APPLICATION_XML_TYPE));

        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertTrue(body.contains("preview-xml-ok"));
    }

    private TemplateTestExecutionRequest validRequest() {
        return new TemplateTestExecutionRequest(
                "jsontemplate",
                "a test main template",
                "a test branch template",
                false,
                "12345",
                "project01",
                "http://localhost/somewhere",
                null,
                "buildStarted");
    }
}

