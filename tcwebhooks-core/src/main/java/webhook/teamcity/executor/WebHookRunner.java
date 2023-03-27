package webhook.teamcity.executor;

import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;

public interface WebHookRunner extends Runnable {
	
	public WebHookHistoryItem getWebHookHistoryItem();
	public WebHookHistoryItem buildWebHookHistoryItem(WebHookErrorStatus webHookErrorStatus);

}
