package webhook.teamcity.settings.entity.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public class WebHookTemplateEntityBuilder {
	
	private WebHookTemplate template;

	public WebHookTemplateEntityBuilder(webhook.teamcity.payload.WebHookTemplate template){
		this.template = template;
	}

	public WebHookTemplateEntity build(){
		WebHookTemplateEntity entityTemplate = new WebHookTemplateEntity(template.getTemplateShortName(), true);
		
		entityTemplate.setTemplateDescription(template.getTemplateDescription());
		entityTemplate.setPreferredDateTimeFormat(template.getPreferredDateTimeFormat());
		entityTemplate.setTemplateToolTip(template.getTemplateToolTip());
		entityTemplate.setRank(template.getRank());
		
		// TODO: Need to handle all the other fields and templates.
		
		/*
		 * Notes:
		 * 
		 * Need to check if all states are supported.
		 * If they are, then set a default template and then override for specific templates.
		 * 
		 */
		
		// If all states are supported, let's try to find a suitable default. 
		if (allBuildStatesAreSupportedByTemplate()){
			
		}
		
		
		return entityTemplate;
	}
	
	
	public static List<WebHookTemplateEntity> buildAll(List<webhook.teamcity.payload.WebHookTemplate> registeredTemplates) {
		List<WebHookTemplateEntity> entityTemplates = new ArrayList<WebHookTemplateEntity>();
		for (webhook.teamcity.payload.WebHookTemplate template : registeredTemplates){
			entityTemplates.add(new WebHookTemplateEntityBuilder(template).build());
		}
		return entityTemplates;
	}
	
	public boolean allBuildStatesAreSupportedByTemplate() {
		return this.template.getSupportedBuildStates().containsAll(Arrays.asList(BuildStateEnum.getNotifyStates()));
	}

}
