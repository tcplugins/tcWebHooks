package webhook.teamcity.auth;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebHookAuthConfig {
	public String type = "";
	public Boolean preemptive = true;
	public Map<String, String> parameters = new LinkedHashMap<>();
}
