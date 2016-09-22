package webhook.teamcity.auth;

import java.util.List;

public interface WebHookAuthenticatorFactory {
	public String getName();
	public String getDescription();
	public void register();
	public WebHookAuthenticator getAuthenticatorInstance();
	public List<WebHookAuthenticationParameter> getParameterList();
}
