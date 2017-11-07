package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    
    @Test
    public void testEditTemplateUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseMsg.id);
    	prettyPrint(responseMsg);

    	responseMsg.description = "New Description";
    	responseMsg.rank = 999;
    	responseMsg.preferredDateFormat="YYYY-MM";
    	responseMsg.toolTip = "Woot, a tooltip";
    	
    	webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).put(Template.class, responseMsg);
    	
    	Template responseAfterEdit = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseAfterEdit.id);
    	assertEquals("New Description", responseAfterEdit.description);
    	assertEquals("YYYY-MM", responseAfterEdit.preferredDateFormat);
    	assertEquals("Woot, a tooltip", responseAfterEdit.toolTip);
    	prettyPrint(responseAfterEdit);
    }
    
    @Test
    public void testEditTemplateWithMinimalChangesUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseMsg.id);
    	prettyPrint(responseMsg);
    	
    	responseMsg.description = null;
    	responseMsg.rank = null;
    	responseMsg.preferredDateFormat = null;
    	responseMsg.toolTip = null;
    	webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).put(Template.class, responseMsg);
    	
    	Template responseAfterEdit = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("Slack.com Compact Notifcation", responseAfterEdit.description);
    	assertEquals("slack.com-compact", responseAfterEdit.id);
    	assertEquals("", responseAfterEdit.preferredDateFormat);
    	assertEquals("POSTs a very compact slack.com notification", responseAfterEdit.toolTip);
    	prettyPrint(responseAfterEdit);
    }
    
    @Test
    public void testEditTemplateFromEmptyTemplateUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	slackCompact.register();
    	
    	Template responseMsg = new Template();
    	responseMsg.id = "slack.com-compact";
    	webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).put(Template.class, responseMsg);
    	
    	Template responseAfterEdit = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("Slack.com Compact Notifcation", responseAfterEdit.description);
    	assertEquals("slack.com-compact", responseAfterEdit.id);
    	assertEquals("", responseAfterEdit.preferredDateFormat);
    	assertEquals("POSTs a very compact slack.com notification", responseAfterEdit.toolTip);
    	prettyPrint(responseAfterEdit);
    }
    
    @Test(expected=UniformInterfaceException.class)
    public void testChangeExistingTemplateNameCausesExceptionAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseMsg.id);
    	prettyPrint(responseMsg);
    	
    	responseMsg.id = "newDescription";
    	try {
    		webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).put(Response.class, responseMsg);
    	} catch (UniformInterfaceException ex) {
    		assertEquals(422, ex.getResponse().getStatus());
    		throw ex;
    	}
    }
    
}