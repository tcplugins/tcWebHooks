package webhook.teamcity.endpoint;

import org.junit.Test;

import static org.junit.Assert.*;
import static webhook.teamcity.endpoint.WebHookEndPointViewerController.stripTrailingSlash;


public class WebHookEndPointViewerControllerTest {

    @Test
    public void testStripTrailingSlash() {
        assertEquals("blah", stripTrailingSlash("blah/"));
        assertEquals("blah", stripTrailingSlash("blah"));
        assertEquals("blah/blah", stripTrailingSlash("blah/blah/"));
        assertEquals("blah/blah", stripTrailingSlash("blah/blah"));
        assertEquals("/blah/blah", stripTrailingSlash("/blah/blah/"));
        assertEquals("/blah/blah", stripTrailingSlash("/blah/blah"));
    }

}
