package webhook.teamcity.testing.model;

import lombok.Data;
import webhook.teamcity.BuildStateEnum;

@Data
public class WebHookTemplateExecutionRequest {
	
	// These are obtained from the test request.
	private Long buildId;
	private String projectId;
	private BuildStateEnum testBuildState;
	private String uniqueKey;
	private String url;
	
	// These are obtained from the edit WebHookTempalte dialog box, 
	// because they may not have been persisted by the dialog yet.
	private String content;

}
