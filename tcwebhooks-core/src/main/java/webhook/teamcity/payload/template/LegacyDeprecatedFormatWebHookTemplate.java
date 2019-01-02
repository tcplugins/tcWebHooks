package webhook.teamcity.payload.template;

import java.util.HashSet;
import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public abstract class LegacyDeprecatedFormatWebHookTemplate extends AbstractWebHookTemplate implements WebHookPayloadTemplate {

	Set<BuildStateEnum> states = new HashSet<>();

	public LegacyDeprecatedFormatWebHookTemplate(WebHookTemplateManager manager) {
		super();
		this.manager = manager;
		for (BuildStateEnum b : BuildStateEnum.getNotifyStates()){
			states.add(b);
		}
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

}
