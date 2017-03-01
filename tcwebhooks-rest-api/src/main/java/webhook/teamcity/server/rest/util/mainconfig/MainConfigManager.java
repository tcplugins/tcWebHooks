package webhook.teamcity.server.rest.util.mainconfig;

import javax.ws.rs.core.UriInfo;

import jetbrains.buildServer.server.rest.util.BeanContext;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.server.rest.model.mainconfig.Information;
import webhook.teamcity.server.rest.model.mainconfig.NoProxy;
import webhook.teamcity.server.rest.model.mainconfig.Proxy;
import webhook.teamcity.server.rest.model.mainconfig.Webhooks;
import webhook.teamcity.settings.WebHookMainSettings;

public class MainConfigManager {
	
	WebHookMainSettings mainConfig;
	
	public MainConfigManager(@NotNull final WebHookMainSettings config) {
		mainConfig = config;
	}
	
	public Webhooks build(UriInfo uriInfo) {
		Webhooks webhooks = new Webhooks(uriInfo);
		Information info = new Information(uriInfo);
		boolean showInfo = false;
		
		if (mainConfig.getInfoUrl() != null){
			info.setUrl(mainConfig.getInfoUrl());
			showInfo = true;
		}
		if (mainConfig.getInfoText() != null){
			info.setText(mainConfig.getInfoText());
			showInfo = true;
		}
		
		if (mainConfig.getWebhookShowFurtherReading() != null){
			info.setFurtherReading(mainConfig.getWebhookShowFurtherReading());
			showInfo = true;
		}
		if (showInfo){
			webhooks.setInfo(info);
		}
		
		Proxy proxy = new Proxy();
		boolean showProxy = false;
		if (mainConfig.getWebHookMainConfig().getProxyHost() != null){
			proxy.setHost(mainConfig.getWebHookMainConfig().getProxyHost());
			showProxy = true;
		}
		
		if (mainConfig.getWebHookMainConfig().getProxyPort() != null){
			proxy.setPort(mainConfig.getWebHookMainConfig().getProxyPort());
			showProxy = true;
		}
		
		for (String noProxyUrl : mainConfig.getWebHookMainConfig().getNoProxyUrls()){
			proxy.addNoProxy(new NoProxy(noProxyUrl));
			showProxy = true;
		}
		
		if (showProxy){
			webhooks.setProxy(proxy);
		}
		
		return webhooks;
	}
	
	public void updateMainConfig (Webhooks webhooks){
		if (webhooks.getInfo() != null){
			mainConfig.getWebHookMainConfig().setWebhookShowFurtherReading(webhooks.getInfo().getFurtherReading());
			if (webhooks.getInfo().getText() != null){
				mainConfig.getWebHookMainConfig().setWebhookInfoText(webhooks.getInfo().getText());
			}
			if (webhooks.getInfo().getUrl() != null){
				mainConfig.getWebHookMainConfig().setWebhookInfoUrl(webhooks.getInfo().getUrl());
			}
		}
	}
	
	public void updateInfo(Information info){
		
	}

}
