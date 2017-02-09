package webhook.teamcity.payload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.Loggers;

public class WebHookPayloadManager {
	
	HashMap<String, WebHookPayload> formats = new HashMap<>();
	Comparator<WebHookPayload> rankComparator = new WebHookPayloadRankingComparator();
	List<WebHookPayload> orderedFormatCollection = new ArrayList<>();
	SBuildServer server;
	
	public WebHookPayloadManager(SBuildServer server){
		this.server = server;
		Loggers.SERVER.info("WebHookPayloadManager :: Starting");
	}
	
	public void registerPayloadFormat(WebHookPayload payloadFormat){
		Loggers.SERVER.info(this.getClass().getSimpleName() + " :: Registering payload " 
				+ payloadFormat.getFormatShortName() 
				+ " with rank of " + payloadFormat.getRank());
		formats.put(payloadFormat.getFormatShortName().toLowerCase(),payloadFormat);
		this.orderedFormatCollection.add(payloadFormat);
		
		Collections.sort(this.orderedFormatCollection, rankComparator);
		Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Payloads list is " + this.orderedFormatCollection.size() + " items long. Payloads are ranked in the following order..");
		for (WebHookPayload pl : this.orderedFormatCollection){
			Loggers.SERVER.debug(this.getClass().getSimpleName() + " :: Payload Name: " + pl.getFormatShortName() + " Rank: " + pl.getRank());
		}
	}

	public WebHookPayload getFormat(String formatShortname){
		if (formats.containsKey(formatShortname.toLowerCase())){
			return formats.get(formatShortname.toLowerCase());
		}
		return null;
	}
	
	public Boolean isRegisteredFormat(String format){
		return formats.containsKey(format.toLowerCase());
	}
	
	public List<WebHookPayload> getRegisteredFormats(){
		return orderedFormatCollection;
	}
	
	public Collection<WebHookPayload> getRegisteredFormatsAsCollection(){
		return orderedFormatCollection;
	}

	public SBuildServer getServer() {
		return server;
	}	
	
	
}