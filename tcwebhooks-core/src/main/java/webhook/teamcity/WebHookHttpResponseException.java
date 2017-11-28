package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookHttpResponseException extends WebHookExecutionException {
	
	private static final long serialVersionUID = 2824659645558904102L;
	
	public WebHookHttpResponseException(String message, int errorCode) {
		super(message, errorCode);
	}

}
