package webhook.teamcity.server.rest.request;

import static org.junit.Assert.*;
import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import javax.ws.rs.core.MediaType;


import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.server.rest.model.template.Templates;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

import org.springframework.beans.factory.annotation.Autowired;

public class CreateNewTemplateTest extends WebHookAbstractSpringAwareJerseyTest {
	
	@Autowired 
	WebHookTemplateManager webHookTemplateManager;
	
	@Autowired 
	WebHookPayloadManager webHookPayloadManager;
	
	@Autowired
	WebHookTemplateJaxHelper webHookTemplateJaxHelper;

	WebResource webResource;
	
	@Before 
	public void setup(){
    	webResource = resource();
    	webResource.addFilter(new LoggingFilter(System.out));
	}

    @Test
    public void testRequestTemplatesUsingXmlWithNoneConfiguredReturnsZeroTemplates() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_XML_TYPE).get(Templates.class);
        assertEquals("Templates count should be zero", 0, (int)responseMsg.count );
    }
    
    @Test
    public void testRequestTemplatesUsingJsonWithNoneConfiguredReturnsZeroTemplates() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
        assertTrue(responseMsg.count == 0);
    }
    
    @Test
    public void testCreateNewMinimalTemplateUsingJsonReturnsCreatedTemplate() {
    	WebResource webResource = resource();
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(responseMsg.count == 0);
    	
    	Template newTemplate = new Template();
    	newTemplate.description = "A test template";
    	newTemplate.id = "testTemplateFromUnitTest";
    	newTemplate.format = "jsontemplate";
    	newTemplate.rank = 500;

    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplate);
    	Templates updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(updatedResponse.count == 1);

    }
    
    @Test(expected=com.sun.jersey.api.client.UniformInterfaceException.class)
    public void testCreateAndUpdateUsingJsonFailsForSameTemplateName() {
    	WebResource webResource = resource();
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertEquals("Templates count should be zero", 0, (int)responseMsg.count);
    	
    	Template newTemplate = new Template();
    	newTemplate.description = "A test template";
    	newTemplate.id = "testTemplateFromUnitTest";
    	
    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplate);
    	Templates updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertEquals("Templates count should now be one", 1, (int)updatedResponse.count);
    	
    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplate);
    	updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	
    }
    
    @Test
    public void testCreateTemplateItemOnExistingTempalteUsingJson() {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	elastic.register();
    	
    	WebResource webResource = resource();
    	
    	Template templateResponse = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertTrue(templateResponse.getTemplates().size() == 1);

    	
    	TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate").queryParam("fields","$long,templateItem,content").accept(MediaType.APPLICATION_JSON_TYPE).get(TemplateItem.class);
    	prettyPrint(responseMsg);
    	responseMsg.id= "_new";
    	
    	webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItem").accept(MediaType.APPLICATION_JSON_TYPE).post(responseMsg);
    	
    	Template updatedResponse = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertTrue(updatedResponse.getTemplates().size() == 2);
    	
    	prettyPrint(updatedResponse);
    	
    }
    
    @Test(expected=UniformInterfaceException.class)
    public void testCreateDefaultTemplateItemFailsUsingJsonWhenDefaultTemplateAlreadyExists() {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	elastic.register();
    	
    	WebResource webResource = resource();
    	
    	Template templateResponse = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertTrue(templateResponse.getTemplates().size() == 1);
    	
    	
    	TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).get(TemplateItem.class);
    	
    	responseMsg.id= "_new";
    	prettyPrint(responseMsg);
    	
    	webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).post(responseMsg);
    	
    }
    
    @Test
    public void testCreateTemplateByRequestingAnExistingTemplateAndThenSubmittingItWithANewTemplateName() {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	elastic.register();
    	
    	WebResource webResource = resource();
    	
    	Template templateResponse = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").queryParam("fields","$long,templateItem,content").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertTrue(templateResponse.getTemplates().size() == 1);
    	
    	templateResponse.id  ="newElastic";
    	
    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(templateResponse);
    	
    	Template createdTemplateResponse = webResource.path(API_TEMPLATES_URL + "/id:newElastic").queryParam("fields","$long,templateItem,content").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	prettyPrint(createdTemplateResponse);
    	assertTrue(createdTemplateResponse.getTemplates().size() == 1);
    	assertTrue(createdTemplateResponse.defaultTemplate.getTemplateText() != null);
    	assertFalse(createdTemplateResponse.defaultTemplate.getTemplateText().content.isEmpty());
    	assertTrue(createdTemplateResponse.defaultTemplate.getBranchTemplateText() != null);
    	assertFalse(createdTemplateResponse.defaultTemplate.getBranchTemplateText().content.isEmpty());

    }
}