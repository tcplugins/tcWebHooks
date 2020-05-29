package webhook.teamcity.auth;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class WebHookAuthConfig {
	private String type = "";
	private Boolean preemptive = true;
	
	private Map<String, String> parameters = new LinkedHashMap<>();
	
	public WebHookAuthConfig copy() {
		WebHookAuthConfig newConfig = new WebHookAuthConfig();
		newConfig.setType(this.getType());
		newConfig.setPreemptive(this.getPreemptive());
		newConfig.getParameters().putAll(getParameters());
		return newConfig;
	}
}
