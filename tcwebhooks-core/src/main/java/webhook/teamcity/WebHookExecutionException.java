package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookExecutionException extends RuntimeException {
	
	public static final int WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE = 900;
	public static final String WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE = "Unexpected exception. Please log a bug on GitHub tcplugins/tcWebHooks. Exception was: ";

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
