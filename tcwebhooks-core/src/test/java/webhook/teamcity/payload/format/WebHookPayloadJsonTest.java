package webhook.teamcity.payload.format;


import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import org.junit.Test;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.MockSBuildType;
import webhook.teamcity.MockSProject;
import webhook.teamcity.MockSRunningBuild;
import webhook.teamcity.payload.WebHookPayloadDefaultTemplates;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.WebHookPayloadContent;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebHookPayloadJsonTest {

    @Test
    public void testRegister() {
        SBuildServer mockServer = mock(SBuildServer.class);
        when(mockServer.getRootUrl()).thenReturn("http://test.url");
        WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
        WebHookPayloadJson whp = new WebHookPayloadJson(wpm);
        whp.register();
        assertEquals(whp, wpm.getFormat(whp.getFormatShortName()));
    }

    @Test
    public void testGetContentType() {
        WebHookPayloadJson whp = new WebHookPayloadJson(null);
        assertEquals(whp.getContentType().toString(), "application/json");

    }

    @Test
    public void testGetRank() {
        WebHookPayloadJson whp = new WebHookPayloadJson(null);
        assertTrue(whp.getRank() == 100);
    }

    @Test
    public void testSetRank() {
        WebHookPayloadJson whp = new WebHookPayloadJson(null);
        whp.setRank(10);
        assertTrue(whp.getRank() == 10);
    }

    @Test
    public void testGetCharset() {
        WebHookPayloadJson whp = new WebHookPayloadJson(null);
        assertTrue(whp.getCharset().equals("UTF-8".toString()));
    }

    @Test
    public void testGetFormatDescription() {
        WebHookPayloadJson whp = new WebHookPayloadJson(null);
        assertTrue(whp.getFormatDescription().equals("JSON".toString()));
    }

    @Test
    public void testGetFormatShortName() {
        WebHookPayloadJson whp = new WebHookPayloadJson(null);
        assertTrue(whp.getFormatShortName().equals("json".toString()));
    }

    @Test
    public void testGetFormatToolTipText() {
        WebHookPayloadJson whp = new WebHookPayloadJson(null);
        assertTrue(whp.getFormatToolTipText().equals("Send the payload formatted in JSON".toString()));
    }

    @Test
    public void testGetFormatContent() {
        MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
        String triggeredBy = "SubVersion";
        MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, triggeredBy, Status.NORMAL, "Running", "TestBuild01");
        SFinishedBuild previousBuild = mock(SFinishedBuild.class);
        when(previousBuild.getFinishDate()).thenReturn(new Date());
        MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
        sBuildType.setProject(sProject);
        SBuildServer mockServer = mock(SBuildServer.class);
        when(mockServer.getRootUrl()).thenReturn("http://test.url");
        WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
        WebHookPayloadJson whp = new WebHookPayloadJson(wpm);
        whp.register();

        SortedMap<String, String> extraParameters = new TreeMap<>();

        extraParameters.put("item1", "content1");
        extraParameters.put("item2", "content2");
        extraParameters.put("item3", "content3");
        extraParameters.put("item4", "content4");
        extraParameters.put("item5", "content5");

        assertEquals(whp, wpm.getFormat(whp.getFormatShortName()));
        WebHookPayloadContent content = new WebHookPayloadContent(mockServer, sRunningBuild, previousBuild, BuildStateEnum.BUILD_SUCCESSFUL, extraParameters, extraParameters, WebHookPayloadDefaultTemplates.getDefaultEnabledPayloadTemplates());
        String result = whp.getStatusAsString(content, null);
        System.out.println(result);
    }
}
