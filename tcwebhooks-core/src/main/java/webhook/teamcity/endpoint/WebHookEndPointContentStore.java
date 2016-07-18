package webhook.teamcity.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;
import webhook.teamcity.payload.template.render.WebHookStringRenderer.WebHookHtmlRendererException;
import lombok.Synchronized;


public class WebHookEndPointContentStore {
	
	private static final int MAX_SIZE = 100;
	
	List<WebHookEndPointPayload> store = new ArrayList<>(50);
	Comparator<WebHookEndPointPayload> dateComparator = new WebHookEndPointContentStoreCompator();
	final WebHookPayloadManager webHookPayloadManager;
	
	public WebHookEndPointContentStore(WebHookPayloadManager webHookPayloadManager) {
		this.webHookPayloadManager = webHookPayloadManager;
	}
	
	@Synchronized
	public void put(WebHookEndPointPayload payload){
		for (WebHookPayload format : webHookPayloadManager.getRegisteredFormats()){
			if (format.getContentType().equalsIgnoreCase(payload.contentType)){
				try {
					if (payload.contentType.equals(WebHookPayloadNameValuePairs.FORMAT_CONTENT_TYPE)) {
						payload.setPrettyPayload(format.getWebHookStringRenderer().render(payload.getParameters()));
					} else {
						payload.setPrettyPayload(format.getWebHookStringRenderer().render(payload.getPayload()));
					}
				} catch (WebHookHtmlRendererException e) {
					// Don't set anything here. 
					// The WebHookEndPointPayload.getPrettyPrintedPayload() will do the right thing.
				}
				break;
			}
		}
		store.add(payload);
		if (store.size() > MAX_SIZE){
			store.remove(0);
		}
	}
	
	@Synchronized
	public List<WebHookEndPointPayload> getAll(){
		List<WebHookEndPointPayload> sortedStore = new ArrayList<>();
		sortedStore.addAll(store);
		Collections.sort(sortedStore, dateComparator);
		return sortedStore;
	}
	

	public class WebHookEndPointContentStoreCompator implements Comparator<WebHookEndPointPayload> {
		
		@Override
		public int compare(WebHookEndPointPayload o1, WebHookEndPointPayload o2) {
			if (o1.getDate().before(o2.getDate())){
				return 1;
			} else if (o1.getDate().after(o2.getDate())){
				return -1;
			} else {
				return 0;
			}
		}
	}
}
