package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
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
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplate,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	
    	assertEquals("slack.com-compact", responseMsg.parentTemplate.getId());
    	
    	prettyPrint(responseMsg);
    	
    	// templateItem "id:1" is for buildFixed and buildSuccessful
    	// disable buildSuccessful
    	responseMsg.findConfigForBuildState("buildSuccessful").setEnabled(false);
		Template.TemplateItem responseMsg2 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).put(Template.TemplateItem.class, responseMsg);
		
		// check that the response back is false
    	assertFalse("buildSuccessful should now be false for template id:1", responseMsg2.findConfigForBuildState("buildSuccessful").isEnabled());
		prettyPrint(responseMsg2);
		
		// Get it again (and check it again just for an extra check)
		Template.TemplateItem responseMsg3 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplateName,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
		assertFalse("buildSuccessful should now be false for template id:1", responseMsg2.findConfigForBuildState("buildSuccessful").isEnabled());
		
		// Now update the value and PUT it back
		responseMsg3.findConfigForBuildState("buildSuccessful").setEnabled(true);
		Template.TemplateItem responseMsg4 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).put(Template.TemplateItem.class, responseMsg3);

		// Check the response now has it set to true again.
		assertTrue("buildSuccessful should now be false for template id:1", responseMsg4.findConfigForBuildState("buildSuccessful").isEnabled());    	
    	prettyPrint(responseMsg4);
    }
    
    @Test(expected=UniformInterfaceException.class)
    public void testUpdateJsonTemplateTemplateItemByEnablingInvalidBuildStateRequestUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplate,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	
    	assertEquals("slack.com-compact", responseMsg.parentTemplate.getId());
    	prettyPrint(responseMsg);
    	
    	responseMsg.findConfigForBuildState("buildFailed").setEnabled(true);
    	try {
    		webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").accept(MediaType.APPLICATION_JSON_TYPE).put(Template.TemplateItem.class, responseMsg);
    	} catch (UniformInterfaceException e) {
    		assertEquals("Client response status: 422", e.getMessage());
    		throw e;
		}
    }
    
    @Test
    public void testCreateDefaultTemplateUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplate,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	
    	assertEquals("slack.com-compact", responseMsg.parentTemplate.getId());
    	prettyPrint(responseMsg);
    	
    	responseMsg.id= "_new";
    	responseMsg.setBuildStates(new ArrayList<WebHookTemplateStateRest>());
    	
		Template.TemplateItem responseMsg2 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).post(Template.TemplateItem.class, responseMsg);
		prettyPrint(responseMsg2);
		
    }
    
    @Test
    public void testEditTemplateUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseMsg.id);
    	assertEquals(TemplateState.PROVIDED.toString(), responseMsg.status);
    	prettyPrint(responseMsg);

    	responseMsg.description = "New Description";
    	responseMsg.rank = 999;
    	responseMsg.preferredDateFormat="YYYY-MM";
    	responseMsg.toolTip = "Woot, a tooltip";
    	responseMsg.projectId = "TestProject";
    	
    	webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).put(Template.class, responseMsg);
    	
    	Template responseAfterEdit = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseAfterEdit.id);
    	assertEquals("New Description", responseAfterEdit.description);
    	assertEquals("YYYY-MM", responseAfterEdit.preferredDateFormat);
    	assertEquals("Woot, a tooltip", responseAfterEdit.toolTip);
    	assertEquals(TemplateState.USER_OVERRIDDEN.toString(), responseAfterEdit.status);

    	prettyPrint(responseAfterEdit);
    }
    
    @Test
    public void testEditTemplateWithMinimalChangesUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseMsg.id);
    	assertEquals(TemplateState.PROVIDED.toString(), responseMsg.status);
    	prettyPrint(responseMsg);
    	
    	responseMsg.description = null;
    	responseMsg.rank = null;
    	responseMsg.preferredDateFormat = null;
    	responseMsg.toolTip = null;
    	webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/patch").accept(MediaType.APPLICATION_JSON_TYPE).post(Template.class, responseMsg);
    	
    	Template responseAfterEdit = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("Slack.com Compact Notification", responseAfterEdit.description);
    	assertEquals("slack.com-compact", responseAfterEdit.id);
    	assertEquals("", responseAfterEdit.preferredDateFormat);
    	assertEquals("POSTs a very compact slack.com notification", responseAfterEdit.toolTip);
    	assertEquals(TemplateState.USER_OVERRIDDEN.toString(), responseAfterEdit.status);

    	prettyPrint(responseAfterEdit);
    }
    
    @Test
    public void testEditTemplateWithMinimalChangesCanRequestOriginalUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseMsg.id);
    	assertEquals(TemplateState.PROVIDED.toString(), responseMsg.status);
    	prettyPrint(responseMsg);
    	
    	responseMsg.description = null;
    	responseMsg.rank = null;
    	responseMsg.preferredDateFormat = null;
    	responseMsg.toolTip = "Something new for a tooltip";
    	webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/patch").accept(MediaType.APPLICATION_JSON_TYPE).post(Template.class, responseMsg);
    	
    	Template responseAfterEdit = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact,status:PROVIDED").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("Slack.com Compact Notification", responseAfterEdit.description);
    	assertEquals("slack.com-compact", responseAfterEdit.id);
    	assertEquals("", responseAfterEdit.preferredDateFormat);
    	assertEquals("POSTs a very compact slack.com notification", responseAfterEdit.toolTip);
    	assertEquals(TemplateState.PROVIDED.toString(), responseAfterEdit.status);
    	
    	prettyPrint(responseAfterEdit);
    }
    
    @Test
    public void testEditTemplateWithMinimalChangesCanRequestOverridenUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseMsg.id);
    	assertEquals(TemplateState.PROVIDED.toString(), responseMsg.status);
    	prettyPrint(responseMsg);
    	
    	responseMsg.description = null;
    	responseMsg.rank = null;
    	responseMsg.preferredDateFormat = null;
    	responseMsg.toolTip = "Something new for a tooltip";
    	webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/patch").accept(MediaType.APPLICATION_JSON_TYPE).post(Template.class, responseMsg);
    	
    	Template responseAfterEdit = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact,status:USER_OVERRIDDEN").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("Slack.com Compact Notification", responseAfterEdit.description);
    	assertEquals("slack.com-compact", responseAfterEdit.id);
    	assertEquals("", responseAfterEdit.preferredDateFormat);
    	assertEquals("Something new for a tooltip", responseAfterEdit.toolTip);
    	assertEquals(TemplateState.USER_OVERRIDDEN.toString(), responseAfterEdit.status);
    	
    	prettyPrint(responseAfterEdit);
    }
    
    @Test
    public void testDefaultTemplateDiff() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/templateItems/id:1").queryParam("fields","id,content,parentTemplateDescription,parentTemplate,editable").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	
    	assertEquals("slack.com-compact", responseMsg.parentTemplate.getId());
    	prettyPrint(responseMsg);
    	
    	responseMsg.id= "_new";
    	responseMsg.setBuildStates(new ArrayList<WebHookTemplateStateRest>());
    	
		Template.TemplateItem responseMsg2 = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).post(Template.TemplateItem.class, responseMsg);
		prettyPrint(responseMsg2);
		
    	String responsePatch = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact,status:PROVIDED/templateItems/id:1/templateContent/diff/id:slack.com-compact,status:USER_OVERRIDDEN/templateItems/id:2/templateContent").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
    	
    	System.out.println("################################");
    	System.out.println(responsePatch);
		
    }
    
    @Test
    public void testEditTemplateFromEmptyTemplateUsingSlackCompactTemplateAndRequestAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseMsg = new Template();
    	responseMsg.id = "slack.com-compact";
    	webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/patch").accept(MediaType.APPLICATION_JSON_TYPE).post(Template.class, responseMsg);
    	
    	Template responseAfterEdit = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("Slack.com Compact Notification", responseAfterEdit.description);
    	assertEquals("slack.com-compact", responseAfterEdit.id);
    	assertEquals("", responseAfterEdit.preferredDateFormat);
    	assertEquals("POSTs a very compact slack.com notification", responseAfterEdit.toolTip);
    	prettyPrint(responseAfterEdit);
    }
    
    @Test(expected=UniformInterfaceException.class)
    public void testChangeExistingTemplateIdCausesExceptionAsJson() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals("slack.com-compact", responseMsg.id);
    	prettyPrint(responseMsg);
    	
    	responseMsg.id = "newDescription";
    	try {
    		webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/patch").accept(MediaType.APPLICATION_JSON_TYPE).post(Response.class, responseMsg);
    	} catch (UniformInterfaceException ex) {
    		assertEquals(422, ex.getResponse().getStatus());
    		throw ex;
    	}
    }
    
}