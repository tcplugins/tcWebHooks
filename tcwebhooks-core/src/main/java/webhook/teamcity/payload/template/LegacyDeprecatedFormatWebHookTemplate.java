package webhook.teamcity.payload.template;

import java.util.HashSet;
import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;

public class LegacyDeprecatedFormatWebHookTemplate extends AbstractWebHookTemplate implements WebHookTemplate {
	
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
	public String getTemplateToolTipText() {
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
}
