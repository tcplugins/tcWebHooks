package webhook.teamcity.payload.content;

import webhook.teamcity.WebHookContentResolutionException;

public class WebHookPayloadContentAssemblyException extends WebHookContentResolutionException {

	private static final int ERROR_CODE = 904;
	private static final long serialVersionUID = 1L;

	public WebHookPayloadContentAssemblyException(String message) {
		super(message, ERROR_CODE);
	}

}