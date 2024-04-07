package webhook.teamcity.server.rest.errors;

import webhook.teamcity.server.rest.model.template.ErrorResult;

public class UnprocessableEntityException extends RuntimeException {
	private static final long serialVersionUID = 8664310771373654913L;
	private final ErrorResult result;

	public UnprocessableEntityException(String message, ErrorResult result) {
		super(message);
		this.result = result;
	}

	public UnprocessableEntityException(String message, Throwable cause, ErrorResult result) {
		super(message, cause);
		this.result = result;
	}

	public ErrorResult getResult() {
		return result;
	}
}