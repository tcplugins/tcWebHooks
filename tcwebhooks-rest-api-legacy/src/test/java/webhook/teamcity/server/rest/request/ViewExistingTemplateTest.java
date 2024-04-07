package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertTrue;
import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import java.io.FileNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

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
import webhook.teamcity.settings.config.builder.WebHookTemplateConfigBuilder;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItem;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateState;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;

public class ViewExistingTemplateTest extends WebHookAbstractSpringAwareJerseyTest {
	
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
    public void testXmlTemplatesRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_XML_TYPE).get(Templates.class);
        assertEquals(0, (int)responseMsg.count);
    }
    
    @Test
    public void testJsonTemplatesRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
        assertEquals(0, (int)responseMsg.count);
    }
    
    @Test
    public void testJsonTemplatesRequestUsingRegisteredTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebResource webResource = resource();
    	WebHookTemplates templatesList =  webHookTemplateJaxHelper.readTemplates("../tcwebhooks-core/src/test/resources/webhook-templates.xml");
    	WebHookTemplateConfig templateEntity = WebHookTemplateConfigBuilder.buildConfig(templatesList.getWebHookTemplateList().get(0));
    	webHookTemplateManager.registerTemplateFormatFromXmlConfig(templateEntity);
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	
    	prettyPrint(responseMsg);
    	
    	assertEquals(1, (int)responseMsg.count);
    	assertEquals(1, responseMsg.getTemplates().size());
    }

    @Test
    public void testJsonTemplatesRequestUsingLotsOfRegisteredTemplates() throws FileNotFoundException, JAXBException {
     	
    	WebResource webResource = resource();
    	WebHookTemplates templatesList =  webHookTemplateJaxHelper.readTemplates("../tcwebhooks-core/src/test/resources/webhook-templates.xml");
    	for (WebHookTemplateEntity templateEntity : templatesList.getWebHookTemplateList()){
    		webHookTemplateManager.registerTemplateFormatFromXmlEntity(templateEntity);
    	}
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertEquals(3, (int)responseMsg.count);
    	assertEquals(3, responseMsg.getTemplates().size());
    	
    	prettyPrint(responseMsg);
    }    
    
    @Test(expected=UniformInterfaceException.class)
    public void testJsonTemplateNotFoundWhenProvidedRequested() throws FileNotFoundException, JAXBException {
    	
    	WebResource webResource = resource();
    	WebHookTemplates templatesList =  webHookTemplateJaxHelper.readTemplates("../tcwebhooks-core/src/test/resources/webhook-templates.xml");
    	assertEquals("There should be 3 templates loaded from file", 3, templatesList.getWebHookTemplateList().size());
    	
    	for (WebHookTemplateEntity templateEntity : templatesList.getWebHookTemplateList()){
    		webHookTemplateManager.registerTemplateFormatFromXmlEntity(templateEntity);
    	}
    	
    	try {
    		webResource.path(API_TEMPLATES_URL + "/id:testXMLtemplate,status:PROVIDED").queryParam("fields","$long").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	} catch (UniformInterfaceException ex) {
        	// I've not figured out how to invoke the exception handler, so "NotFoundException" returns 500
        	// in Grizzly tests.
        	assertEquals(500, ex.getResponse().getStatus());
    		throw ex;
    	}

    }      
    
    @Test
    public void testJsonTemplatesRequestUsingLotsOfRegisteredTemplatesButOnlyReturningOne() throws FileNotFoundException, JAXBException {
    	
    	WebResource webResource = resource();
    	WebHookTemplates templatesList =  webHookTemplateJaxHelper.readTemplates("../tcwebhooks-core/src/test/resources/webhook-templates.xml");
    	assertEquals("There should be 3 templates loaded from file", 3, templatesList.getWebHookTemplateList().size());
    	
    	for (WebHookTemplateEntity templateEntity : templatesList.getWebHookTemplateList()){
    		webHookTemplateManager.registerTemplateFormatFromXmlEntity(templateEntity);
    	}
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:testXMLtemplate").queryParam("fields","$long").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals(1, responseMsg.getTemplates().size());
    	assertEquals("testXMLtemplate", responseMsg.id);
    	
    	prettyPrint(responseMsg);
    }    
    
    @Test
    public void testJsonTemplatesRequestUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals(1, responseMsg.getTemplates().size());
    	assertEquals("elasticsearch", responseMsg.id);
    	assertEquals(TemplateState.PROVIDED.toString(), responseMsg.status);
    	prettyPrint(responseMsg);
    }  
    
    @Test
    public void testJsonTemplatesRequestTemplateContentUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	String responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:1/templateContent").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
    	assertEquals(elastic.getTemplateForState(BuildStateEnum.BUILD_FIXED).getTemplateText(), responseMsg);
    	prettyPrint(responseMsg);
    	
    	responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:1/branchTemplateContent").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
    	assertEquals(elastic.getBranchTemplateForState(BuildStateEnum.BUILD_FIXED).getTemplateText(), responseMsg);
    	prettyPrint(responseMsg);
    }
    
    @Test
    public void testJsonTemplatesRequestDefaultTemplateContentUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	String responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate/templateContent").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
    	assertEquals(elastic.getTemplateForState(BuildStateEnum.BUILD_STARTED).getTemplateText(), responseMsg);
    	prettyPrint(responseMsg);
    	
    	responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate/branchTemplateContent").accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
    	assertEquals(elastic.getBranchTemplateForState(BuildStateEnum.BUILD_STARTED).getTemplateText(), responseMsg);
    	prettyPrint(responseMsg);
    }  
    
    @Test
    public void testJsonTemplatesRequestTemplateItemUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:1").queryParam("fields","id,content").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	for (WebHookTemplateItem templateItem : elastic.getAsEntity().getTemplates().getTemplates()) {
    		if (Integer.valueOf(responseMsg.id) == templateItem.getId()){
    			assertEquals(templateItem.getTemplateText().getTemplateContent(), responseMsg.getTemplateText().content);
    		}
    	}
    	prettyPrint(responseMsg);
    	
    }
    
    @Test
    public void testJsonTemplatesRequestTemplateUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").queryParam("fields","$long").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals(1, responseMsg.getTemplates().size());
    	assertEquals("elasticsearch", responseMsg.id);
    	prettyPrint(responseMsg);
    }
    
    @Test
    public void testJsonTemplatesRequestTemplateUsingElasticTemplateShort() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch").queryParam("fields","$short,templateItem").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	assertEquals(1, responseMsg.getTemplates().size());
    	assertEquals("elasticsearch", responseMsg.id);
    	prettyPrint(responseMsg);
    }  
    
    @Test
    public void testJsonTemplatesRequestTemplateItemForDefaultTemplateUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/defaultTemplate").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class); //.queryParam("fields","id,content,parentTemplateDescription,parentTemplateName")
    	assertEquals("defaultTemplate", responseMsg.getId());
    	prettyPrint(responseMsg);
    	
    }
    
    @Test
    public void testJsonTemplatesRequestTemplateItemWithParentNameAndDescriptionUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	Template.TemplateItem responseMsg = webResource.path(API_TEMPLATES_URL + "/id:elasticsearch/templateItems/id:1").queryParam("fields","id,content,parentTemplate,parentTemplate").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.TemplateItem.class);
    	boolean itemFound=false;
    	for (WebHookTemplateItem templateItem : elastic.getAsEntity().getTemplates().getTemplates()) {
    		if (Integer.valueOf(responseMsg.id) == templateItem.getId()){
    			assertEquals(templateItem.getTemplateText().getTemplateContent(), responseMsg.getTemplateText().content);
    			assertEquals(elastic.getTemplateDescription(), responseMsg.parentTemplate.getDescription());
    			assertEquals(elastic.getTemplateId(), responseMsg.parentTemplate.getId());
    			itemFound = true;
    		}
    	}
    	assertTrue(itemFound);
    	prettyPrint(responseMsg);
    	
    }  
    
    @Test
    public void testJsonTemplatesRequestBuildStateUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	
    	for (WebHookTemplateItem item : elastic.getAsEntity().getTemplates().getTemplates()){
    		for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
    			WebHookTemplateStateRest responseMsg = webResource.path(API_TEMPLATES_URL + 
    																	"/id:" + elastic.getTemplateId() + 
    																	"/templateItems/id:" + item.getId() + 
    																	"/buildStates/" + state.getShortName()
    													   )
    													  .accept(MediaType.APPLICATION_JSON_TYPE)
    													  .get(WebHookTemplateStateRest.class);
    	    	prettyPrint(responseMsg);
    	    	for (WebHookTemplateState templateState: item.getStates()){
    	    		if (templateState.getType().equals(state.getShortName())){
    	    			assertEquals(templateState.isEnabled(), responseMsg.isEnabled());
    	    			assertEquals(templateState.getType(), responseMsg.getType());
    	    		}
    	    	}
    		}
    	}
    }
    
    @Test
    public void testJsonTemplatesRequestDefaultBuildStateUsingElasticTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate elastic = new ElasticSearchXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	elastic.register();
    	
    	
    	for (WebHookTemplateItem item : elastic.getAsEntity().getTemplates().getTemplates()){
    		for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
    			WebHookTemplateStateRest responseMsg = webResource.path(API_TEMPLATES_URL + 
    					"/id:" + elastic.getTemplateId() + 
    					"/templateItems/id:defaultTemplate" + 
    					"/buildStates/" + state.getShortName()
    					)
    					.accept(MediaType.APPLICATION_JSON_TYPE)
    					.get(WebHookTemplateStateRest.class);
    			prettyPrint(responseMsg);
    			for (WebHookTemplateState templateState: item.getStates()){
    				if (templateState.getType().equals(state.getShortName())){
    					assertThat(templateState.isEnabled(), not(equalTo(responseMsg.isEnabled())));
    					assertEquals(templateState.getType(), responseMsg.getType());
    					assertEquals(false, responseMsg.getEditable());
    				}
    			}
    		}
    	}
    }
    
	@Test
	public void testJsonTemplatesRequestBuildStateUsingFlowdockTemplate() throws FileNotFoundException, JAXBException {
		
		WebHookPayloadTemplate flowdock = new FlowdockXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
		flowdock.register();
		
		
		for (WebHookTemplateItem item : flowdock.getAsEntity().getTemplates().getTemplates()){
			for (BuildStateEnum state : BuildStateEnum.getNotifyStates()){
				WebHookTemplateStateRest responseMsg = webResource.path(API_TEMPLATES_URL + 
						"/id:" + flowdock.getTemplateId() + 
						"/templateItems/id:" + item.getId() + 
						"/buildStates/" + state.getShortName()
						)
						.accept(MediaType.APPLICATION_JSON_TYPE)
						.get(WebHookTemplateStateRest.class);
				prettyPrint(responseMsg);
				for (WebHookTemplateState templateState: item.getStates()){
					if (templateState.getType().equals(state.getShortName())){
						assertEquals(templateState.isEnabled(), responseMsg.isEnabled());
						assertEquals(templateState.getType(), responseMsg.getType());
					}
				}
			}
		}
	}

    
    @Test
    public void testJsonTemplatesRequestUsingSlackCompactTemplate() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_JSON_TYPE).get(Template.class);
    	
    	assertEquals(5, responseMsg.getTemplates().size());
    	assertEquals("slack.com-compact", responseMsg.id);
    	prettyPrint(responseMsg);
    }
    
    @Test
    public void testJsonTemplatesRequestUsingSlackCompactTemplateAndRequestAsXml() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	Template responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact").accept(MediaType.APPLICATION_XML_TYPE).get(Template.class);
    	
    	assertEquals(5, responseMsg.getTemplates().size());
    	assertEquals("slack.com-compact", responseMsg.id);
    	//prettyPrint(responseMsg);
    }
    
    @Test
    public void testFullJsonTemplatesRequestUsingSlackCompactTemplateAndRequestAsXml() throws FileNotFoundException, JAXBException {
    	
    	WebHookPayloadTemplate slackCompact = new SlackComCompactXmlWebHookTemplate(webHookTemplateManager, webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver, null);
    	slackCompact.register();
    	
    	WebHookTemplateConfig responseMsg = webResource.path(API_TEMPLATES_URL + "/id:slack.com-compact/fullConfig").accept(MediaType.APPLICATION_XML_TYPE).get(WebHookTemplateConfig.class);
    	
    	assertEquals(5, responseMsg.getTemplates().getTemplates().size());
    	assertEquals("slack.com-compact", responseMsg.getId());
    	//prettyPrint(responseMsg);
    }  
    

}