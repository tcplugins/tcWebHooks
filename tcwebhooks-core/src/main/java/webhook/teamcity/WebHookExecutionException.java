package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookExecutionException extends RuntimeException {
	
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
