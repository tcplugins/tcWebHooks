package webhook.teamcity.server.rest.request;

import static webhook.teamcity.server.rest.request.TemplateTestRequest.API_TEMPLATE_TEST_URL;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.model.template.TemplateTestExecutionRequest;
import webhook.teamcity.server.rest.model.template.TemplateTestHistoryItem;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

/**
 * Tests for {@link TemplateTestRequest}
 * 
 * @author netwolfuk
 *
 */
public class ExecuteTestWebHookTemplateRequestTest extends WebHookAbstractSpringAwareJerseyTest {
	
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
    public void testRequestTemplateTestsUsingJson() {
        WebResource webResource = resource();
        TemplateTestExecutionRequest executionRequest = new TemplateTestExecutionRequest(
									        		"jsontemplate",
        											"a test main template",
        											"a test branch template",
        											false,
        											"12345",
        											"project01",
        											"http://localhost/somewhere",
        											null,
        											"buildStarted");
        TemplateTestHistoryItem responseMsg = webResource.path(API_TEMPLATE_TEST_URL).type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_TYPE).post(TemplateTestHistoryItem.class, executionRequest);
        
    	prettyPrint(responseMsg);
    }
    
    @Test
    public void testRequestTemplateTestsUsingXml() {
    	WebResource webResource = resource();
    	TemplateTestExecutionRequest executionRequest = new TemplateTestExecutionRequest(
    			"jsontemplate",
    			"a test main template",
    			"a test branch template",
    			false,
    			"12345",
    			"project01",
    			"http://localhost/somewhere",
    			null,
    			"buildStarted");
    	String responseMsg = webResource.path(API_TEMPLATE_TEST_URL).type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML_TYPE).post(String.class, executionRequest);
    	
    	System.out.println(responseMsg);
    }
 
}