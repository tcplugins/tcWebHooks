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
		fail("Not yet implemented");
	}

	@Test
	public void testWebHookPayloadJson() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFormatDescription() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFormatShortName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFormatToolTipText() {
		fail("Not yet implemented");
	}

	@Test
	public void testWebHookPayloadGeneric() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPayloadManager() {
		fail("Not yet implemented");
	}

	@Test
	public void testBeforeBuildFinish() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildChangedStatus() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildFinished() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildInterrupted() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuildStarted() {
		fail("Not yet implemented");
	}

	@Test
	public void testResponsibleChanged() {
		fail("Not yet implemented");
	}

}
