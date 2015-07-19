package webhook.teamcity.payload;

import java.util.Comparator;

public class WebHookTemplateRankingComparator implements Comparator<WebHookTemplate> {
	
	public int compare(WebHookTemplate template1, WebHookTemplate template2) {
		if (template1.getRank() > template2.getRank()){
			return -1;
		} else if (template1.getRank() < template2.getRank()){
			return 1;
		} else {
			return 0;
		}
	}
}