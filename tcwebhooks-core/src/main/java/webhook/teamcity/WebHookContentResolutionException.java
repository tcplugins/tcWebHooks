package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookContentResolutionException extends WebHookExecutionException {

	private static final long serialVersionUID = 2327979470726268623L;
	public WebHookContentResolutionException(String message) {
		super(message, WEBHOOK_CONTENT_RESOLUTION_ERROR_CODE);
	}
	
	public WebHookContentResolutionException(String message, int errorCode) {
		super(message, errorCode);
	}

}
