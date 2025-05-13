package webhook.teamcity.settings.secure;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.SBuildServer;

public class WebHookSecretResolverFactory {
	private static final Logger LOG = Logger.getInstance(WebHookSecretResolverFactory.class.getName());

	private static final int MINIMUM_SUPPORTED_VERSION = 46533;
	private WebHookSecretResolver webHookSecretResolver;
	
	public WebHookSecretResolverFactory(SBuildServer sBuildServer) {
		try {
			if (MINIMUM_SUPPORTED_VERSION <= Integer.parseInt(sBuildServer.getBuildNumber())) {
				webHookSecretResolver = new WebHookSecretResolverImpl();
			} else {
				webHookSecretResolver = new WebHookSecretResolverNoOpImpl();
			}
		} catch (NumberFormatException ex) {
			webHookSecretResolver = new WebHookSecretResolverNoOpImpl();
			LOG.debug("WebHookSecretResolverFactory:: NumberFormatException... WebHookSecretResolver is: '" + webHookSecretResolver.getClass() + "'.");
		}
		LOG.debug("WebHookSecretResolverFactory:: Teamcity build is: '" + sBuildServer.getBuildNumber() + "'. WebHookSecretResolver is: '" + webHookSecretResolver.getClass() + "'.");
	}
	
	public WebHookSecretResolver getWebHookSecretResolver() {
		return webHookSecretResolver;
	}



}