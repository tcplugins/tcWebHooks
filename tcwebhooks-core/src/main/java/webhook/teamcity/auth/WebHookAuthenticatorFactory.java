package webhook.teamcity.auth;


public interface WebHookAuthenticatorFactory {
	public String getName();
	public void register();
	public WebHookAuthenticator getAuthenticatorInstance();
}
