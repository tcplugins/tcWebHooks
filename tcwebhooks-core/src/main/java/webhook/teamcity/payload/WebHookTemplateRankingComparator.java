package webhook.teamcity.payload;

import java.util.Comparator;

public class WebHookTemplateRankingComparator implements Comparator<WebHookPayloadTemplate> {
	
	public int compare(WebHookPayloadTemplate template1, WebHookPayloadTemplate template2) {
		// First compare the rank of the template
		if (template1.getRank() > template2.getRank()){
			return 1;
		} else if (template1.getRank() < template2.getRank()){
			return -1;
			
		// If both templates have the same rank, compare the name and sort alphabetically
		} else {
	        int res = String.CASE_INSENSITIVE_ORDER.compare(template1.getTemplateShortName(), template2.getTemplateShortName());
	        return (res != 0) ? res : template1.getTemplateShortName().compareTo(template2.getTemplateShortName());
		}
	}
}