package webhook.teamcity.payload;

import java.util.Comparator;

public class WebHookPayloadRankingComparator implements Comparator<WebHookPayload> {
	
	public int compare(WebHookPayload payload1, WebHookPayload payload2) {
		if (payload1.getRank() > payload2.getRank()){
			return 1;
		} else if (payload1.getRank() < payload2.getRank()){
			return -1;
		} else {
			return 0;
		}
	}
}