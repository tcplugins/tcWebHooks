package webhook.teamcity.executor;

import webhook.teamcity.history.WebHookHistoryItem;

public interface WebHookRunner extends Runnable {
	
	public WebHookHistoryItem getWebHookHistoryItem();

}
