package webhook.teamcity.payload.format;


import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.junit.Test;
import org.mockito.Mock;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.ExtraParametersMap;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.testframework.WebHookMockingFramework;
import webhook.testframework.WebHookMockingFrameworkImpl;

import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebHookPayloadTailoredJsonTest {

    @Mock
    SBuildServer server;
    @Mock
    SRunningBuild buildType;
    @Mock
    SFinishedBuild sFinishedBuild;

    @Test
    public void testRegister() {
        WebHookPayloadManager wpm = new WebHookPayloadManager(server);
        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(wpm);
        whp.register();
        assertEquals(whp, wpm.getFormat(whp.getFormatShortName()));
    }

    @Test
    public void testGetContentType() {
        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);
        assertEquals(whp.getContentType().toString(), "application/json");

    }

    @Test
    public void testGetRank() {
        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);
        assertEquals(101, whp.getRank().intValue());
    }

    @Test
    public void testSetRank() {
        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);
        whp.setRank(10);
        assertTrue(whp.getRank() == 10);
    }

    @Test
    public void testGetCharset() {
        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);
        assertTrue(whp.getCharset().equals("UTF-8".toString()));
    }

    @Test
    public void testGetFormatDescription() {
        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);
        assertEquals("Tailored JSON in body", whp.getFormatDescription());
    }

    @Test
    public void testGetFormatShortName() {
        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);
        assertEquals("tailoredjson", whp.getFormatShortName());
    }

    @Test
    public void testGetFormatToolTipText() {
        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);
        assertEquals("Send a JSON payload with content specified by parameter named 'body'", whp.getFormatToolTipText());
    }

    @Test(expected = WebHookPayloadContentAssemblyException.class)
    public void testForNullPointerWithoutBody() throws WebHookPayloadContentAssemblyException {

        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);

        ExtraParametersMap extraParameters = new ExtraParametersMap(new TreeMap<String, String>());
        ExtraParametersMap teamcityProperties = new ExtraParametersMap(new TreeMap<String, String>());
        ExtraParametersMap templates = new ExtraParametersMap(new TreeMap<String, String>());
        templates.put(WebHookPayloadDefaultTemplates.HTML_BUILDSTATUS_TEMPLATE, "test template");

        WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);

        WebHookPayloadContent content = new WebHookPayloadContent(framework.getServer(), framework.getRunningBuild(), sFinishedBuild, BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties, templates);
        whp.getStatusAsString(content, null);

    }

    @Test
    public void testForNotNullPointerWithBody() throws WebHookPayloadContentAssemblyException {

        WebHookPayloadTailoredJson whp = new WebHookPayloadTailoredJson(null);

        ExtraParametersMap extraParameters = new ExtraParametersMap(new TreeMap<String, String>());
        extraParameters.put("body", "{ \"someBody\" : \"This is a body for project ${projectName} \"}");

        ExtraParametersMap teamcityProperties = new ExtraParametersMap(new TreeMap<String, String>());
        ExtraParametersMap templates = new ExtraParametersMap(new TreeMap<String, String>());
        templates.put(WebHookPayloadDefaultTemplates.HTML_BUILDSTATUS_TEMPLATE, "test template");

        WebHookMockingFramework framework = WebHookMockingFrameworkImpl.create(BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties);

        WebHookPayloadContent content = new WebHookPayloadContent(framework.getServer(), framework.getRunningBuild(), sFinishedBuild, BuildStateEnum.BUILD_FINISHED, extraParameters, teamcityProperties, templates);
        whp.getStatusAsString(content, null);
        assertEquals("{ \"someBody\" : \"This is a body for project Test Project \"}", whp.getStatusAsString(content, null));

    }
}
