package webhook.teamcity.auth;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractWebHookAuthenticator implements WebHookAuthenticator {

	protected WebHookAuthConfig config;

	@Override
	@NotNull
	public WebHookAuthConfig getWebHookAuthConfig() {
		return config;
	}

	@Override
	public void setWebHookAuthConfig(WebHookAuthConfig authenticationConfig) {
		this.config = authenticationConfig;
		
	}

}
