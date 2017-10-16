package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.template.SlackComCompactXmlWebHookTemplate;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.Templates;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

public class EditExistingTemplateTest extends WebHookAbstractSpringAwareJerseyTest {
	
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
    public void testUpdateJsonTemplatesRequestUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	slackCompact.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItem/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplate,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	
    	assertEquals("slack.com-compact", responseMsg.parentTemplate.getName());
    	prettyPrint(responseMsg);
    	
    	responseMsg.findConfigForBuildState("beforeBuildFinish").setEnabled(true);
		Template.TemplateItem responseMsg2 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItem/id:1").accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).put(Template.TemplateItem.class, responseMsg);
		prettyPrint(responseMsg2);
		
    	Template.TemplateItem responseMsg3 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItem/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplateName,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	assertEquals(true, responseMsg3.findConfigForBuildState("beforeBuildFinish").isEnabled());
    	prettyPrint(responseMsg3);
    }
    
    @Test(expected=UniformInterfaceException.class)
    public void testUpdateJsonTemplateTemplateItemByEnablingInvalidBuildStateRequestUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	slackCompact.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItem/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplate,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	
    	assertEquals("slack.com-compact", responseMsg.parentTemplate.getName());
    	prettyPrint(responseMsg);
    	
    	responseMsg.findConfigForBuildState("buildFailed").setEnabled(true);
    	try {
    		webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItem/id:1").accept(MediaType.APPLICATION_JSON_TYPE).put(Template.TemplateItem.class, responseMsg);
    	} catch (UniformInterfaceException e) {
    		assertEquals("Client response status: 422", e.getMessage());
    		throw e;
		}
    }
    
    @Test
    public void testCreateDefaultTemplateUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	slackCompact.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItem/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplate,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	
    	assertEquals("slack.com-compact", responseMsg.parentTemplate.getName());
    	prettyPrint(responseMsg);
    	
    	responseMsg.id= "_new";
    	responseMsg.setStates(new ArrayList<WebHookTemplateStateRest>());
    	
		Template.TemplateItem responseMsg2 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).post(Template.TemplateItem.class, responseMsg);
		prettyPrint(responseMsg2);
		
    }
    
}