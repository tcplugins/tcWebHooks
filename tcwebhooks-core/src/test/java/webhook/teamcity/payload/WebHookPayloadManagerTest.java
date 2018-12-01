package webhook.teamcity.payload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.serverSide.SBuildServer;

public class WebHookPayloadManagerTest {
	
	@Mock
	WebHookPayload payloadOne;
	
	@Mock
	WebHookPayload payloadTwo;
	
	@Mock
	WebHookPayload payloadThree;
	
	@Mock
	SBuildServer server;
	
	WebHookPayloadManager manager;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		when(payloadOne.getFormatShortName()).thenReturn("one");
		when(payloadOne.getTemplateEngineType()).thenReturn(PayloadTemplateEngineType.LEGACY);
		when(payloadTwo.getFormatShortName()).thenReturn("two");
		when(payloadTwo.getTemplateEngineType()).thenReturn(PayloadTemplateEngineType.STANDARD);
		when(payloadThree.getFormatShortName()).thenReturn("three");
		when(payloadThree.getTemplateEngineType()).thenReturn(PayloadTemplateEngineType.VELOCITY);
		
		manager = new WebHookPayloadManager(server);
		manager.registerPayloadFormat(payloadOne);
		manager.registerPayloadFormat(payloadTwo);
		manager.registerPayloadFormat(payloadThree);
	}

	@Test
	public void testGetTemplatedFormats() {
		assertEquals(3, manager.getRegisteredFormats().size());
		assertEquals(3, manager.getRegisteredFormatsAsCollection().size());
		assertEquals(2, manager.getTemplatedFormats().size());
	}
	
	@Test
	public void testIsRegisteredFormat() {
		assertEquals(true, manager.isRegisteredFormat("one"));
		assertEquals(false, manager.isRegisteredFormat("none"));
	}
	@Test
	public void testGetFormat() {
		assertEquals("one", manager.getFormat("one").getFormatShortName());
	}
	
	@Test
	public void testGetNonExistantFormatThrowsException() {
		try {
			manager.getFormat("none");
		} catch(UnsupportedWebHookFormatException ex) {
			assert(true);
		} catch (Exception e) {
			fail();
		}
	}

}
