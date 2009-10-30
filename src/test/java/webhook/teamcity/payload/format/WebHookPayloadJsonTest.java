package webhook.teamcity.payload.format;

import static org.junit.Assert.*;

import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadManager;

public class WebHookPayloadJsonTest {

	@Test
	public void testRegister() {
		WebHookPayloadManager wpm = new WebHookPayloadManager();
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
		assertTrue(whp.getFormatDescription().equals("JSON (beta)".toString()));
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
