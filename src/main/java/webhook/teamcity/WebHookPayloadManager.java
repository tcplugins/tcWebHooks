package webhook.teamcity;

import java.util.HashMap;

import jetbrains.buildServer.log.Loggers;

import webhook.WebHookPayload;

public class WebHookPayloadManager {
	
	HashMap<String, WebHookPayload> formats = null; 
	
	public WebHookPayloadManager(){
		Loggers.SERVER.debug("WebHookPayloadManager :: Starting");
		formats = new HashMap<String,WebHookPayload>();
	}
	
	public void registerPayloadFormat(WebHookPayload payloadFormat){
		Loggers.SERVER.debug("WebHookPayloadManager :: Registering payload " + payloadFormat.getFormatShortName());
		formats.put(payloadFormat.getFormatShortName(),payloadFormat);
	}

	public WebHookPayload getFormat(String formatShortname){
		if (formats.containsKey(formatShortname)){
			return formats.get(formatShortname);
		}
		return null;
	}
}
