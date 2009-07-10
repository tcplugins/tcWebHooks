package webhook.teamcity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import jetbrains.buildServer.log.Loggers;
import webhook.WebHookPayload;

public class WebHookPayloadManager {
	
	HashMap<String, WebHookPayload> formats = null; 
	
	public WebHookPayloadManager(){
		Loggers.SERVER.info("WebHookPayloadManager :: Starting");
		formats = new HashMap<String,WebHookPayload>();
	}
	
	public void registerPayloadFormat(WebHookPayload payloadFormat){
		Loggers.SERVER.info("WebHookPayloadManager :: Registering payload " + payloadFormat.getFormatShortName());
		formats.put(payloadFormat.getFormatShortName(),payloadFormat);
	}

	public WebHookPayload getFormat(String formatShortname){
		if (formats.containsKey(formatShortname)){
			return formats.get(formatShortname);
		}
		return null;
	}
	
	public Boolean isRegisteredFormat(String format){
		return formats.containsKey(format);
	}
	
	public Set<String> getRegisteredFormats(){
		return formats.keySet();
	}
	
	public Collection<WebHookPayload> getRegisteredFormatsAsCollection(){
		return this.formats.values();
	}
}
