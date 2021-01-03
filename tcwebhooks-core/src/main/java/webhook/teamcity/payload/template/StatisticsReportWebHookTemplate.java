package webhook.teamcity.payload.template;

import java.util.Collections;
import java.util.Set;

import webhook.Constants;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public class StatisticsReportWebHookTemplate extends AbstractWebHookTemplate implements WebHookPayloadTemplate {
	
	public static final String TEMPLATE_ID = "statistics-report";
	
	public StatisticsReportWebHookTemplate(WebHookTemplateManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	public void register() {
		super.register(this);
	}

	@Override
	public String getTemplateDescription() {
		return "Webhook Statistics";
	}

	@Override
	public String getTemplateToolTip() {
		return "Assemble Webhook configuration and usage statistics";
	}

	@Override
	public String getTemplateId() {
		return TEMPLATE_ID;
	}

	@Override
	public WebHookTemplateEntity getAsEntity() {
		return WebHookTemplateEntity.build(getAsConfig());
	}

	@Override
	public WebHookTemplateConfig getAsConfig() {
		WebHookTemplateConfig config = new WebHookTemplateConfig();
		config.setFormat(WebHookPayloadJson.FORMAT_SHORT_NAME);
		config.setRank(getRank());
		config.setId(getTemplateId());
		return config;
	}

	/** 
	 * Legacy Formats will always return "_Root", since they are global 
	 * and not able to be overridden.
	 */
	@Override
	public String getProjectId() {
		return Constants.ROOT_PROJECT_ID;
	}

	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return WebHookPayloadJson.FORMAT_SHORT_NAME.equalsIgnoreCase(payloadFormat);
	}

	@Override
	public WebHookTemplateContent getTemplateForState(BuildStateEnum buildState) {
		return null;
	}

	@Override
	public WebHookTemplateContent getBranchTemplateForState(BuildStateEnum buildState) {
		return null;
	}

	@Override
	public Set<BuildStateEnum> getSupportedBuildStates() {
		return Collections.singleton(BuildStateEnum.REPORT_STATISTICS);
	}

	@Override
	public Set<BuildStateEnum> getSupportedBranchBuildStates() {
		return Collections.emptySet();
	}

	@Override
	public String getPreferredDateTimeFormat() {
		return "";
	}

}
