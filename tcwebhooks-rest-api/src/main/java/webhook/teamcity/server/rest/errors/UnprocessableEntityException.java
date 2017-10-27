package webhook.teamcity.server.rest.errors;

import webhook.teamcity.server.rest.model.template.TemplateValidationResult;

public class UnprocessableEntityException extends RuntimeException {
	private static final long serialVersionUID = 8664310771373654913L;
	private TemplateValidationResult result;

	public UnprocessableEntityException(String message, TemplateValidationResult result) {
		super(message);
		this.result = result;
	}

	public UnprocessableEntityException(String message, Throwable cause, TemplateValidationResult result) {
		super(message, cause);
		this.result = result;
	}

	public TemplateValidationResult getResult() {
		return result;
	}
}