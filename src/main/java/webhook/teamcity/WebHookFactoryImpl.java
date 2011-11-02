package webhook.teamcity;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.WebHookProxyConfig;

public class WebHookFactoryImpl implements WebHookFactory {
	public WebHook getWebHook(){
		return new WebHookImpl();
	}

	public WebHook getWebHook(String url, String proxy, Integer proxyPort) {
		return new WebHookImpl(url, proxy, proxyPort);
	}

	public WebHook getWebHook(String url) {
		return new WebHookImpl(url);
	}

	public WebHook getWebHook(String url, String proxy, String proxyPort) {
		return new WebHookImpl(url, proxy, proxyPort);
	}

	public WebHook getWebHook(String url, WebHookProxyConfig proxyConfig) {
		return new WebHookImpl(url, proxyConfig);
	}
}
