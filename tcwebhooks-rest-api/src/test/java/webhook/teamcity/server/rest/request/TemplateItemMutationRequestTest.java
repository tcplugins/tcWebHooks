package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.payload.template.SlackComCompactXmlWebHookTemplate;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class TemplateItemMutationRequestTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testUpdateTemplateItemBuildStateToggleUsingSlackCompactTemplate() {
        registerSlackCompactTemplate();

        String existingItemId = findFirstTemplateItemId("slack.com-compact");
        assertNotNull(existingItemId);

        Response itemResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + existingItemId)
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, itemResponse);
        Template.TemplateItem item = itemResponse.readEntity(Template.TemplateItem.class);
        assertNotNull(item);
        assertEquals(existingItemId, item.getId());
        assertNotNull(item.getTemplateText());
        assertNotNull(item.getTemplateText().getUseTemplateTextForBranch());
        ensureTemplateTextIsValid(item);

        item.findConfigForBuildState("buildSuccessful").setEnabled(false);

        Response firstUpdateResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + existingItemId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(item, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, firstUpdateResponse);
        Template.TemplateItem firstUpdated = firstUpdateResponse.readEntity(Template.TemplateItem.class);
        assertFalse(firstUpdated.findConfigForBuildState("buildSuccessful").isEnabled());

        Response refreshedResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + existingItemId)
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, refreshedResponse);
        Template.TemplateItem refreshed = refreshedResponse.readEntity(Template.TemplateItem.class);
        assertNotNull(refreshed.getTemplateText());
        assertNotNull(refreshed.getTemplateText().getUseTemplateTextForBranch());
        ensureTemplateTextIsValid(refreshed);

        refreshed.findConfigForBuildState("buildSuccessful").setEnabled(true);

        Response secondUpdateResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + existingItemId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(refreshed, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, secondUpdateResponse);
        Template.TemplateItem secondUpdated = secondUpdateResponse.readEntity(Template.TemplateItem.class);
        assertTrue(secondUpdated.findConfigForBuildState("buildSuccessful").isEnabled());
    }

    @Test
    public void testUpdateTemplateItemEnablingInvalidBuildStateReturns422() {
        registerSlackCompactTemplate();

                String existingItemId = findFirstTemplateItemId("slack.com-compact");
                assertNotNull(existingItemId);

                Response itemResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + existingItemId)
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, itemResponse);
        Template.TemplateItem item = itemResponse.readEntity(Template.TemplateItem.class);
        assertNotNull(item);
        assertNotNull(item.getTemplateText());
        assertNotNull(item.getTemplateText().getUseTemplateTextForBranch());
        ensureTemplateTextIsValid(item);

        item.findConfigForBuildState("buildFailed").setEnabled(true);

        Response updateResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + existingItemId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(item, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(422, updateResponse.getStatus());
        String body = updateResponse.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testCreateDefaultTemplateItemForUserTemplate() {
        String templateId = "j2-item-default-" + System.nanoTime();

        Response createTemplateResponse = target(TemplateRequest.API_TEMPLATES_URL + "/_Root")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(buildMinimalTemplate(templateId), MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, createTemplateResponse);

        registerSlackCompactTemplate();

        String sourceItemId = findFirstTemplateItemId("slack.com-compact");
        assertNotNull(sourceItemId);

        Response sourceItemResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + sourceItemId)
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, sourceItemResponse);
        Template.TemplateItem sourceItem = sourceItemResponse.readEntity(Template.TemplateItem.class);
        assertNotNull(sourceItem);
        assertNotNull(sourceItem.getTemplateText());
        assertNotNull(sourceItem.getTemplateText().getUseTemplateTextForBranch());
        ensureTemplateTextIsValid(sourceItem);

        sourceItem.setId("_new");
        sourceItem.setBuildStates(new ArrayList<Template.WebHookTemplateStateRest>());

        Response createDefaultResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:" + templateId + "/defaultTemplate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(sourceItem, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(201, createDefaultResponse.getStatus());
        Template.TemplateItem createdDefault = createDefaultResponse.readEntity(Template.TemplateItem.class);
        assertNotNull(createdDefault);
        assertEquals("defaultTemplate", createdDefault.getId());
    }

    @Test
    public void testCreateTemplateItemOnExistingTemplateIncreasesItemCount() {
        registerElasticTemplate();

                String sourceItemId = findFirstTemplateItemId("elasticsearch");
                assertNotNull(sourceItemId);

        Response templateResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, templateResponse);
        Template template = templateResponse.readEntity(Template.class);
        assertNotNull(template);
        int initialCount = template.getTemplates().size();
        assertTrue(initialCount > 0);

        Response defaultItemResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:" + sourceItemId)
                .queryParam("fields", "$long,templateItem,content")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, defaultItemResponse);
        Template.TemplateItem defaultItem = defaultItemResponse.readEntity(Template.TemplateItem.class);
        assertNotNull(defaultItem);
        defaultItem.id = "_new";

        Response createItemResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/templateItem")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(defaultItem, MediaType.APPLICATION_JSON_TYPE));
        int createItemStatus = createItemResponse.getStatus();
        assertTrue(createItemStatus == 200
                || createItemStatus == 201
                || createItemStatus == 204
                || createItemStatus == 400
                || createItemStatus == 404
                || createItemStatus == 409
                || createItemStatus == 422);

        if (createItemStatus >= 400) {
            String body = createItemResponse.readEntity(String.class);
            assertNotNull(body);
            assertFalse(body.isEmpty());
            return;
        }

        Response verifyResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, verifyResponse);
        Template updatedTemplate = verifyResponse.readEntity(Template.class);
        assertNotNull(updatedTemplate);
        assertEquals(initialCount + 1, updatedTemplate.getTemplates().size());
    }

    @Test
    public void testCreateDuplicateDefaultTemplateItemReturnsError() {
        String templateId = "j2-dup-default-" + System.nanoTime();

        Response createTemplateResponse = target(TemplateRequest.API_TEMPLATES_URL + "/_Root")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(buildMinimalTemplate(templateId), MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, createTemplateResponse);

        registerSlackCompactTemplate();

        String sourceItemId = findFirstTemplateItemId("slack.com-compact");
        assertNotNull(sourceItemId);

        Response defaultItemResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + sourceItemId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, defaultItemResponse);
        Template.TemplateItem defaultItem = defaultItemResponse.readEntity(Template.TemplateItem.class);
        assertNotNull(defaultItem);
        defaultItem.id = "_new";
        defaultItem.setBuildStates(new ArrayList<Template.WebHookTemplateStateRest>());

        Response createFirstDefaultResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:" + templateId + "/defaultTemplate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(defaultItem, MediaType.APPLICATION_JSON_TYPE));
        int createFirstDefaultStatus = createFirstDefaultResponse.getStatus();
        assertTrue(createFirstDefaultStatus == 200
                || createFirstDefaultStatus == 201
                || createFirstDefaultStatus == 204
                || createFirstDefaultStatus == 400
                || createFirstDefaultStatus == 404
                || createFirstDefaultStatus == 409
                || createFirstDefaultStatus == 422);

        if (createFirstDefaultStatus >= 400) {
            String body = createFirstDefaultResponse.readEntity(String.class);
            assertNotNull(body);
            assertFalse(body.isEmpty());
            return;
        }

        Response createDefaultResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:" + templateId + "/defaultTemplate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(defaultItem, MediaType.APPLICATION_JSON_TYPE));
        int duplicateCreateStatus = createDefaultResponse.getStatus();
        assertTrue(duplicateCreateStatus == 400
                || duplicateCreateStatus == 409
                || duplicateCreateStatus == 422);

        String body = createDefaultResponse.readEntity(String.class);
        assertNotNull(body);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testDeleteTemplateItemRemovesItemAndReducesCount() {
        registerSlackCompactTemplate();

                String itemIdToDelete = findFirstTemplateItemId("slack.com-compact");
                assertNotNull(itemIdToDelete);

        Response templateResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, templateResponse);
        Template beforeDelete = templateResponse.readEntity(Template.class);
        assertNotNull(beforeDelete);
        int beforeCount = beforeDelete.getTemplates().size();
        assertTrue(beforeCount > 1);

        Response deleteResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + itemIdToDelete)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete();
        assertTrue(deleteResponse.getStatus() == 200 || deleteResponse.getStatus() == 204);

        Response fetchDeletedResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + itemIdToDelete)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertTrue(fetchDeletedResponse.getStatus() == 404);

        Response verifyResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, verifyResponse);
        Template afterDelete = verifyResponse.readEntity(Template.class);
        assertNotNull(afterDelete);
        assertEquals(beforeCount - 1, afterDelete.getTemplates().size());
    }

    @Test
    public void testDeleteDefaultTemplateItemMakesItUnavailable() {
        String templateId = "j2-del-default-" + System.nanoTime();

        Response createTemplateResponse = target(TemplateRequest.API_TEMPLATES_URL + "/_Root")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(buildMinimalTemplate(templateId), MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Status.OK, createTemplateResponse);

        registerSlackCompactTemplate();

        String sourceItemId = findFirstTemplateItemId("slack.com-compact");
        assertNotNull(sourceItemId);

        Response sourceItemResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:" + sourceItemId)
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, sourceItemResponse);
        Template.TemplateItem sourceItem = sourceItemResponse.readEntity(Template.TemplateItem.class);
        assertNotNull(sourceItem);
        sourceItem.setId("_new");
        sourceItem.setBuildStates(new ArrayList<Template.WebHookTemplateStateRest>());

        Response createDefaultResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:" + templateId + "/defaultTemplate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(sourceItem, MediaType.APPLICATION_JSON_TYPE));
        int createDefaultStatus = createDefaultResponse.getStatus();
        assertTrue(createDefaultStatus == 200
                || createDefaultStatus == 201
                || createDefaultStatus == 204
                || createDefaultStatus == 400
                || createDefaultStatus == 404
                || createDefaultStatus == 409
                || createDefaultStatus == 422);

        if (createDefaultStatus >= 400) {
            String body = createDefaultResponse.readEntity(String.class);
            assertNotNull(body);
            assertFalse(body.isEmpty());
            return;
        }

        Response existingResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:" + templateId + "/templateItems/id:defaultTemplate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, existingResponse);

        Response deleteResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:" + templateId + "/templateItems/id:defaultTemplate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .delete();
        assertTrue(deleteResponse.getStatus() == 200 || deleteResponse.getStatus() == 204);

        Response fetchDeletedResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:" + templateId + "/templateItems/id:defaultTemplate")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertTrue(fetchDeletedResponse.getStatus() == 404);
    }

    private Template buildMinimalTemplate(String id) {
        Template t = new Template();
        t.id = id;
        t.description = "template-" + id;
        t.format = "jsontemplate";
        t.rank = 500;
        return t;
    }

    private void registerSlackCompactTemplate() {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        WebHookTemplateManager templateManager = context.getBean(WebHookTemplateManager.class);
        WebHookPayloadManager payloadManager = context.getBean(WebHookPayloadManager.class);
        WebHookTemplateJaxHelper templateJaxHelper = context.getBean(WebHookTemplateJaxHelper.class);

        WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(
                templateManager,
                payloadManager,
                templateJaxHelper,
                projectIdResolver,
                null);
        slackCompact.register();
    }

        private void registerElasticTemplate() {
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

        private void ensureTemplateTextIsValid(Template.TemplateItem item) {
                if (item.getTemplateText().getContent() == null || item.getTemplateText().getContent().trim().isEmpty()) {
                        item.getTemplateText().setContent("template-content");
                }
                if (item.getTemplateText().getUseTemplateTextForBranch() == null) {
                        item.getTemplateText().setUseTemplateTextForBranch(true);
                }
                if (!item.getTemplateText().getUseTemplateTextForBranch()
                                && item.getBranchTemplateText() != null
                                && (item.getBranchTemplateText().getContent() == null || item.getBranchTemplateText().getContent().trim().isEmpty())) {
                        item.getBranchTemplateText().setContent("branch-template-content");
                }
        }
}

