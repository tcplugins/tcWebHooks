package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static webhook.teamcity.server.rest.request.WebHookParametersRequest.API_PARAMETERS_URL;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameter;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameters;

public class DeleteWebHookParameterTest extends WebHookAbstractSpringAwareJerseyTest {
	
	WebResource webResource;
	
	@Before 
	public void setup(){
    	webResource = resource();
    	webResource.addFilter(new LoggingFilter(System.out));
	}

    @Test
    public void testDeleteParameterUsingJson() {
    	final String rootProjectPath = API_PARAMETERS_URL + "/_Root";
    	
    	WebResource webResource = resource();
		ProjectWebhookParameters responseMsg = webResource.path(rootProjectPath).queryParam("fields","$long").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhookParameters.class);
    	assertEquals(0, (int)responseMsg.getCount());
    	
    	prettyPrint(responseMsg);
    	
    	ProjectWebhookParameter newParameter = new ProjectWebhookParameter();
    	newParameter.setName("A test parameter");
    	newParameter.setSecure(true);
    	newParameter.setIncludedInLegacyPayloads(true);
    	newParameter.setValue("Some value");

    	webResource.path(rootProjectPath).accept(MediaType.APPLICATION_JSON_TYPE).post(newParameter);
    	ProjectWebhookParameters updatedResponse = webResource.path(rootProjectPath).queryParam("fields","$long").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhookParameters.class);
    	assertEquals(1, (int)updatedResponse.getCount());
    	prettyPrint(updatedResponse);

    	webResource.path(rootProjectPath + "/id:" + updatedResponse.getParameters().get(0).getId()).delete();
    	
    	updatedResponse = webResource.path(rootProjectPath).queryParam("fields","$long").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhookParameters.class);
    	
    	prettyPrint(updatedResponse);

    	assertEquals(0, (int)updatedResponse.getCount());
    	
    	
    }
    
    @Test
    public void testDeleteParameterUsingXml() {
    	final String rootProjectPath = API_PARAMETERS_URL + "/_Root";
    	
    	WebResource webResource = resource();
    	ProjectWebhookParameters responseMsg = webResource.path(rootProjectPath).queryParam("fields","$long").accept(MediaType.APPLICATION_XML_TYPE).get(ProjectWebhookParameters.class);
    	assertEquals(0, (int)responseMsg.getCount());
    	
    	prettyPrint(responseMsg);
    	
    	ProjectWebhookParameter newParameter = new ProjectWebhookParameter();
    	newParameter.setName("A test parameter");
    	newParameter.setSecure(true);
    	newParameter.setIncludedInLegacyPayloads(true);
    	newParameter.setValue("Some value");
    	
    	webResource.path(rootProjectPath).accept(MediaType.APPLICATION_XML_TYPE).post(newParameter);
    	ProjectWebhookParameters updatedResponse = webResource.path(rootProjectPath).queryParam("fields","$short,value").accept(MediaType.APPLICATION_XML_TYPE).get(ProjectWebhookParameters.class);
    	assertEquals(1, (int)updatedResponse.getCount());
    	prettyPrint(updatedResponse);
    	
    	ProjectWebhookParameter param = webResource.path(rootProjectPath + "/id:" + updatedResponse.getParameters().get(0).getId()).queryParam("fields","$short").get(ProjectWebhookParameter.class);
    	prettyPrint(param);
    	webResource.path(rootProjectPath + "/id:" + updatedResponse.getParameters().get(0).getId()).delete();
    	
    	updatedResponse = webResource.path(rootProjectPath).queryParam("fields","$long").accept(MediaType.APPLICATION_XML_TYPE).get(ProjectWebhookParameters.class);
    	
    	prettyPrint(updatedResponse);
    	
    	assertEquals(0, (int)updatedResponse.getCount());
    	
    	
    }
}
