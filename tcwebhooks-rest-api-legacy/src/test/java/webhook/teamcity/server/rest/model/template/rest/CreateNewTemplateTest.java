package webhook.teamcity.server.rest.model.template.rest;

import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import javax.ws.rs.core.MediaType;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import webhook.teamcity.server.rest.model.template.Templates;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
	    "classpath:/TestSpringContext.xml"
	    })

public class CreateNewTemplateTest extends JerseyTest {
	
	@Mock
	private PermissionChecker permissionChecker;
	
	@Mock
	private SecurityContext securityContext;
	
	@Mock
	@InjectMocks
	private SBuildServer sBuildServer;

    private static final String API_URL = API_TEMPLATES_URL;

	public CreateNewTemplateTest()throws Exception {
        super("webhook.teamcity.test.jerseyprovider",  "webhook.teamcity.server.rest.request", "webhook.teamcity.server.rest.model", "webhook.teamcity.settings");
    }
	
//    @Before public void initMocks() {
//        MockitoAnnotations.initMocks(this);
//    }


    @Test @Ignore
    public void testXmlRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_URL).accept(MediaType.APPLICATION_XML_TYPE).get(Templates.class);

    }
    
    @Test @Ignore
    public void testJsonRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    }
    
/*    @Test
    public void testJsonRequest() {
    	WebResource webResource = resource();
    	Templates responseMsg = webResource.path(API_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertEquals("Using WebHooks in myCompany Inc.", responseMsg.getInfo().getText());
    	assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", responseMsg.getInfo().getUrl());
    }
    
    @Test @Ignore
    public void testJsonRequestAndUpdate() {
    	WebResource webResource = resource();
    	Webhooks responseMsg = webResource.path(WEBHOOKS_API_SERVER).accept(MediaType.APPLICATION_JSON_TYPE).get(Webhooks.class);
    	assertEquals("Using WebHooks in myCompany Inc.", responseMsg.getInfo().getText());
    	assertEquals("http://intranet.mycompany.com/docs/UsingWebHooks", responseMsg.getInfo().getUrl());

    	responseMsg.getInfo().setText("Using WebHooks in some other company Inc.");
    	webResource.path(WEBHOOKS_API_SERVER).accept(MediaType.APPLICATION_JSON_TYPE).put(responseMsg);
    	Webhooks updatedResponse = webResource.path(WEBHOOKS_API_SERVER).accept(MediaType.APPLICATION_JSON_TYPE).get(Webhooks.class);
    	assertEquals("Using WebHooks in some other company Inc.", updatedResponse.getInfo().getText());

    }*/
}