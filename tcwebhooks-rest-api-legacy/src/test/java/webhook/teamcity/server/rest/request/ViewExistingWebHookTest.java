package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static webhook.teamcity.server.rest.request.WebHooksRequest.API_WEBHOOKS_URL;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.server.rest.model.template.Templates;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class ViewExistingWebHookTest extends WebHookAbstractSpringAwareJerseyTest {
	
	@Autowired
	ProjectSettingsManager projectSettingsManager;
	
	WebResource webResource;
	
	@Before 
	public void setup(){
    	webResource = resource();
    	webResource.addFilter(new LoggingFilter(System.out));
	}

    @Test
    public void testXmlWebHooksRequest() {
        WebResource webResource = resource();
        ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/testProject").accept(MediaType.APPLICATION_XML_TYPE).get(ProjectWebhooks.class);
        assertEquals(0, (int)responseMsg.getCount());
    }
    
    @Test
    public void testJsonTemplatesRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_WEBHOOKS_URL + "/testProject").accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
        assertEquals(0, (int)responseMsg.count);
    }
    
    @Test
    public void testJsonWebHooksRequestUsingRegisteredWebHook() throws JAXBException, IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
		framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));
		
		Element webhooksParent = new Element("webhooks");
		framework.getWebHookProjectSettings().writeTo(webhooksParent);
		projectSettingsManager.readFrom(webhooksParent, "testProject");
		
		ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/testProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());
    }
    
    @Test
    public void testShortJsonWebHooksRequestUsingRegisteredWebHook() throws JAXBException, IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "testProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/testProject").queryParam("fields","$short").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());
    	assertNotNull(responseMsg.getWebhooks().get(0).getUrl());
    	assertNull(responseMsg.getWebhooks().get(0).getAuthentication());
    }

}