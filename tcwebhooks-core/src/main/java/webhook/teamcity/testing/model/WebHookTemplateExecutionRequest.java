package webhook.teamcity.testing.model;

import lombok.Builder;
import lombok.Data;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateBranchText;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateText;

@Data @Builder
public class WebHookTemplateExecutionRequest {
	
	// These are obtained from the test request.
	private Long buildId;
	private String projectExternalId;
	private BuildStateEnum testBuildState;
	private String uniqueKey;
	private String url;
	
	// These are obtained from the edit WebHookTemplate dialog box, 
	// because they may not have been persisted by the dialog yet.
	private String id;
	private WebHookTemplateText defaultTemplate;
	private WebHookTemplateBranchText defaultBranchTemplate;
	private String preferredDateTimeFormat;
	private String format;

	public WebHookTemplateConfig toConfig() {
		WebHookTemplateConfig config = new WebHookTemplateConfig(this.id, true);
		config.setPreferredDateTimeFormat(this.preferredDateTimeFormat);
		config.setDefaultTemplate(this.defaultTemplate);
		config.setDefaultBranchTemplate(this.defaultBranchTemplate);
		config.setFormat(this.format);
		return config;
	}
	
}
