package webhook.teamcity;

import webhook.WebHook;
import webhook.WebHookProxyConfig;
import webhook.teamcity.settings.WebHookConfig;

public interface WebHookFactory {
	public abstract WebHook getWebHook(WebHookConfig webhookConfig, WebHookProxyConfig pc);
}
