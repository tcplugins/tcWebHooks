package webhook.teamcity.settings.secure;

import org.apache.commons.lang3.StringUtils;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.impl.SecureDataStorage;

/**
 * WebHookSecretResolver implementation for TeamCity 2017.1 or higher.
 *
 */
public class WebHookSecretResolverImpl implements WebHookSecretResolver {
	
	public WebHookSecretResolverImpl() {
		Loggers.SERVER.info("WebHookSecretResolverImpl :: Starting WebHookSecretResolver for 2017.1 and newer");
	}

	@Override
	public String getSecret(SProject sProject, String token) {
		if (StringUtils.isBlank(token)) {
			return null;
		}
		// Will try to resolve the token's associated value, or will return the same token unresolved. 
		final String value = ((SecureDataStorage) sProject).getSecureValue(token, "WebHook payload assembly");
		
		// If we got the same token back, a match was not found. So return null. 
		if (!token.equals(value)) {
			return value;
		}
		return null;
	}

}
