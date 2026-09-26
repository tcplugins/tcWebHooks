package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.payload.template.FlowdockXmlWebHookTemplate;
import webhook.teamcity.payload.template.SlackComCompactXmlWebHookTemplate;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.Templates;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItem;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateState;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class TemplateRequestReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testTemplatesEndpointRespondsJson() {
        Response response = target(TemplateRequest.API_TEMPLATES_URL).request(MediaType.APPLICATION_JSON_TYPE).get();
        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testTemplatesEndpointRespondsXml() {
        Response response = target(TemplateRequest.API_TEMPLATES_URL).request(MediaType.APPLICATION_XML_TYPE).get();
        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testTemplateEndpointRespondsLongForElasticTemplate() {
        registerElasticTemplate();

        Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch")
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        Template template = response.readEntity(Template.class);
        assertNotNull(template);
        assertEquals("elasticsearch", template.id);
        assertTrue(TemplateState.PROVIDED.toString().equals(template.status)
            || TemplateState.USER_OVERRIDDEN.toString().equals(template.status));
        assertNotNull(template.getTemplates());
        assertEquals(1, template.getTemplates().size());
    }

    @Test
    public void testTemplateEndpointRespondsShortForElasticTemplate() {
        registerElasticTemplate();

        Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch")
                .queryParam("fields", "$short,templateItem")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        Template template = response.readEntity(Template.class);
        assertNotNull(template);
        assertEquals("elasticsearch", template.id);
        assertNotNull(template.getTemplates());
        assertEquals(1, template.getTemplates().size());
    }

    @Test
    public void testTemplateRawConfigEndpointRespondsXmlForElasticTemplate() {
        registerElasticTemplate();

        Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/rawConfig")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        WebHookTemplateEntity rawConfig = response.readEntity(WebHookTemplateEntity.class);
        assertNotNull(rawConfig);
        assertEquals("elasticsearch", rawConfig.getId());
    }

    @Test
    public void testTemplateFullConfigEndpointRespondsJsonForElasticTemplate() {
        registerElasticTemplate();

        Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/fullConfig")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        WebHookTemplateConfig fullConfig = response.readEntity(WebHookTemplateConfig.class);
        assertNotNull(fullConfig);
        assertEquals("elasticsearch", fullConfig.getId());
        assertNotNull(fullConfig.getFormat());
    }

    @Test
    public void testTemplateExportEndpointRespondsWithAttachmentHeader() {
        registerElasticTemplate();

        Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:elasticsearch/export")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        String contentDisposition = response.getHeaderString("Content-Disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment"));
        assertTrue(contentDisposition.contains("elasticsearch.json"));

        String body = response.readEntity(String.class);
        assertNotNull(body);
        assertFalse(body.isEmpty());
    }

    @Test
    public void testTemplateEndpointRespondsForSlackCompactJsonAndXml() {
        WebHookPayloadTemplate slackCompact = registerSlackCompactTemplate();

        Response jsonResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, jsonResponse);
        Template jsonTemplate = jsonResponse.readEntity(Template.class);
        assertNotNull(jsonTemplate);
        assertEquals("slack.com-compact", jsonTemplate.id);
        assertNotNull(jsonTemplate.getTemplates());
        assertTrue(jsonTemplate.getTemplates().size() > 0);

        Response xmlResponse = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertHttpStatus(Status.OK, xmlResponse);
        Template xmlTemplate = xmlResponse.readEntity(Template.class);
        assertNotNull(xmlTemplate);
        assertEquals("slack.com-compact", xmlTemplate.id);
        assertNotNull(xmlTemplate.getTemplates());
        assertTrue(xmlTemplate.getTemplates().size() > 0);
    }

    @Test
    public void testTemplateFullConfigEndpointRespondsForSlackCompactXml() {
        WebHookPayloadTemplate slackCompact = registerSlackCompactTemplate();

        Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:slack.com-compact/fullConfig")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        WebHookTemplateConfig fullConfig = response.readEntity(WebHookTemplateConfig.class);
        assertNotNull(fullConfig);
        assertEquals("slack.com-compact", fullConfig.getId());
        assertNotNull(fullConfig.getTemplates());
        assertTrue(fullConfig.getTemplates().getTemplates().size() > 0);
    }

    @Test
    public void testTemplateBuildStateEndpointRespondsForFlowdockTemplate() {
        WebHookPayloadTemplate flowdock = registerFlowdockTemplate();

        for (WebHookTemplateItem item : flowdock.getAsEntity().getTemplates().getTemplates()) {
            for (BuildStateEnum state : BuildStateEnum.getNotifyStates()) {
                Response response = target(TemplateRequest.API_TEMPLATES_URL
                        + "/id:" + flowdock.getTemplateId()
                        + "/templateItems/id:" + item.getId()
                        + "/buildStates/" + state.getShortName())
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get();
                assertHttpStatus(Status.OK, response);

                WebHookTemplateStateRest stateRest = response.readEntity(WebHookTemplateStateRest.class);
                assertNotNull(stateRest);
                for (WebHookTemplateState templateState : item.getStates()) {
                    if (templateState.getType().equals(state.getShortName())) {
                        assertEquals(templateState.isEnabled(), stateRest.isEnabled());
                        assertEquals(templateState.getType(), stateRest.getType());
                    }
                }
            }
        }
    }

    @Test
    public void testTemplatesEndpointRespondsWithSingleRegisteredXmlTemplate() throws FileNotFoundException, JAXBException {
        Set<String> baselineTemplateIds = getTemplateIds();

        WebHookTemplateManager templateManager = getContext().getBean(WebHookTemplateManager.class);
        WebHookTemplates templatesFromXml = getContext().getBean(WebHookTemplateJaxHelper.class)
                .readTemplates("../tcwebhooks-core/src/test/resources/webhook-templates.xml");

        assertEquals(3, templatesFromXml.getWebHookTemplateList().size());
        String registeredTemplateId = templatesFromXml.getWebHookTemplateList().get(0).getId();
        boolean isNewTemplateId = !baselineTemplateIds.contains(registeredTemplateId);
        templateManager.registerTemplateFormatFromXmlEntity(templatesFromXml.getWebHookTemplateList().get(0));

        Set<String> actualTemplateIds = getTemplateIds();
        int expectedCount = baselineTemplateIds.size() + (isNewTemplateId ? 1 : 0);
        assertEquals(expectedCount, actualTemplateIds.size());
        assertTrue(actualTemplateIds.contains(registeredTemplateId));
    }

    @Test
    public void testTemplatesEndpointRespondsWithMultipleRegisteredXmlTemplates() throws FileNotFoundException, JAXBException {
        Set<String> baselineTemplateIds = getTemplateIds();

        WebHookTemplateManager templateManager = getContext().getBean(WebHookTemplateManager.class);
        WebHookTemplates templatesFromXml = getContext().getBean(WebHookTemplateJaxHelper.class)
                .readTemplates("../tcwebhooks-core/src/test/resources/webhook-templates.xml");

        assertEquals(3, templatesFromXml.getWebHookTemplateList().size());
        Set<String> xmlTemplateIds = new HashSet<>();
        for (WebHookTemplateEntity templateEntity : templatesFromXml.getWebHookTemplateList()) {
            xmlTemplateIds.add(templateEntity.getId());
            templateManager.registerTemplateFormatFromXmlEntity(templateEntity);
        }

        Set<String> newTemplateIds = new HashSet<>(xmlTemplateIds);
        newTemplateIds.removeAll(baselineTemplateIds);

        Set<String> actualTemplateIds = getTemplateIds();
        int expectedCount = baselineTemplateIds.size() + newTemplateIds.size();
        assertEquals(expectedCount, actualTemplateIds.size());
        assertTrue(actualTemplateIds.containsAll(xmlTemplateIds));
    }

    @Test
    public void testTemplateEndpointProvidedStateReturnsErrorForUserTemplate() throws FileNotFoundException, JAXBException {
        WebHookTemplateManager templateManager = getContext().getBean(WebHookTemplateManager.class);
        WebHookTemplates templatesFromXml = getContext().getBean(WebHookTemplateJaxHelper.class)
                .readTemplates("../tcwebhooks-core/src/test/resources/webhook-templates.xml");

        assertEquals(3, templatesFromXml.getWebHookTemplateList().size());
        templateManager.registerTemplateFormatFromXmlEntity(templatesFromXml.getWebHookTemplateList().get(0));

        Response response = target(TemplateRequest.API_TEMPLATES_URL + "/id:testXMLtemplate,status:PROVIDED")
                .queryParam("fields", "$long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertTrue(response.getStatus() == 404);
        String body = response.readEntity(String.class);
        assertNotNull(body);
        assertFalse(body.isEmpty());
    }

    private Set<String> getTemplateIds() {
        Response response = target(TemplateRequest.API_TEMPLATES_URL)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);
        Templates templates = response.readEntity(Templates.class);
        assertNotNull(templates);

        Set<String> ids = new HashSet<>();
        if (templates.getTemplates() != null) {
            for (Template template : templates.getTemplates()) {
                if (template != null && template.id != null) {
                    ids.add(template.id);
                }
            }
        }
        return ids;
    }

    private WebApplicationContext getContext() {
        return ContextLoader.getCurrentWebApplicationContext();
    }

    private void registerElasticTemplate() {
        WebApplicationContext context = getContext();
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

    private WebHookPayloadTemplate registerSlackCompactTemplate() {
        WebApplicationContext context = getContext();
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
        return slackCompact;
    }

    private WebHookPayloadTemplate registerFlowdockTemplate() {
        WebApplicationContext context = getContext();
        WebHookTemplateManager templateManager = context.getBean(WebHookTemplateManager.class);
        WebHookPayloadManager payloadManager = context.getBean(WebHookPayloadManager.class);
        WebHookTemplateJaxHelper templateJaxHelper = context.getBean(WebHookTemplateJaxHelper.class);

        WebHookPayloadTemplate flowdock = new FlowdockXmlWebHookTemplate(
                templateManager,
                payloadManager,
                templateJaxHelper,
                projectIdResolver,
                null);
        flowdock.register();
        return flowdock;
    }
}

