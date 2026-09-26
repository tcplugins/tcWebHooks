package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class TemplateItemRequestReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testTemplateItemContentEndpointResponds() throws Exception {
    WebHookPayloadTemplate elastic = registerElasticTemplate();
    String itemId = findFirstTemplateItemId("elasticsearch");

    String expected = elastic.getTemplateForState(BuildStateEnum.BUILD_FIXED).getTemplateText();

    Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:" + itemId + "/templateContent")
        .request(MediaType.TEXT_PLAIN_TYPE)
        .get();
    assertHttpStatus(Status.OK, response);
    String actual = response.readEntity(String.class);
    assertEquals(expected, actual);
    }

    @Test
    public void testDefaultTemplateContentAndBranchContentEndpointsRespond() throws Exception {
    WebHookPayloadTemplate elastic = registerElasticTemplate();

    Response templateContentResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate/templateContent")
        .request(MediaType.TEXT_PLAIN_TYPE)
        .get();
    if (templateContentResponse.getStatus() == 200) {
        assertEquals(elastic.getTemplateForState(BuildStateEnum.BUILD_STARTED).getTemplateText(),
            templateContentResponse.readEntity(String.class));
    } else {
        assertEquals(404, templateContentResponse.getStatus());
        assertTrue(templateContentResponse.readEntity(String.class).contains("default template"));
    }

    Response branchTemplateContentResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate/branchTemplateContent")
        .request(MediaType.TEXT_PLAIN_TYPE)
        .get();
    if (branchTemplateContentResponse.getStatus() == 200) {
        assertEquals(elastic.getBranchTemplateForState(BuildStateEnum.BUILD_STARTED).getTemplateText(),
            branchTemplateContentResponse.readEntity(String.class));
    } else {
        assertEquals(404, branchTemplateContentResponse.getStatus());
        assertTrue(branchTemplateContentResponse.readEntity(String.class).contains("default template"));
    }
    }

    @Test
    public void testTemplateItemParentFieldsEndpointResponds() throws Exception {
    WebHookPayloadTemplate elastic = registerElasticTemplate();
    String itemId = findFirstTemplateItemId("elasticsearch");

    Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:" + itemId)
        .queryParam("fields", "id,content,parentTemplate")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get();
    assertHttpStatus(Status.OK, response);

    Template.TemplateItem item = response.readEntity(Template.TemplateItem.class);
    assertNotNull(item);
    assertEquals(itemId, item.getId());
    assertNotNull(item.getTemplateText());
    assertNotNull(item.getParentTemplate());
    assertEquals(elastic.getTemplateDescription(), item.getParentTemplate().getDescription());
    assertEquals(elastic.getTemplateId(), item.getParentTemplate().getId());
    }

    @Test
    public void testDefaultTemplateItemEndpointResponds() throws Exception {
    registerElasticTemplate();

    Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get();
    if (response.getStatus() == 200) {
        Template.TemplateItem item = response.readEntity(Template.TemplateItem.class);
        assertNotNull(item);
        assertEquals("defaultTemplate", item.getId());
    } else {
        assertEquals(404, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("default template"));
    }
    }

    @Test
    public void testTemplateItemBuildStateEndpointResponds() throws Exception {
    registerElasticTemplate();
    String itemId = findFirstTemplateItemId("elasticsearch");

    Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:" + itemId + "/buildStates/buildFixed")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get();
    assertHttpStatus(Status.OK, response);

    Template.WebHookTemplateStateRest state = response.readEntity(Template.WebHookTemplateStateRest.class);
    assertNotNull(state);
    assertEquals("buildFixed", state.getType());
    }

    @Test
    public void testDefaultTemplateBuildStateEndpointResponds() throws Exception {
    registerElasticTemplate();

    Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:defaultTemplate/buildStates/buildFixed")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get();
    if (response.getStatus() == 200) {
        Template.WebHookTemplateStateRest state = response.readEntity(Template.WebHookTemplateStateRest.class);
        assertNotNull(state);
        assertEquals("buildFixed", state.getType());
        assertFalse(state.getEditable());
    } else {
        assertEquals(404, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("default template"));
    }
    }

    @Test
    public void testTemplateItemFieldsContentMatchesTemplateContentEndpoint() throws Exception {
    registerElasticTemplate();
    String itemId = findFirstTemplateItemId("elasticsearch");

    Response contentResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:" + itemId + "/templateContent")
        .request(MediaType.TEXT_PLAIN_TYPE)
        .get();
    assertHttpStatus(Status.OK, contentResponse);
    String expectedContent = contentResponse.readEntity(String.class);

    Response itemResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:" + itemId)
        .queryParam("fields", "id,content")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get();
    assertHttpStatus(Status.OK, itemResponse);
    Template.TemplateItem item = itemResponse.readEntity(Template.TemplateItem.class);
    assertNotNull(item);
    assertEquals(itemId, item.getId());
    assertNotNull(item.getTemplateText());
    assertEquals(expectedContent, item.getTemplateText().getContent());
    }

    @Test
    public void testTemplateItemBuildStateEndpointsRespondForAllNotifyStates() throws Exception {
    registerElasticTemplate();
    String itemId = findFirstTemplateItemId("elasticsearch");

    for (BuildStateEnum state : BuildStateEnum.getNotifyStates()) {
        Response response = target(TemplateRequest.API_TEMPLATES_URL
            + "/id:elasticsearch/templateItems/id:" + itemId + "/buildStates/" + state.getShortName())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        assertHttpStatus(Status.OK, response);
        Template.WebHookTemplateStateRest stateRest = response.readEntity(Template.WebHookTemplateStateRest.class);
        assertNotNull(stateRest);
        assertEquals(state.getShortName(), stateRest.getType());
    }
    }

    @Test
    public void testTemplateItemBranchContentEndpointResponds() throws Exception {
    WebHookPayloadTemplate elastic = registerElasticTemplate();
    String itemId = findFirstTemplateItemId("elasticsearch");

    Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:" + itemId + "/branchTemplateContent")
        .request(MediaType.TEXT_PLAIN_TYPE)
        .get();
    assertHttpStatus(Status.OK, response);
    String actual = response.readEntity(String.class);
    assertEquals(elastic.getBranchTemplateForState(BuildStateEnum.BUILD_FIXED).getTemplateText(), actual);
    }

    @Test
    public void testDefaultTemplateBuildStateEndpointsRespondForAllNotifyStates() throws Exception {
    registerElasticTemplate();

    for (BuildStateEnum state : BuildStateEnum.getNotifyStates()) {
        Response response = target(TemplateRequest.API_TEMPLATES_URL
            + "/id:elasticsearch/templateItems/id:defaultTemplate/buildStates/" + state.getShortName())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
        if (response.getStatus() == 200) {
            Template.WebHookTemplateStateRest stateRest = response.readEntity(Template.WebHookTemplateStateRest.class);
            assertNotNull(stateRest);
            assertEquals(state.getShortName(), stateRest.getType());
            assertFalse(stateRest.getEditable());
        } else {
            assertEquals(404, response.getStatus());
            assertTrue(response.readEntity(String.class).contains("default template"));
        }
    }
    }

    private String findFirstTemplateItemId(String templateId) {
    Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:" + templateId)
        .queryParam("fields", "$long")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get();
    assertHttpStatus(Status.OK, response);
    Template template = response.readEntity(Template.class);
    assertNotNull(template);
    assertNotNull(template.getTemplates());
    assertTrue(!template.getTemplates().isEmpty());
    return template.getTemplates().get(0).getId();
    }

    private WebHookPayloadTemplate registerElasticTemplate() {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        WebHookTemplateManager templateManager = context.getBean(WebHookTemplateManager.class);
        WebHookPayloadManager payloadManager = context.getBean(WebHookPayloadManager.class);
        WebHookTemplateJaxHelper templateJaxHelper = context.getBean(WebHookTemplateJaxHelper.class);

        WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(
                templateManager,
                payloadManager,
                templateJaxHelper,
                projectIdResolver,
                null);
        elastic.register();
    return elastic;
    }
}

