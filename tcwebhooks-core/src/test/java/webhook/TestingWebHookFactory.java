package webhook;

import org.apache.http.impl.client.HttpClients;

public class TestingWebHookFactory {
	public WebHook getWebHook(){
		return getWebHook("http://testing.test");
	}

	public WebHook getWebHook(String url, String proxy, Integer proxyPort) {
		return getWebHook(url, new WebHookProxyConfig(proxy, proxyPort));
	}

	public WebHook getWebHook(String url) {
		return getWebHook(url, null);
	}

	public WebHook getWebHook(String url, String proxy, String proxyPort) {
		return getWebHook(url, new WebHookProxyConfig(proxy, Integer.valueOf(proxyPort)));
	}
	public WebHook getWebHook(String url, WebHookProxyConfig proxyConfig) {
		return new WebHookImpl(url, proxyConfig, HttpClients.createDefault());
	}
}