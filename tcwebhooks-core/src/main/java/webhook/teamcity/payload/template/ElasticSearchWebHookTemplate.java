package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.payload.format.WebHookPayloadTailoredJson;

public class ElasticSearchWebHookTemplate extends AbstractPropertiesBasedWebHookTemplate implements WebHookTemplate {
	
	public ElasticSearchWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);
	}

	String CONF_PROPERTIES = "webhook/teamcity/payload/template/ElasticSearchWebHookTemplate.properties";


	@Override
	public String getTemplateDescription() {
		return "ElasticSearch Document Creation";
	}

	@Override
	public String getTemplateToolTipText() {
		return "Creates document in Elastic Search index";
	}

	@Override
	public String getTemplateShortName() {
		return "elasticsearch";
	}
	
	@Override
	public boolean supportsPayloadFormat(String payloadFormat) {
		return payloadFormat.equalsIgnoreCase(WebHookPayloadJsonTemplate.FORMAT_SHORT_NAME);
	}

	@Override
	public String getLoggingName() {
		return "ElasticSearchWebHookTemplate";
	}

	@Override
	public String getPropertiesFileName() {
		return CONF_PROPERTIES;
	}

}
