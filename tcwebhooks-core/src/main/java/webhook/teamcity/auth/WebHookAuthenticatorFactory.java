package webhook.teamcity.auth;

import java.util.List;

public interface WebHookAuthenticatorFactory {
	public String getName();
	public String getProjectFeaturePrefix();
	public String getKotlinDslName();
	public String getDescription();
	public void register();
	public WebHookAuthenticator getAuthenticatorInstance();
	public List<WebHookAuthenticationParameter> getParameterList();
	public boolean areAllRequiredParametersPresent(WebHookAuthConfig webHookAuthConfig);
}
