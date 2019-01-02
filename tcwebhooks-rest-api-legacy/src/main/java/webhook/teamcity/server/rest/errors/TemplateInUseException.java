package webhook.teamcity.server.rest.errors;

import webhook.teamcity.server.rest.model.template.ErrorResult;

public class TemplateInUseException extends RuntimeException {

	private static final long serialVersionUID = 1062265324610559830L;
	private final ErrorResult result;

	public TemplateInUseException(String message, ErrorResult result) {
		super(message);
		this.result = result;
	}

	public TemplateInUseException(String message, Throwable cause, ErrorResult result) {
		super(message, cause);
		this.result = result;
	}

	public ErrorResult getResult() {
		return result;
	}

}
