package webhook.teamcity.settings.secure;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;

public class WebHookSecretResolverFactory {
	
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
			Loggers.SERVER.debug("WebHookSecretResolverFactory:: NumberFormatException... WebHookSecretResolver is: '" + webHookSecretResolver.getClass() + "'.");
		}
		Loggers.SERVER.debug("WebHookSecretResolverFactory:: Teamcity build is: '" + sBuildServer.getBuildNumber() + "'. WebHookSecretResolver is: '" + webHookSecretResolver.getClass() + "'.");
	}
	
	public WebHookSecretResolver getWebHookSecretResolver() {
		return webHookSecretResolver;
	}



}