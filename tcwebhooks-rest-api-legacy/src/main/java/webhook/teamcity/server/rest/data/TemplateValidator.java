package webhook.teamcity.server.rest.data;

import java.util.regex.Pattern;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.ErrorResult;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

public class TemplateValidator {
	
	public ErrorResult validateNewTemplate(Template requestTemplate, ErrorResult result) {
		
		if (requestTemplate.name == null || requestTemplate.name.trim().isEmpty()) {
			result.addError("name", "The template name cannot be empty. It is used to identify the template and is referenced by webhook configuration");
		}
		
		if (requestTemplate.name != null && ! Pattern.matches("^[A-Za-z0-9_.-]+$", requestTemplate.name) ) {
			result.addError("name", "The template name can only be 'A-Za-z0-9_.-'. It is used to identify the template and is referenced by webhook configuration");
		}
		
		if (requestTemplate.format == null || requestTemplate.format.trim().isEmpty()) {
			result.addError("format", "The template format cannot be empty.");
		}
		
		if (requestTemplate.rank == null || requestTemplate.rank < 0 || requestTemplate.rank > 1000) {
			result.addError("rank", "The template rank cannot be empty and must be between 0 and 1000.");
		}
		
		if (requestTemplate.defaultTemplate != null) {
			validateDefaultTemplateItem(requestTemplate.defaultTemplate, result);
		}
		
		if (requestTemplate.getTemplates() != null) {
			for (TemplateItem templateItem : requestTemplate.getTemplates()) {
				validateTemplateItem(templateItem, templateItem, result);
			}
		}
		return result;
		
	}
	
	public ErrorResult validateTemplate(WebHookTemplateConfig webHookTemplateConfig, Template requestTemplate, ErrorResult result) {
		
		if ( ! webHookTemplateConfig.getName().equals(requestTemplate.name)) {
			result.addError("name", "Sorry, it's not possible to change the name (aka id) of an existing template. Please create a new template with a new name and delete this one.");
			
		}
		
		if (requestTemplate.defaultTemplate != null) {
			result.addError("defaultTemplate", "Sorry, it's not possible to update templateItems when updating a template. Please update the templateItem specifically.");
			//validateDefaultTemplateItem(requestTemplate.defaultTemplate, result);
		}
		
		  if (requestTemplate.getTemplates() != null) {
			result.addError("templateItem", "Sorry, it's not possible to update templateItems when updating a template. Please update the templateItem specifically.");
			//validateDefaultTemplateItem(requestTemplate.defaultTemplate, result);
		}

		return result;
	}
	
	public ErrorResult validateTemplateItem(TemplateItem templateItem, TemplateItem requestTemplateItem, ErrorResult result) {
		if (!"_new".equals(requestTemplateItem.getId()) && !templateItem.getId().equals(requestTemplateItem.getId())) {
			result.addError("id", "The id field must match the existing one.");
		}

		validateTemplateText(requestTemplateItem, result);
		
		for (WebHookTemplateStateRest requestItemState : requestTemplateItem.getStates()) {
			if (BuildStateEnum.findBuildState(requestItemState.getType()) == null){ 
				result.addError(requestItemState.getType(), requestItemState.getType() + " is an not a valid buildState");
			}
		}
		
		for (WebHookTemplateStateRest itemState : templateItem.getStates()) {
			WebHookTemplateStateRest requestItemState = requestTemplateItem.findConfigForBuildState(itemState.getType());
				
			if (requestItemState != null && !itemState.getEditable() && itemState.isEnabled() != requestItemState.isEnabled()) { 
				result.addError(itemState.getType(), itemState.getType() + " is not editable for this templateItem");						
			}
		}
		return result;
	}

	public ErrorResult validateDefaultTemplateItem(TemplateItem requestTemplateItem, ErrorResult result) {
		return validateTemplateText(requestTemplateItem, result);
	}

	private ErrorResult validateTemplateText(TemplateItem requestTemplateItem, ErrorResult result) {
		if (requestTemplateItem.getTemplateText().getContent() == null 
				|| requestTemplateItem.getTemplateText().getContent().trim().isEmpty()) {
			result.addError("templateText", "The template text content must not be null or empty.");
		}
		
		if (! requestTemplateItem.getTemplateText().getUseTemplateTextForBranch()
				&& (
						requestTemplateItem.getBranchTemplateText().getContent() == null 
					||  requestTemplateItem.getBranchTemplateText().getContent().trim().isEmpty()
					)
			) {
			result.addError("branchTemplateText", "The branch template text content must not be null or empty if 'useTemplateTextForBranch' is false.");
		}
		return result;
	}
}
