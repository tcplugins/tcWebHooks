package webhook.teamcity.server.rest.request;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameter;
import webhook.teamcity.server.rest.model.template.Templates;
import webhook.teamcity.server.rest.model.webhook.ProjectWebHookFilter;
import webhook.teamcity.server.rest.model.webhook.ProjectWebHookFilters;
import webhook.teamcity.server.rest.model.webhook.ProjectWebHookParameters;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhook;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static webhook.teamcity.server.rest.request.WebHooksRequest.API_WEBHOOKS_URL;

public class EditExistingWebHookTest extends WebHookAbstractSpringAwareJerseyTest {
	
	@Autowired
	private ProjectSettingsManager projectSettingsManager;
	
	private WebResource webResource;
	
	@Before 
	public void setup(){
    	webResource = resource();
    	webResource.addFilter(new LoggingFilter(System.out));
	}

    @Test
    public void testXmlWebHooksRequest() {
        WebResource webResource = resource();
        ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_XML_TYPE).get(ProjectWebhooks.class);
        assertEquals(0, (int)responseMsg.getCount());
    }
    
    @Test
    public void testJsonTemplatesRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
        assertEquals(0, (int)responseMsg.count);
    }
    
    @Test
    public void testUpdateParameters() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(2, (int)projectWebhook.getParameters().getCount());

		ProjectWebhookParameter newParameter = new ProjectWebhookParameter();
		
		newParameter.setName("myNewName");
		newParameter.setValue("myNewValue");
		ProjectWebHookParameters projectWebHookParameters = projectWebhook.getParameters();
    	projectWebHookParameters.getParameters().add(newParameter);
		ProjectWebhook updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ProjectWebhook.class, projectWebhook);
		prettyPrint(updatedResponseMsg);
		assertEquals(3, (int)updatedResponseMsg.getParameters().getCount());
    }
    
    @Test
    public void testDeleteParameter() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(2, (int)projectWebhook.getParameters().getCount());

		projectWebhook.getParameters().getParameters().remove(1); // remove second parameter
		ProjectWebhook updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ProjectWebhook.class, projectWebhook);
		prettyPrint(updatedResponseMsg);
		assertEquals(1, (int)updatedResponseMsg.getParameters().getCount());
    }
    
    @Test
    public void testDeleteParameters() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(2, (int)projectWebhook.getParameters().getCount());

		projectWebhook.getParameters().setParameters(new ArrayList<>()); // remove all parameters
		ProjectWebhook updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ProjectWebhook.class, projectWebhook);
		prettyPrint(updatedResponseMsg);
		assertEquals(0, (int)updatedResponseMsg.getParameters().getCount());
    }
    
    @Test
    public void testAddFilter() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-bearer-auth.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNotNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(0, (int)projectWebhook.getFilters().getCount());

		ProjectWebHookFilter newFilter = new ProjectWebHookFilter();
		newFilter.setEnabled(true);
		newFilter.setRegex(".*.*");
		newFilter.setValue("yes");
		ProjectWebHookFilters filters = projectWebhook.getFilters();
    	filters.getFilters().add(newFilter);
		ProjectWebhook updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ProjectWebhook.class, projectWebhook);
		prettyPrint(updatedResponseMsg);
		assertEquals(1, (int)updatedResponseMsg.getFilters().getCount());
    }
    
    @Test
    public void testAddFilters() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-bearer-auth.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNotNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(0, (int)projectWebhook.getFilters().getCount());

		ProjectWebHookFilter newFilter = new ProjectWebHookFilter();
		newFilter.setEnabled(true);
		newFilter.setRegex(".*.*");
		newFilter.setValue("yes");
		ProjectWebHookFilter newFilter2 = new ProjectWebHookFilter();
		newFilter2.setEnabled(true);
		newFilter2.setRegex(".*.*");
		newFilter2.setValue("yes");
		ProjectWebHookFilters filters = projectWebhook.getFilters();
    	filters.getFilters().add(newFilter);
    	filters.getFilters().add(newFilter2);
		ProjectWebhook updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ProjectWebhook.class, projectWebhook);
		prettyPrint(updatedResponseMsg);
		assertEquals(2, (int)updatedResponseMsg.getFilters().getCount());
    }
    
    @Test
    public void testUpdateFilters() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(2, (int)projectWebhook.getFilters().getCount());

		ProjectWebHookFilter newFilter = new ProjectWebHookFilter();
		newFilter.setEnabled(true);
		newFilter.setRegex(".*.*");
		newFilter.setValue("yes");
		ProjectWebHookFilters filters = projectWebhook.getFilters();
    	filters.getFilters().add(newFilter);
		ProjectWebhook updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ProjectWebhook.class, projectWebhook);
		prettyPrint(updatedResponseMsg);
		assertEquals(3, (int)updatedResponseMsg.getFilters().getCount());
    }
    
    
    @Test
    public void testUpdateFiltersFailsWhenFilterMalformed() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(2, (int)projectWebhook.getFilters().getCount());

		ProjectWebHookFilter newFilter = new ProjectWebHookFilter();
		newFilter.setEnabled(true);
		newFilter.setRegex(".*.*");
		ProjectWebHookFilters filters = projectWebhook.getFilters();
    	filters.getFilters().add(newFilter);
		ClientResponse updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, projectWebhook);
		assertEquals(422, updatedResponseMsg.getStatus());
    }
    
    @Test
    public void testDeleteFilter() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(2, (int)projectWebhook.getFilters().getCount());

		projectWebhook.getFilters().getFilters().remove(1); // remove second filter
		ProjectWebhook updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ProjectWebhook.class, projectWebhook);
		prettyPrint(updatedResponseMsg);
		assertEquals(1, (int)updatedResponseMsg.getFilters().getCount());
    }
    @Test
    public void testDeleteFilters() throws IOException, JDOMException {
    	
    	SortedMap<String, String> map = new TreeMap<>();
    	ExtraParameters  extraParameters  = new ExtraParameters(map); 
    	
    	WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
    	framework.loadWebHookProjectSettingsFromConfigXml(new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branchNameAndBuildNameFilter.xml"));
    	
    	Element webhooksParent = new Element("webhooks");
    	framework.getWebHookProjectSettings().writeTo(webhooksParent);
    	projectSettingsManager.readFrom(webhooksParent, "TestProject");
    	
    	ProjectWebhooks responseMsg = webResource.path(API_WEBHOOKS_URL + "/TestProject").accept(MediaType.APPLICATION_JSON_TYPE).get(ProjectWebhooks.class);
    	prettyPrint(responseMsg);
    	assertEquals(1, (int)responseMsg.getCount());

		ProjectWebhook projectWebhook = responseMsg.getWebhooks().get(0);
    	
    	assertNotNull(projectWebhook.getUrl());
    	assertNull(projectWebhook.getAuthentication());
    	assertNotNull(projectWebhook.getParameters());
    	assertNotNull(projectWebhook.getFilters());
    	assertEquals(2, (int)projectWebhook.getFilters().getCount());

		projectWebhook.getFilters().setFilters(new ArrayList<>()); // remove all filters
		ProjectWebhook updatedResponseMsg = webResource.path(projectWebhook.getHref()).accept(MediaType.APPLICATION_JSON_TYPE).put(ProjectWebhook.class, projectWebhook);
		prettyPrint(updatedResponseMsg);
		assertEquals(0, (int)updatedResponseMsg.getFilters().getCount());
    }

}