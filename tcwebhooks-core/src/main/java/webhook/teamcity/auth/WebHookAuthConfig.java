package webhook.teamcity.auth;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;

@Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class WebHookAuthConfig {
	public String type = "";
	public Boolean preemptive = true;
	
	@Singular
	public Map<String, String> parameters = new LinkedHashMap<>();
}
