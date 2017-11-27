package webhook.teamcity.payload.content;

import webhook.teamcity.WebHookContentResolutionException;

public class WebHookPayloadContentAssemblyException extends WebHookContentResolutionException {

	private static final long serialVersionUID = 5186986877713082013L;

	public WebHookPayloadContentAssemblyException(String message) {
		super(message, WEBHOOK_PAYLOAD_CONTENT_ASSEMBLY_EXCEPTION_ERROR_CODE);
	}

}