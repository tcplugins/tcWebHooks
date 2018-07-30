package webhook.teamcity.extension.bean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildType;
import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.history.GeneralisedWebAddress;
import webhook.teamcity.history.GeneralisedWebAddressType;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.settings.WebHookConfig;

public class BuildWebhooksBean{
	
	private SBuildType sBuildType;
	private List<WebHookConfigWithGeneralisedAddressWrapper> buildConfigs;
	private WebAddressTransformer webAddressTransformer;
	
	public BuildWebhooksBean(SBuildType b, List<WebHookConfig> c, WebAddressTransformer w) {
		this.webAddressTransformer = w;
		this.setsBuildType(b);
		this.setBuildConfigs(c);
	}

	public SBuildType getsBuildType() {
		return sBuildType;
	}

	public void setsBuildType(SBuildType sBuildType) {
		this.sBuildType = sBuildType;
	}

	public List<WebHookConfigWithGeneralisedAddressWrapper> getBuildWebHookList() {
		return buildConfigs;
	}

	public void setBuildConfigs(List<WebHookConfig> buildConfigs) {
		List<WebHookConfigWithGeneralisedAddressWrapper> buildConfigWithAddresses = new ArrayList<>();
		for (WebHookConfig c : buildConfigs) {
			buildConfigWithAddresses.add(new WebHookConfigWithGeneralisedAddressWrapper(
					c, getGeneralisedWebAddress(c.getUrl())
				));
		}
		
		this.buildConfigs = buildConfigWithAddresses;
	}
	
	private GeneralisedWebAddress getGeneralisedWebAddress(String uri) {
		if (this.webAddressTransformer != null) {
			URL url = null;
			try {
				url = new URL(uri);
			} catch (MalformedURLException e) {
				Loggers.SERVER.warn("BuildWebhooksBean :: Could not build URL from '" + url + "'" );
				try {
					url = new URL("http://unknown");
				} catch (MalformedURLException e1) {}
			}
			
			return this.webAddressTransformer.getGeneralisedHostName(url);
		}
		return GeneralisedWebAddress.build(uri, GeneralisedWebAddressType.DOMAIN_NAME);
	}

	public boolean hasBuilds(){
		return ! this.buildConfigs.isEmpty();
	}
	
	public boolean isHasBuilds(){
		return hasBuilds();
	}
	
	public boolean hasNoBuildWebHooks(){
		return this.buildConfigs.isEmpty();
	}
	
	public boolean isHasNoBuildWebHooks(){
		return hasNoBuildWebHooks();
	}
	
	public boolean hasBuildWebHooks(){
		return ! this.buildConfigs.isEmpty();
	}
	
	public boolean isHasBuildWebHooks(){
		return hasBuildWebHooks();
	}
	
	public int getBuildCount(){
		return this.buildConfigs.size();
	}
	
	public String getBuildExternalId(){
		return TeamCityIdResolver.getExternalBuildId(sBuildType);
	}
	public String getBuildName(){
		return sBuildType.getName();
	}
	
	
	public static class WebHookConfigWithGeneralisedAddressWrapper {
		private WebHookConfig webHookConfig;
		private GeneralisedWebAddress generalisedWebAddress;
		
		public WebHookConfigWithGeneralisedAddressWrapper(
				WebHookConfig webHookConfig,
				GeneralisedWebAddress generalisedWebAddress
				) {
			this.webHookConfig = webHookConfig;
			this.generalisedWebAddress = generalisedWebAddress;
		}
		
		public String getGeneralisedUrl() {
			return generalisedWebAddress.getGeneralisedAddress();
		}
		
		public String getUrl() {
			return webHookConfig.getUrl();
		}
		
		public String getEnabledListAsString() {
			return webHookConfig.getEnabledListAsString();
		}
	}
}