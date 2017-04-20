package webhook.teamcity.payload.template;

import java.util.List;

import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateBranchText;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateFormat;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateItems;
import webhook.teamcity.settings.entity.WebHookTemplateEntity.WebHookTemplateText;

public class ElasticSearchWebHookTemplate extends AbstractPropertiesBasedWebHookTemplate implements WebHookPayloadTemplate {
	
	public ElasticSearchWebHookTemplate(WebHookTemplateManager manager) {
		super(manager);
	}

	String CONF_PROPERTIES = "webhook/teamcity/payload/template/ElasticSearchWebHookTemplate.properties";


	@Override
	public String getTemplateDescription() {
		return "ElasticSearch Document Creation";
	}

	@Override
	public String getTemplateToolTip() {
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

	/** Return the date format as "yyyy-MM-dd'T'HH:mm:ss.SSSXXX".<br>
	 *  This is a format that Elastic can use.
	 */
	@Override
	public String getPreferredDateTimeFormat() {
		return "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	}

}
