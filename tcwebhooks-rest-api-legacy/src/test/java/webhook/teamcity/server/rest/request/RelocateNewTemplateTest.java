package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.ElasticSearchXmlWebHookTemplate;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class RelocateNewTemplateTest extends WebHookAbstractSpringAwareJerseyTest {
	
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
    public void testCreateTemplateByRequestingAnExistingTemplateAndThenSubmittingItWithANewTemplateNameTheRelocatingIt() {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	WebResource webResource = resource();
    	
    	Template templateResponse = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").queryParam("fields","**").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertTrue(templateResponse.getTemplates().size() == 1);
    	
    	templateResponse.id  ="newElastic";
    	
    	webResource.path(API_TEMPLATES_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).post(templateResponse);
    	
    	Template createdTemplateResponse = webResource.path(API_TEMPLATES_URL + "/id:newElastic").queryParam("fields","**").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	prettyPrint(createdTemplateResponse);
    	assertTrue(createdTemplateResponse.getTemplates().size() == 1);
    	assertTrue(createdTemplateResponse.defaultTemplate.getTemplateText() != null);
    	assertFalse(createdTemplateResponse.defaultTemplate.getTemplateText().content.isEmpty());
    	assertTrue(createdTemplateResponse.defaultTemplate.getBranchTemplateText() != null);
    	assertFalse(createdTemplateResponse.defaultTemplate.getBranchTemplateText().content.isEmpty());

    	createdTemplateResponse.projectId = "_Root";
    	webResource.path(API_TEMPLATES_URL + "/id:newElastic").accept(MediaType.APPLICATION_JSON_TYPE).put(Template.class, createdTemplateResponse);
    	
    	Template relocatedTemplateResponse = webResource.path(API_TEMPLATES_URL + "/id:newElastic").queryParam("fields","**").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	prettyPrint(relocatedTemplateResponse);
    	assertEquals("_Root", relocatedTemplateResponse.projectId);
    	
    }
}