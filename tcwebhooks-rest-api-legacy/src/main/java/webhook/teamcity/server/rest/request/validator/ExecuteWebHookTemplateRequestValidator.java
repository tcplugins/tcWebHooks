package webhook.teamcity.server.rest.request.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import webhook.teamcity.server.rest.model.template.TemplateTestExecutionRequest;

public class ExecuteWebHookTemplateRequestValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return TemplateTestExecutionRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		TemplateTestExecutionRequest request = (TemplateTestExecutionRequest) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "format", "format.empty");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "templateText", "templateText.empty");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "useTemplateTextForBranch", "useTemplateTextForBranch.empty");
		
		if (! request.isUseTemplateTextForBranch()) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "branchTemplateText", "branchTemplateText.empty.when.useTemplateTextForBranch.not.set");
		}
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "buildId", "buildId.empty");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "projectExternalId", "projectExternalId.empty");
		
		if (request.getWebhookId() == null || request.getWebhookId().trim().isEmpty()) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "url", "url.empty.when.webhookId.empty");
		}
		if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "webhookId", "webhookId.empty.when.url.empty");
		}
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "buildStateName", "buildStateName.empty");
		
	}

}
