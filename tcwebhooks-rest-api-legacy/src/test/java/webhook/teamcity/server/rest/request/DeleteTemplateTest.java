package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.payload.template.SlackComCompactXmlWebHookTemplate;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Templates;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class DeleteTemplateTest extends WebHookAbstractSpringAwareJerseyTest {
	
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
    public void testDeleteTemplateUsingJson() {
    	
    	WebResource webResource = resource();
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertEquals(0, (int)responseMsg.count);
    	
    	Template newTemplate = new Template();
    	newTemplate.description = "A test template";
    	newTemplate.id = "testTemplateFromUnitTest";
    	newTemplate.format = "jsontemplate";
    	newTemplate.rank = 500;

    	webResource.path(API_TEMPLATES_URL + "/_Root").accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplate);
    	Templates updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertEquals(1, (int)updatedResponse.count);
    	

    	webResource.path(API_TEMPLATES_URL + "/id:testTemplateFromUnitTest").delete();
    	
    	updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertEquals(0, (int)updatedResponse.count);
    	
    	
    }
    @Test
    public void testDeleteTemplateItemUsingJson() {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseTemplate = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	Template.TemplateItem responseTemplateItem = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplate,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	
    	prettyPrint(responseTemplate);
    	
    	assertEquals("slack.com-compact", responseTemplate.id);
    	assertEquals("slack.com-compact", responseTemplateItem.parentTemplate.getId());
    	prettyPrint(responseTemplateItem);
    	
    	assertEquals(5, responseTemplate.getTemplates().size());
    	
    	ClientResponse deleteResponse = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
    	assertEquals(204, deleteResponse.getStatus());
    	
    	ClientResponse getResponse = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
    	// I've not figured out how to invoke the exception handler, so "NotFoundException" returns 500
    	// in Grizzly tests.
    	assertEquals(500, getResponse.getStatus());
    	
    	Template responseTemplate2 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals(4, responseTemplate2.getTemplates().size());
    	
    }
    
    @Test
    public void testDeleteDefaultTemplateItemUsingJson() {
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	WebResource webResource = resource();
    	
    	Template templateResponse = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals(1, templateResponse.getTemplates().size());

    	ClientResponse responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
    	assertEquals(200, responseMsg.getStatus());
    	
    	ClientResponse deleteResponse = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
    	assertEquals(204, deleteResponse.getStatus());
    	
    	ClientResponse getResponse = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
    	// I've not figured out how to invoke the exception handler, so "NotFoundException" returns 500
    	// in Grizzly tests.
    	assertEquals(500, getResponse.getStatus());
    	
    }
    
    

}