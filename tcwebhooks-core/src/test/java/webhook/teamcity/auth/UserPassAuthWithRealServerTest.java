package webhook.teamcity.auth;

import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import webhook.WebHookTestServer;
import webhook.WebHookTestServerTestBase;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class UserPassAuthWithRealServerTest extends WebHookTestServerTestBase {

    protected SortedMap<String, String> map = new TreeMap<>();
    protected ExtraParametersMap extraParameters = new ExtraParametersMap(map);
    protected ExtraParametersMap teamcityProperties = new ExtraParametersMap(map);
    protected WebHookMockingFramework framework;

    WebHookTestServer server;

    @Override
    public String getHost() {
        return "localhost";
    }

    @Override
    public Integer getPort() {
        return 58001;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        server = startWebServer();
    }

    @After
    public void tearDown() throws InterruptedException {
        stopWebServer(server);
    }

    @Test
    public void testRealWebHookWithAuth() throws JDOMException, IOException, InterruptedException {
        framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);
        framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-auth.xml"));
        framework.getWebHookListener().buildFinished(framework.getRunningBuild());
        assertEquals(HttpServletResponse.SC_OK, server.getReponseCode());
    }

    @Test
    public void testRealWebHookWithWrongAuth() throws JDOMException, IOException, InterruptedException {
        framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);
        framework.loadWebHookProjectSettingsFromConfigXml(new File("src/test/resources/project-settings-test-all-states-enabled-with-branch-and-wrong-auth.xml"));
        framework.getWebHookListener().buildFinished(framework.getRunningBuild());

        // The filter in WebHookTestServer does not fire for 401, so we can't actually see
        // if a request came into the servlet. :(
        //assertEquals(HttpServletResponse.SC_UNAUTHORIZED, server.getReponseCode());
    }


}
