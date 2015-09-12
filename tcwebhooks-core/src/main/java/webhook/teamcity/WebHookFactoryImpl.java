package webhook.teamcity;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.WebHookProxyConfig;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookFactoryImpl implements WebHookFactory {

	public WebHook getWebHook(WebHookConfig webHookConfig, WebHookProxyConfig proxyConfig) {
		WebHook webHook = new WebHookImpl(webHookConfig.getUrl(), proxyConfig);
		webHook.setUrl(webHookConfig.getUrl());
		webHook.setEnabled(webHookConfig.getEnabled());
		//webHook.addParams(webHookConfig.getParams());
		webHook.setBuildStates(webHookConfig.getBuildStates());
		//webHook.setProxy(myMainSettings.getProxyConfigForUrl(webHookConfig.getUrl()));
		return webHook;
	}
	
}
