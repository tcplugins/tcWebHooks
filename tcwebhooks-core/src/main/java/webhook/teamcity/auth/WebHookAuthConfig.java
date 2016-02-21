package webhook.teamcity.auth;

import java.util.Map;
import java.util.TreeMap;

public class WebHookAuthConfig {
	public String type = "";
	public Map<String, String> parameters = new TreeMap<String, String>();
}
