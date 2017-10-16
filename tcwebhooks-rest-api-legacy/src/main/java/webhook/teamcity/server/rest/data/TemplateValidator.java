package webhook.teamcity.server.rest.data;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.TemplateValidationResult;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

public class TemplateValidator {
	
	public TemplateValidationResult validateNewTemplate(Template requestTemplate, TemplateValidationResult result) {
		
		if (requestTemplate.name == null || requestTemplate.name.trim().isEmpty()) {
			result.setErrored(true);
			result.addError("name", "The template name annot be empty. It is used to identify the template and is referenced by webhook configuration");
		}
		
		if (requestTemplate.format == null || requestTemplate.format.trim().isEmpty()) {
			result.setErrored(true);
			result.addError("format", "The template format cannot be empty.");
		}
		
		if (requestTemplate.rank == null || requestTemplate.rank < 0 || requestTemplate.rank > 1000) {
			result.setErrored(true);
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
	
	public TemplateValidationResult validateTemplate(WebHookTemplateConfig webHookTemplateConfig, Template requestTemplate, TemplateValidationResult result) {
		
		if (requestTemplate.defaultTemplate != null) {
			result.setErrored(true);
			result.addError("defaultTemplate", "Sorry, it's not possible to update templateItems when updating a template. Please update the templateItem specifically.");
			//validateDefaultTemplateItem(requestTemplate.defaultTemplate, result);
		}
		
		  if (requestTemplate.getTemplates() != null || requestTemplate.getTemplates().size() > 0) {
			result.setErrored(true);
			result.addError("templateItem", "Sorry, it's not possible to update templateItems when updating a template. Please update the templateItem specifically.");
			//validateDefaultTemplateItem(requestTemplate.defaultTemplate, result);
		}

		return result;
	}
	
	public TemplateValidationResult validateTemplateItem(TemplateItem templateItem, TemplateItem requestTemplateItem, TemplateValidationResult result) {
		if (!"_new".equals(requestTemplateItem.getId()) && !templateItem.getId().equals(requestTemplateItem.getId())) {
			result.setErrored(true);
			result.addError("id", "The id field must match the existing one.");
		}

		for (WebHookTemplateStateRest requestItemState : requestTemplateItem.getStates()) {
			if (BuildStateEnum.findBuildState(requestItemState.getType()) == null){ 
				result.setErrored(true);
				result.addError(requestItemState.getType(), requestItemState.getType() + " is an not a valid buildState");
			}
		}
		
		for (WebHookTemplateStateRest itemState : templateItem.getStates()) {
			WebHookTemplateStateRest requestItemState = requestTemplateItem.findConfigForBuildState(itemState.getType());
				
			if (requestItemState != null && !itemState.getEditable() && itemState.isEnabled() != requestItemState.isEnabled()) { 
				result.setErrored(true);
				result.addError(itemState.getType(), itemState.getType() + " is not editable for this templateItem");						
			}
		}
		return result;
	}

	public TemplateValidationResult validateDefaultTemplateItem(TemplateItem requestTemplateItem, TemplateValidationResult result) {
		if (requestTemplateItem.getTemplateText().getContent() == null 
				|| requestTemplateItem.getTemplateText().getContent().trim().isEmpty()) {
			result.setErrored(true);
			result.addError("templateText", "The template text content must not be null or empty.");
		}
		
		if (! requestTemplateItem.getTemplateText().getUseTemplateTextForBranch()
				&& (
						requestTemplateItem.getBranchTemplateText().getContent() == null 
					||  requestTemplateItem.getBranchTemplateText().getContent().trim().isEmpty()
					)
			) {
			result.setErrored(true);
			result.addError("branchTemplateText", "The branch template text content must not be null or empty if 'useTemplateTextForBranch' is false.");
		}
		return result;
	}
}
