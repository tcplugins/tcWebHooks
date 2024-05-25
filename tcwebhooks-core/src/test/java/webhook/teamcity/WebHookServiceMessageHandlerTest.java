package webhook.teamcity;

import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Collections;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.serverSide.SRunningBuild;

public class WebHookServiceMessageHandlerTest {

	private static final String TEAMCITY_SERVICE_MESSAGE_PREFIX = "##teamcity[";
	private static final String TEAMCITY_SERVICE_MESSAGE_SUFFIX = "]";


	@Mock
	WebHookListener webhookListener;
	
	@InjectMocks
	WebHookServiceMessageHandler webHookServiceMessageHandler;
	
	@Mock
	SRunningBuild runningBuild;
	
	@Test
	public void testServiceMessageHandlerCalledWebHookListener() throws ParseException {
		MockitoAnnotations.initMocks(this);
		ServiceMessage serviceMessage = ServiceMessage.parse(TEAMCITY_SERVICE_MESSAGE_PREFIX + WebHookServiceMessageHandler.SERVICE_MESSAGE_NAME + " foo='bar'" + TEAMCITY_SERVICE_MESSAGE_SUFFIX);
		assertNotNull(serviceMessage);
		
		webHookServiceMessageHandler.translate(runningBuild, null, serviceMessage);
		Mockito.verify(webhookListener).serviceMessageReceived(runningBuild, Collections.singletonMap("foo", "bar"));
	}
	
	//@Test Removed so that TeamCity does not trigger it twice.
	@SuppressWarnings("java:S2699")
	public void testOutputServiceMessage() {
		System.out.println(TEAMCITY_SERVICE_MESSAGE_PREFIX + WebHookServiceMessageHandler.SERVICE_MESSAGE_NAME + " foo='bar'" + TEAMCITY_SERVICE_MESSAGE_SUFFIX);
	}

}
