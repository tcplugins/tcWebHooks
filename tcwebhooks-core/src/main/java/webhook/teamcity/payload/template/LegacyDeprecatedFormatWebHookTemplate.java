package webhook.teamcity.payload.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateBranchText;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateFormat;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItems;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateText;

public class LegacyDeprecatedFormatWebHookTemplate extends AbstractWebHookTemplate implements WebHookPayloadTemplate {
	
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
	public String getTemplateToolTip() {
		return "The legacy non-templated webhooks.";
	}

	@Override
	public String getTemplateShortName() {
		return "none";
	}
	
	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return  payloadFormat.equalsIgnoreCase("JSON") || 
				payloadFormat.equalsIgnoreCase("nvpairs") || 
				payloadFormat.equalsIgnoreCase("xml") || 
				payloadFormat.equalsIgnoreCase("tailoredjson") || 
				payloadFormat.equalsIgnoreCase("none");
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
		return null;
	}

	@Override
	public WebHookTemplateConfig getAsConfig() {
		return null;
	}

}
