package webhook.teamcity.exception;

import webhook.teamcity.settings.WebHookConfig;

public class ProjectFeatureMigrationException extends Exception {

	private static final long serialVersionUID = 1L;
	private WebHookConfig webhookConfig;

	public ProjectFeatureMigrationException(String string, WebHookConfig w) {
		super(string);
		this.webhookConfig = w;
	}

	public ProjectFeatureMigrationException(String string, Exception ex) {
		super(string, ex);
	}

}
