package webhook.teamcity.settings;

import lombok.Getter;

@Getter
public class WebHookUpdateResult {
	boolean updated;
	WebHookConfig webHookConfig;
	
	public WebHookUpdateResult(Boolean updated, WebHookConfig webHookConfig) {
		this.updated = updated;
		this.webHookConfig = webHookConfig;
	}
}