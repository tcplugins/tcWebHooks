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
}
