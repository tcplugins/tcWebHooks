package webhook.teamcity.json;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webhook.teamcity.auth.WebHookAuthConfig;

@Data @AllArgsConstructor @NoArgsConstructor
public class WebHookAuthenticationJson {
	String type;
	Boolean preemptive;
	Map<String, String> parameters;
	
	
	public WebHookAuthConfig toWebHookAuthConfig() {
		return new WebHookAuthConfig(getType(), getPreemptive(), getParameters());
	}
	
	public static WebHookAuthenticationJson fromWebHookAuthConfig(WebHookAuthConfig webHookAuthConfig) {
		if (webHookAuthConfig == null) {
			return null;
		}
		return new WebHookAuthenticationJson(webHookAuthConfig.getType(), webHookAuthConfig.getPreemptive(), webHookAuthConfig.getParameters());
	}

}
