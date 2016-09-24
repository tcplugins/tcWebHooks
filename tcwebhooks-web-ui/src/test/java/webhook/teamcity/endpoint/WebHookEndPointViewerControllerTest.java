package webhook.teamcity.endpoint;

import static org.junit.Assert.*;
import static webhook.teamcity.endpoint.WebHookEndPointViewerController.stripTrailingSlash;
import org.junit.Test;


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
