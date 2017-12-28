package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookExecutionException extends RuntimeException {
	
	public static final String WEBHOOK_DISABLED_INFO_MESSAGE = "WebHook disabled";
	public static final int    WEBHOOK_DISABLED_INFO_CODE = 701;
	public static final int    WEBHOOK_DISABLED_BY_FILTER_INFO_CODE = 702;
	
	public static final int    WEBHOOK_EXECUTION_ERROR_CODE = 801;
	
	public static final int    WEBHOOK_CONTENT_RESOLUTION_ERROR_CODE = 900;
	public static final int    TEMPLATE_NOT_FOUND_ERROR_CODE = 901;
	public static final int    UNSUPPORTED_BUILDSTATE_EXCEPTION_ERROR_CODE = 902;
	public static final int    UNSUPPORTED_WEBHOOK_FORMAT_EXCEPTION_ERROR_CODE = 903;
	public static final int    WEBHOOK_PAYLOAD_CONTENT_ASSEMBLY_EXCEPTION_ERROR_CODE = 904;

	public static final String WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE = "Unexpected exception. Please log a bug on GitHub tcplugins/tcWebHooks. Exception was: ";
	public static final int    WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE = 999;
	
	private static final long serialVersionUID = 2824659645558904102L;
	protected final int errorCode;
	
	
	public WebHookExecutionException(String message, Throwable throwable, int errorCode) {
		super(message, throwable);
		this.errorCode = errorCode;
	}
	public WebHookExecutionException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

}
