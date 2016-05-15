package webhook.teamcity.settings.entity.builder;

import java.util.ArrayList;
import java.util.List;

import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public class WebHookTemplateEntityBuilder {

	public static WebHookTemplateEntity build(webhook.teamcity.payload.WebHookTemplate template){
		WebHookTemplateEntity entityTemplate = new WebHookTemplateEntity(template.getTemplateShortName(), true);
		
		entityTemplate.setTemplateDescription(template.getTemplateDescription());
		entityTemplate.setPreferredDateTimeFormat(template.getPreferredDateTimeFormat());
		entityTemplate.setTemplateToolTip(template.getTemplateToolTipText());
		entityTemplate.setRank(template.getRank());
		
		// TODO: Need to handle all the other fields and templates.
		
		return entityTemplate;
	}
	
	
	public static List<WebHookTemplateEntity> buildAll(List<webhook.teamcity.payload.WebHookTemplate> registeredTemplates) {
		List<WebHookTemplateEntity> entityTemplates = new ArrayList<WebHookTemplateEntity>();
		for (webhook.teamcity.payload.WebHookTemplate template : registeredTemplates){
			entityTemplates.add(build(template));
		}
		return entityTemplates;
	}

}
