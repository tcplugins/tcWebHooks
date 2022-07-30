package webhook.teamcity.extension.util;

import java.util.HashMap;
import java.util.Map;

import webhook.teamcity.history.PagedList;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.settings.WebHookSecureValuesEnquirer;

public class WebHookSecureValuesHelperService {
	
	private WebHookSecureValuesEnquirer webHookSecureValuesEnquirer;
	
	public WebHookSecureValuesHelperService(WebHookSecureValuesEnquirer webHookSecureValuesEnquirer) {
		this.webHookSecureValuesEnquirer = webHookSecureValuesEnquirer;
	}
	
	
	public Map<String,Boolean> assembleWebHookSecureValues(PagedList<WebHookHistoryItem> pagedList) {
		Map<String,Boolean> isHideSecureEnabledMap = new HashMap<>();
		for (WebHookHistoryItem historyItem : pagedList.getItems()) {
			if (! isHideSecureEnabledMap.containsKey(historyItem.getWebHookConfig().getUniqueKey())) {
				isHideSecureEnabledMap.put(
						historyItem.getWebHookConfig().getUniqueKey(), 
						this.webHookSecureValuesEnquirer.isHideSecureValuesEnabled(historyItem.getWebHookConfig().getUniqueKey())
					);
			}
		}
		return isHideSecureEnabledMap;
	}

}
