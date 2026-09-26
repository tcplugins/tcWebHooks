package webhook.teamcity.server.rest.model.mainconfig.rest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import webhook.teamcity.server.rest.model.mainconfig.Information;
import webhook.teamcity.server.rest.model.mainconfig.Webhooks;
import webhook.teamcity.server.rest.request.WebHookAbstractSpringAwareJerseyTest;

public class MainConfigTest extends WebHookAbstractSpringAwareJerseyTest {

    private static final String WEBHOOKS_API_BASE = "/app/rest/webhooks";
    private static final String WEBHOOKS_API_SERVER = WEBHOOKS_API_BASE + "/server";


//
//	public MainConfigTest()throws Exception {
//        super("webhook.teamcity.test.jerseyprovider",  "webhook.teamcity.server.rest.request", "webhook.teamcity.server.rest.model", "webhook.teamcity.settings");
//    }
	
        @Before
        public void initMocks() {
        MockitoAnnotations.initMocks(this);
                seedMainConfigInfo();
    }

        private void seedMainConfigInfo() {
                Webhooks seed = new Webhooks();
                Information info = new Information();
                info.setText("Using WebHooks in myCompany Inc.");
                info.setUrl("http://intranet.mycompany.com/docs/UsingWebHooks");
                seed.setInfo(info);

                Response putResponse = target(WEBHOOKS_API_SERVER)
                                .request(MediaType.APPLICATION_JSON_TYPE)
                                .put(Entity.entity(seed, MediaType.APPLICATION_JSON_TYPE));
                assertHttpStatus(Response.Status.NO_CONTENT, putResponse);
        }


    @Test
    public void testXmlRequest() {
        Response response = target(WEBHOOKS_API_SERVER)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertHttpStatus(Response.Status.OK, response);
        Webhooks responseMsg = response.readEntity(Webhooks.class);
        assertEquals("Using WebHooks in myCompany Inc.", responseMsg.getInfo().getText());
        assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", responseMsg.getInfo().getUrl());
    }
    
    @Test
    public void testJsonRequest() {
        Response response = target(WEBHOOKS_API_SERVER)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Response.Status.OK, response);
        Webhooks responseMsg = response.readEntity(Webhooks.class);
        assertEquals("Using WebHooks in myCompany Inc.", responseMsg.getInfo().getText());
        assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", responseMsg.getInfo().getUrl());
    }

    @Test
    public void testJsonRequestAndUpdate() {
        Response getResponse = target(WEBHOOKS_API_SERVER)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Response.Status.OK, getResponse);
        Webhooks responseMsg = getResponse.readEntity(Webhooks.class);
        assertEquals("Using WebHooks in myCompany Inc.", responseMsg.getInfo().getText());
        assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", responseMsg.getInfo().getUrl());

        responseMsg.getInfo().setText("Using WebHooks in some other company Inc.");
        Response putResponse = target(WEBHOOKS_API_SERVER)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.entity(responseMsg, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(Response.Status.NO_CONTENT, putResponse);

        Response verifyResponse = target(WEBHOOKS_API_SERVER)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Response.Status.OK, verifyResponse);
        Webhooks updatedResponse = verifyResponse.readEntity(Webhooks.class);
        assertEquals("Using WebHooks in some other company Inc.", updatedResponse.getInfo().getText());

    }
}