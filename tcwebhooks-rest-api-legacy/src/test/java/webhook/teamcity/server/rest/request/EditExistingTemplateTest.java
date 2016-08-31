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