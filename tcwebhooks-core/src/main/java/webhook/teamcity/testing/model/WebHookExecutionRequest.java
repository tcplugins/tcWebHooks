package webhook.teamcity.testing.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import webhook.teamcity.BuildStateEnum;

@Data @Builder
public class WebHookExecutionRequest {

	// These are obtained from the test request.
	private Long buildId;
	private String projectExternalId;
	private BuildStateEnum testBuildState;
	
	// These are obtained from the edit WebHook dialog box, 
	// because they may not have been persisted by the dialog yet.
	private String uniqueKey;
	private String url;
	private String templateId;
	private String authType;
	private boolean authEnabled;
	private boolean authPreemptive;
	@Builder.Default private Map<String,String> authParameters = new LinkedHashMap<>();
	private Map<BuildStateEnum, Boolean> configBuildStates;

}
