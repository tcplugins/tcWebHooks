package webhook.teamcity.settings.entity.builder;

import java.util.ArrayList;
import java.util.List;

import webhook.teamcity.settings.entity.WebHookTemplate;

public class WebHookTemplateEntityBuilder {

	public static WebHookTemplate build(webhook.teamcity.payload.WebHookTemplate template){
		WebHookTemplate entityTemplate = new WebHookTemplate(template.getTemplateShortName(), true);
		
		// TODO: Need to handle all the other fields and templates.
		
		return entityTemplate;
	}
	
	
	public static List<WebHookTemplate> buildAll(List<webhook.teamcity.payload.WebHookTemplate> registeredTemplates) {
		List<WebHookTemplate> entityTemplates = new ArrayList<WebHookTemplate>();
		for (webhook.teamcity.payload.WebHookTemplate template : registeredTemplates){
			entityTemplates.add(build(template));
		}
		return entityTemplates;
	}

}
