package webhook.teamcity.payload.format;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Before;
import org.junit.Test;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManagerImpl;
import webhook.teamcity.payload.variableresolver.WebHooksBeanUtilsVariableResolverFactory;

public class WebHookPayloadJsonTest {
	
	WebHookVariableResolverManager variableResolverManager = new WebHookVariableResolverManagerImpl();
	
	@Before
	public void setup() {
		variableResolverManager.registerVariableResolverFactory(new WebHooksBeanUtilsVariableResolverFactory());
	}
	
	@Test
	public void testRegister() {
		SBuildServer mockServer = mock(SBuildServer.class);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		WebHookPayloadManager wpm = new WebHookPayloadManager(mockServer);
		WebHookPayloadJson whp = new WebHookPayloadJson(wpm, variableResolverManager);
		whp.register();
		assertEquals(whp, wpm.getFormat(whp.getFormatShortName()));
	}

	@Test
	public void testGetContentType() {
		WebHookPayloadJson whp = new WebHookPayloadJson(null, variableResolverManager);
		assertEquals(whp.getContentType().toString(), "application/json");

	}

	@Test
	public void testGetRank() {
		WebHookPayloadJson whp = new WebHookPayloadJson(null, variableResolverManager);
		assertTrue(whp.getRank() == 100);
	}

	@Test
	public void testSetRank() {
		WebHookPayloadJson whp = new WebHookPayloadJson(null, variableResolverManager);
		whp.setRank(10);
		assertTrue(whp.getRank() == 10);
	}

	@Test
	public void testGetCharset() {
		WebHookPayloadJson whp = new WebHookPayloadJson(null, variableResolverManager);
		assertTrue(whp.getCharset().equals("UTF-8".toString()));
	}

	@Test
	public void testGetFormatDescription() {
		WebHookPayloadJson whp = new WebHookPayloadJson(null, variableResolverManager);
		assertTrue(whp.getFormatDescription().equals("JSON".toString()));
	}

	@Test
	public void testGetFormatShortName() {
		WebHookPayloadJson whp = new WebHookPayloadJson(null, variableResolverManager);
		assertTrue(whp.getFormatShortName().equals("json".toString()));
	}

	@Test
	public void testGetFormatToolTipText() {
		WebHookPayloadJson whp = new WebHookPayloadJson(null, variableResolverManager);
		assertTrue(whp.getFormatToolTipText().equals("Send the payload formatted in JSON".toString()));
	}
}
