package webhook.teamcity;

import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.plugins.PluginManager;
import jetbrains.buildServer.plugins.bean.PluginInfo;
import lombok.Synchronized;

public class WebHookPluginDataResolverImpl implements WebHookPluginDataResolver {
	
	protected static final String REST_API_PLUGIN_NAME = "tcWebHooks-rest-api";
	protected static final String CORE_PLUGIN_NAME = "tcWebHooks";
	
	private final PluginManager myPluginManager;
	private Boolean isRestApiInstalled;
	private String restApiVersion;
	private String coreVersion;

	public WebHookPluginDataResolverImpl(PluginManager pluginManager) {
		this.myPluginManager = pluginManager;
	}

	@Override
	public boolean isWebHooksRestApiInstalled() {
		if (isRestApiInstalled != null) {
			return isRestApiInstalled;
		}
		findAndPopulateVersions();
		return Boolean.TRUE.equals(isRestApiInstalled);
	}

	@Override @Nullable
	public String getWebHooksRestApiVersion() {
		if (restApiVersion != null) {
			return restApiVersion;
		}
		findAndPopulateVersions();
		return restApiVersion;
	}

	@Override
	public boolean isWebHooksCoreAndApiVersionTheSame() {
		return getWebHooksCoreVersion().equals(getWebHooksRestApiVersion());
	}

	@Override
	public String getWebHooksCoreVersion() {
		if (this.coreVersion != null) {
			return this.coreVersion;
		}
		findAndPopulateVersions();
		return this.coreVersion;
	}
	
	@Synchronized
	private void findAndPopulateVersions() {
		for (PluginInfo pluginInfo : myPluginManager.getDetectedPlugins()) {
			if (REST_API_PLUGIN_NAME.equals(pluginInfo.getPluginName())) {
				this.isRestApiInstalled = true;
				this.restApiVersion = pluginInfo.getPluginVersion();
			} else if (CORE_PLUGIN_NAME.equals(pluginInfo.getPluginName())) {
				this.coreVersion = pluginInfo.getPluginVersion();
			}
		}
	}

}
