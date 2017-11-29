package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookHttpExecutionException extends WebHookExecutionException {
	
	private static final long serialVersionUID = 2824659645558904102L;
	public WebHookHttpExecutionException(String message) {
		super(message, WEBHOOK_EXECUTION_ERROR_CODE);
	}
	public WebHookHttpExecutionException(String message, Throwable throwable) {
		super(message, throwable, WEBHOOK_EXECUTION_ERROR_CODE);
	}
	public WebHookHttpExecutionException(String message, int errorCode) {
		super(message, errorCode);
	}

}
