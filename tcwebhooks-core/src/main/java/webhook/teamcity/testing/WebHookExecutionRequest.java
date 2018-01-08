package webhook.teamcity.testing;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class WebHookExecutionRequest {
	
	private String url;
	private String templateId;
	private boolean authEnabled;
	private Map<String,String> authParameters = new LinkedHashMap<>();
	

}
