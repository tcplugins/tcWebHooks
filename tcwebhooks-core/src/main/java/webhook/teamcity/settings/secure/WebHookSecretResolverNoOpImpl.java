package webhook.teamcity.settings.secure;

import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.Loggers;

public class WebHookSecretResolverNoOpImpl implements WebHookSecretResolver {
	
	public WebHookSecretResolverNoOpImpl() {
		Loggers.SERVER.info("WebHookSecretResolverNoOpImpl :: Starting WebHookSecretResolver for verions older than 2017.1");
	}


	@Override
	public String getSecret(SProject sProject, String token) {
		return null;
	}

}
