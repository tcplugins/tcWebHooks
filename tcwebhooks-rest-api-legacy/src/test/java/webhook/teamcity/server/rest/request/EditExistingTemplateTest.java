package webhook.teamcity.server.rest.request;

import static org.junit.Assert.*;
import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.ElasticSearchWebHookTemplate;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.server.rest.model.template.NewTemplateDescription;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Templates;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

import com.sun.jersey.api.client.WebResource;

public class EditExistingTemplateTest extends WebHookAbstractSpringAwareJerseyTest {
	
	@Autowired 
	WebHookTemplateManager webHookTemplateManager;
	
	@Autowired 
	WebHookPayloadManager webHookPayloadManager;
	
	@Autowired
	WebHookTemplateJaxHelper webHookTemplateJaxHelper;

    @Test
    public void testXmlTemplatesRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_XML_TYPE).get(Templates.class);
        assertTrue(responseMsg.count == 0);
    }
    
    @Test
    public void testJsonTemplatesRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
        assertTrue(responseMsg.count == 0);
    }
    
    @Test
    public void testJsonTemplatesRequestUsingRegisteredTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebResource webResource = resource();
    	WebHookTemplates templatesList =  webHookTemplateJaxHelper.read("../tcwebhooks-core/src/test/resources/webhook-templates.xml");
    	WebHookTemplateEntity templateEntity = templatesList.getWebHookTemplateList().get(0);
    	webHookTemplateManager.registerTemplateFormatFromXmlConfig(templateEntity);
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	//assertEquals(templateEntity.getDefaultTemplate().getTemplateContent(), responseMsg.);
    	assertEquals(1, (int)responseMsg.count);
    	assertEquals(1, responseMsg.getTemplates().size());
    	assertEquals(1, responseMsg.getTemplates().get(0).getTemplates().size());
    	//assertEquals(1, responseMsg.getTemplates().get(0).getTemplates().get(0).);
    }

    @Test
    public void testJsonTemplatesRequestUsingLotsOfRegisteredTemplates() throws FileNotFoundException, JAXBException {
     	
    	WebResource webResource = resource();
    	WebHookTemplates templatesList =  webHookTemplateJaxHelper.read("../tcwebhooks-core/src/test/resources/webhook-templates.xml");
    	for (WebHookTemplateEntity templateEntity : templatesList.getWebHookTemplateList()){
    		webHookTemplateManager.registerTemplateFormatFromXmlConfig(templateEntity);
    	}
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	//assertEquals(templateEntity.getDefaultTemplate().getTemplateContent(), responseMsg.);
    	assertEquals(3, (int)responseMsg.count);
    	assertEquals(3, responseMsg.getTemplates().size());
    	assertEquals(1, responseMsg.getTemplates().get(0).getTemplates().size());
    }    
    
    @Test
    public void testJsonTemplatesRequestUsingLotsOfRegisteredTemplatesButOnlyReturnignOne() throws FileNotFoundException, JAXBException {
    	
    	WebResource webResource = resource();
    	WebHookTemplates templatesList =  webHookTemplateJaxHelper.read("../tcwebhooks-core/src/test/resources/webhook-templates.xml");
    	assertEquals("There should be 3 templates laoded from file", 3, templatesList.getWebHookTemplateList().size());
    	
    	for (WebHookTemplateEntity templateEntity : templatesList.getWebHookTemplateList()){
    		webHookTemplateManager.registerTemplateFormatFromXmlConfig(templateEntity);
    	}
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:testXMLtemplate").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	//assertEquals(templateEntity.getDefaultTemplate().getTemplateContent(), responseMsg.);
    	//assertEquals(1, (int)responseMsg.count);
    	assertEquals(1, responseMsg.getTemplates().size());
    	assertEquals("testXMLtemplate", responseMsg.id);
    	//assertEquals(1, responseMsg.getTemplates().get(0).getTemplates().size());
    }    
    
    @Test
    public void testJsonTemplatesRequestUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebResource webResource = resource();
    	WebHookTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	elastic.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	//assertEquals(templateEntity.getDefaultTemplate().getTemplateContent(), responseMsg.);
    	//assertEquals(1, (int)responseMsg.count);
    	assertEquals(1, responseMsg.getTemplates().size());
    	assertEquals("elasticsearch", responseMsg.id);
    	//assertEquals(1, responseMsg.getTemplates().get(0).getTemplates().size());
    }  
    
    @Test
    public void testJsonRequestAndUpdate() {
    	WebResource webResource = resource();
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(responseMsg.count == 0);
    	
    	NewTemplateDescription newTemplateDescription = new NewTemplateDescription();
    	newTemplateDescription.description = "A test template";
    	newTemplateDescription.name = "testTemplateFromUnitTest";
    	

    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplateDescription);
    	Templates updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(updatedResponse.count == 1);

    }
}