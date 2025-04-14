package webhook.teamcity.settings.project;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.TeamCityCoreFacade;

public class WebHookParameterStoreFactoryImpl implements WebHookParameterStoreFactory {

	private static final Logger LOG = Logger.getInstance(WebHookParameterStoreFactoryImpl.class.getName());
	private static final int MINIMUM_SUPPORTED_VERSION = 42002;  // TeamCity 10
	private WebHookParameterStore webHookParameterStore;

	
	public WebHookParameterStoreFactoryImpl(SBuildServer sBuildServer, TeamCityCoreFacade teamCityCore) {
		try {
			if (MINIMUM_SUPPORTED_VERSION <= Integer.parseInt(sBuildServer.getBuildNumber())) {
				webHookParameterStore = new WebHookParameterStoreImpl(teamCityCore);
			} else {
				webHookParameterStore = new WebHookParameterStoreNoOpImpl();
			}
		} catch (NumberFormatException ex) {
			webHookParameterStore = new WebHookParameterStoreNoOpImpl();
			LOG.debug("WebHookParameterStoreFactory:: NumberFormatException... WebHookParameterStore is: '" + webHookParameterStore.getClass() + "'.");
		}
		LOG.debug("WebHookParameterStoreFactory:: Teamcity build is: '" + sBuildServer.getBuildNumber() + "'. WebHookParameterStore is: '" + webHookParameterStore.getClass() + "'.");
	}
	
	@Override
	public WebHookParameterStore getWebHookParameterStore() {
		return webHookParameterStore;
	}



}