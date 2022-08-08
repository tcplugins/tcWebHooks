package webhook.teamcity.settings;

public interface WebHookSecureValuesEnquirer {
	
	public boolean isHideSecureValuesEnabled(String webhookId);

}
