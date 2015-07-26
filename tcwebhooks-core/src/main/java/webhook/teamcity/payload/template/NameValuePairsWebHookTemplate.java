package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadNameValuePairs;

public class NameValuePairsWebHookTemplate extends AbstractPropertiesBasedWebHookTemplate implements WebHookTemplate {
	
	String CONF_PROPERTIES = "webhook/teamcity/payload/template/NameValuePairsWebHookTemplate.properties";
	
	public NameValuePairsWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);
	}

	@Override
	public String getTemplateDescription() {
		return "Template for NameValue Pairs";
	}

	@Override
	public String getTemplateToolTipText() {
		return "A customisable template which POSTs contents as url-encoded name/value pairs (just like a normal html form).";
	}

	@Override
	public String getTemplateShortName() {
		return "originalNvpairsTemplate";
	}
	
	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return payloadFormat.equals(WebHookPayloadNameValuePairs.FORMAT_SHORT_NAME);
	}
	
	@Override
	public String getLoggingName() {
		return "NameValuePairsWebHookTemplate";
	}

	@Override
	public String getPropertiesFileName() {
		return CONF_PROPERTIES;
	}

}
