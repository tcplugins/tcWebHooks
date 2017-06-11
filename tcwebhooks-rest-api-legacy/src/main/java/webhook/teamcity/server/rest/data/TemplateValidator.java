package webhook.teamcity.server.rest.data;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.TemplateValidationResult;

public class TemplateValidator {
	
	public TemplateValidationResult validateTemplateItem(TemplateItem templateItem, TemplateItem requestTemplateItem) {
		TemplateValidationResult result = new TemplateValidationResult();
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
				result.addError(itemState.getType(), itemState.getType() + " is an not editable for this templateItem");						
			}
		}
		return result;
	}

}
