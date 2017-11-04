package webhook.teamcity.server.rest.errors;

import webhook.teamcity.server.rest.model.template.ErrorResult;

public class BadRequestException extends RuntimeException {
	private static final long serialVersionUID = 8664310771373654913L;
	private ErrorResult result;

	public BadRequestException(String message) {
		super(message);
	}
	
	public BadRequestException(String message, ErrorResult result) {
		super(message);
		this.result = result;
	}

	public BadRequestException(String message, Throwable cause, ErrorResult result) {
		super(message, cause);
		this.result = result;
	}

	public ErrorResult getResult() {
		return result;
	}
}