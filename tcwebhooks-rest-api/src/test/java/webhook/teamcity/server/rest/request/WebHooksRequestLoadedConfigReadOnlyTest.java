package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;
import org.springframework.web.context.ContextLoader;

import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

public class WebHooksRequestLoadedConfigReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testProjectWebHooksEndpointWithLoadedConfigResponds() throws JAXBException, IOException, JDOMException {
        SortedMap<String, String> map = new TreeMap<>();
        ExtraParameters extraParameters = new ExtraParameters(map);

        WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters);
        framework.loadWebHookProjectSettingsFromConfigXml(
                new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));

        Element webhooksParent = new Element("webhooks");
        framework.getWebHookProjectSettings().writeTo(webhooksParent);

        ProjectSettingsManager projectSettingsManager = ContextLoader.getCurrentWebApplicationContext()
                .getBean(ProjectSettingsManager.class);
        projectSettingsManager.readFrom(webhooksParent, "testProject");

        Response response = target(WebHooksRequest.API_WEBHOOKS_URL + "/testProject")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        ProjectWebhooks projectWebhooks = response.readEntity(ProjectWebhooks.class);
        assertEquals(1, (int) projectWebhooks.getCount());
    }
}

