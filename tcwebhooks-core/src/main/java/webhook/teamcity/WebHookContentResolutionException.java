package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookContentResolutionException extends RuntimeException {

	private static final int ERROR_CODE = 900;
	private static final long serialVersionUID = 2327979470726268623L;
	private final int errorCode; 

	private WebHookContentResolutionException(String message) {
		super(message);
		this.errorCode = ERROR_CODE;
	}
	
	public WebHookContentResolutionException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

}
