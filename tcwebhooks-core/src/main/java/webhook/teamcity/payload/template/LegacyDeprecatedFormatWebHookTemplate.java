package webhook.teamcity.payload.template;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import webhook.Constants;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public abstract class LegacyDeprecatedFormatWebHookTemplate extends AbstractWebHookTemplate implements WebHookPayloadTemplate {

	Set<BuildStateEnum> states = new HashSet<>();

	protected LegacyDeprecatedFormatWebHookTemplate(WebHookTemplateManager manager) {
		super();
		this.manager = manager;
		Collections.addAll(states, BuildStateEnum.getNotifyStates());
	}

	@Override
	public String getTemplateDescription() {
		return "Legacy Webhook";
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
		return states;
	}

	@Override
	public Set<BuildStateEnum> getSupportedBranchBuildStates() {
		return states;
	}

	@Override
	public void register() {
		super.register(this);
	}

	public abstract String getLegacyFormat();

	/**
	 * Return an empty dateFormat string. This should then let
	 * SimpleDateFormat pick a format that suits the locale.
	 */
	@Override
	public String getPreferredDateTimeFormat() {
		return "";
	}

	@Override
	public WebHookTemplateEntity getAsEntity() {
		return WebHookTemplateEntity.build(getAsConfig());
	}

	@Override
	public WebHookTemplateConfig getAsConfig() {
		WebHookTemplateConfig config = new WebHookTemplateConfig();
		config.setFormat(getLegacyFormat());
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

}
