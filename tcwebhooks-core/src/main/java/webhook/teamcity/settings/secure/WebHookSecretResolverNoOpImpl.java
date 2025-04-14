package webhook.teamcity.settings.secure;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.SProject;

public class WebHookSecretResolverNoOpImpl implements WebHookSecretResolver {
	private static final Logger LOG = Logger.getInstance(WebHookSecretResolverNoOpImpl.class.getName());

	public WebHookSecretResolverNoOpImpl() {
		LOG.info("WebHookSecretResolverNoOpImpl :: Starting WebHookSecretResolver for verions older than 2017.1");
	}


	@Override
	public String getSecret(SProject sProject, String token) {
		return null;
	}

}
