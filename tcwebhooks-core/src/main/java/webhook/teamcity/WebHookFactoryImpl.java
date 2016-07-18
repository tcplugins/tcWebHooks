package webhook.teamcity;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.WebHookProxyConfig;
import webhook.teamcity.auth.WebHookAuthenticator;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;

public class WebHookFactoryImpl implements WebHookFactory {

	final WebHookMainSettings myMainSettings;
	final WebHookAuthenticatorProvider myAuthenticatorProvider;
	
	public WebHookFactoryImpl(WebHookMainSettings mainSettings, WebHookAuthenticatorProvider authenticatorProvider) {
		this.myMainSettings = mainSettings;
		this.myAuthenticatorProvider = authenticatorProvider;
	}

	public WebHook getWebHook(WebHookConfig webHookConfig, WebHookProxyConfig proxyConfig) {
		WebHook webHook = new WebHookImpl(webHookConfig.getUrl(), proxyConfig);
		webHook.setUrl(webHookConfig.getUrl());
		webHook.setEnabled(webHookConfig.getEnabled());
		//webHook.addParams(webHookConfig.getParams());
		webHook.setBuildStates(webHookConfig.getBuildStates());
		if (webHookConfig.getAuthenticationConfig() != null){
			WebHookAuthenticator auth = myAuthenticatorProvider.getAuthenticator(webHookConfig.getAuthenticationConfig().type);
			if (auth != null){
				auth.setWebHookAuthConfig(webHookConfig.getAuthenticationConfig());
				webHook.setAuthentication(auth);
			} else {
				Loggers.SERVER.warn("Could not enable authentication type '" + webHookConfig.getAuthenticationConfig().type + "' for URL " + webHookConfig.getUrl() );
			}
		}
		
		webHook.setProxy(myMainSettings.getProxyConfigForUrl(webHookConfig.getUrl()));
		return webHook;
	}
	
}
