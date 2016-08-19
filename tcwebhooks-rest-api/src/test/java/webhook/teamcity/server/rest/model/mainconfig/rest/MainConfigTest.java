package webhook.teamcity.server.rest.model.mainconfig.rest;

import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import webhook.teamcity.server.rest.model.mainconfig.Webhooks;
import webhook.teamcity.server.rest.request.WebHookAbstractSpringAwareJerseyTest;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

public class MainConfigTest extends WebHookAbstractSpringAwareJerseyTest {

    private static final String WEBHOOKS_API_BASE = "/app/rest/webhooks";
    private static final String WEBHOOKS_API_SERVER = WEBHOOKS_API_BASE + "/server";
    private static final String WEBHOOKS_API_SERVER_JSON = WEBHOOKS_API_SERVER + "/json";
    

//
//	public MainConfigTest()throws Exception {
//        super("webhook.teamcity.test.jerseyprovider",  "webhook.teamcity.server.rest.request", "webhook.teamcity.server.rest.model", "webhook.teamcity.settings");
//    }
	
    @Before public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testXmlRequest() {
        WebResource webResource = resource();
        Webhooks responseMsg = webResource.path(WEBHOOKS_API_SERVER).accept(MediaType.APPLICATION_XML_TYPE).get(Webhooks.class);
        assertEquals("Using WebHooks in myCompany Inc.", responseMsg.getInfo().getText());
        assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", responseMsg.getInfo().getUrl());
    }
    
    @Test
    public void testJsonRequest() {
    	WebResource webResource = resource();
    	Webhooks responseMsg = webResource.path(WEBHOOKS_API_SERVER_JSON).accept(MediaType.APPLICATION_JSON_TYPE).get(Webhooks.class);
    	assertEquals("Using WebHooks in myCompany Inc.", responseMsg.getInfo().getText());
    	assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", responseMsg.getInfo().getUrl());
    }

    
    @Test
    public void testJsonRequestAndUpdate() {
    	WebResource webResource = resource();
    	Webhooks responseMsg = webResource.path(WEBHOOKS_API_SERVER).accept(MediaType.APPLICATION_JSON_TYPE).get(Webhooks.class);
    	assertEquals("Using WebHooks in myCompany Inc.", responseMsg.getInfo().getText());
    	assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", responseMsg.getInfo().getUrl());

    	responseMsg.getInfo().setText("Using WebHooks in some other company Inc.");
    	webResource.path(WEBHOOKS_API_SERVER).accept(MediaType.APPLICATION_JSON_TYPE).put(responseMsg);
    	Webhooks updatedResponse = webResource.path(WEBHOOKS_API_SERVER).accept(MediaType.APPLICATION_JSON_TYPE).get(Webhooks.class);
    	assertEquals("Using WebHooks in some other company Inc.", updatedResponse.getInfo().getText());

    }
}