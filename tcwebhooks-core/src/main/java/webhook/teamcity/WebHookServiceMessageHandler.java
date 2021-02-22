package webhook.teamcity;

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageHandler;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesRegister;

public class WebHookServiceMessageHandler implements ServiceMessageHandler {
	
	public static final String SERVICE_MESSAGE_NAME = "sendWebhook";
	
	private final WebHookListener myWebHookListener;
	
	public WebHookServiceMessageHandler(ServiceMessagesRegister serviceMessagesRegister, WebHookListener webHookListener) {
		myWebHookListener = webHookListener;
		serviceMessagesRegister.registerHandler(SERVICE_MESSAGE_NAME, this);
	}

	@Override
	public void handle(ServiceMessage serviceMessage) {
		Loggers.SERVER.info("WebHookServiceMessageHandler ::" + serviceMessage.asString());
		//serviceMessage.get
	}

}
