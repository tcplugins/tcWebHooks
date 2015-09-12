package webhook.teamcity;

import webhook.WebHook;
import webhook.WebHookProxyConfig;
import webhook.teamcity.settings.WebHookConfig;

public interface WebHookFactory {
//	public abstract WebHook getWebHook();
//	public abstract WebHook getWebHook(String url, String proxy,
//			Integer proxyPort);
//	public abstract WebHook getWebHook(String string);
//	public abstract WebHook getWebHook(String url, String proxy,
//			String proxyPortString);
	public abstract WebHook getWebHook(WebHookConfig webhookConfig, WebHookProxyConfig pc);
}
