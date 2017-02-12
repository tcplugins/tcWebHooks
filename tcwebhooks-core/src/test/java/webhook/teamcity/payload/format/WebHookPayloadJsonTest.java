package webhook.teamcity.payload.format;


import jetbrains.buildServer.serverSide.SBuildServer;
import org.junit.Test;
import webhook.teamcity.payload.WebHookPayloadManager;

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
}
