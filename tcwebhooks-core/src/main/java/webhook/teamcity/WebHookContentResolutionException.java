package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookContentResolutionException extends RuntimeException {

	private static final long serialVersionUID = 2327979470726268623L;
	protected static final int WEBHOOK_CONTENT_RESOLUTION_ERROR_CODE = 900;
	protected static final int TEMPLATE_NOT_FOUND_ERROR_CODE=901;
	protected static final int UNSUPPORTED_BUILDSTATE_EXCEPTION_ERROR_CODE=902;
	protected static final int UNSUPPORTED_WEBHOOK_FORMAT_EXCEPTION_ERROR_CODE=903;
	protected static final int WEBHOOK_PAYLOAD_CONTENT_ASSEMBLY_EXCEPTION_ERROR_CODE=904;
	
	protected final int errorCode;

	public WebHookContentResolutionException(String message) {
		super(message);
		this.errorCode = WEBHOOK_CONTENT_RESOLUTION_ERROR_CODE;
	}
	
	public WebHookContentResolutionException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

}
