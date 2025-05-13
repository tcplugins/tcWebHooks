package webhook.teamcity;

import java.util.Collections;
import java.util.List;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTranslator;
import jetbrains.buildServer.serverSide.SRunningBuild;

public class WebHookServiceMessageHandler implements ServiceMessageTranslator {
	private static final Logger LOG = Logger.getInstance(WebHookServiceMessageHandler.class.getName());

	public static final String SERVICE_MESSAGE_NAME = "sendWebhook";
	
	private final WebHookListener myWebHookListener;
	
	public WebHookServiceMessageHandler(WebHookListener webHookListener) {
		myWebHookListener = webHookListener;
	}

	@Override
	public List<BuildMessage1> translate(SRunningBuild runningBuild, BuildMessage1 originalMessage, ServiceMessage serviceMessage) {
		myWebHookListener.serviceMessageReceived(runningBuild, serviceMessage.getAttributes());
		LOG.debug("WebHookServiceMessageHandler :: WebHook service message event received for buildType : " + runningBuild.getBuildTypeExternalId() + " with build id "  + runningBuild.getBuildId());
		
		return Collections.singletonList(originalMessage);
	}

	@Override
	public String getServiceMessageName() {
		return SERVICE_MESSAGE_NAME;
	}

}
