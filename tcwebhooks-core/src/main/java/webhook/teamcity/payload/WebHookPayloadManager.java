package webhook.teamcity.payload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.SBuildServer;

public class WebHookPayloadManager {
	private static final Logger LOG = Logger.getInstance(WebHookPayloadManager.class.getName());

	HashMap<String, WebHookPayload> formats = new HashMap<>();
	Comparator<WebHookPayload> rankComparator = new WebHookPayloadRankingComparator();
	List<WebHookPayload> orderedFormatCollection = new ArrayList<>();
	SBuildServer server;
	
	public WebHookPayloadManager(SBuildServer server){
		this.server = server;
		LOG.debug("WebHookPayloadManager :: Starting");
	}
	
	public void registerPayloadFormat(WebHookPayload payloadFormat){
		LOG.info(this.getClass().getSimpleName() + " :: Registering payload " 
				+ payloadFormat.getFormatShortName() 
				+ " with rank of " + payloadFormat.getRank());
		formats.put(payloadFormat.getFormatShortName().toLowerCase(),payloadFormat);
		this.orderedFormatCollection.add(payloadFormat);
		
		Collections.sort(this.orderedFormatCollection, rankComparator);
		LOG.debug(this.getClass().getSimpleName() + " :: Payloads list is " + this.orderedFormatCollection.size() + " items long. Payloads are ranked in the following order..");
		for (WebHookPayload pl : this.orderedFormatCollection){
			LOG.debug(this.getClass().getSimpleName() + " :: Payload Name: " + pl.getFormatShortName() + " Rank: " + pl.getRank());
		}
	}

	public WebHookPayload getFormat(String formatShortname){
		if (formats.containsKey(formatShortname.toLowerCase())){
			return formats.get(formatShortname.toLowerCase());
		}
		throw new UnsupportedWebHookFormatException(formatShortname);
	}
	
	public Boolean isRegisteredFormat(String format){
		return formats.containsKey(format.toLowerCase());
	}
	
	public List<WebHookPayload> getRegisteredFormats(){
		return orderedFormatCollection;
	}
	
	public List<WebHookPayload> getTemplatedFormats() {
		List<WebHookPayload> templatedFormats = new ArrayList<>();
		for (WebHookPayload payload : orderedFormatCollection) {
			if (payload.getTemplateEngineType().isTemplated()) {
				templatedFormats.add(payload);
			}
		}
		return templatedFormats;
	}
	
	public Collection<WebHookPayload> getRegisteredFormatsAsCollection(){
		return orderedFormatCollection;
	}

	public SBuildServer getServer() {
		return server;
	}	
	
	
}