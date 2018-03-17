package webhook.teamcity.testing;

public class WebHookConfigNotFoundException extends Exception {
	
	private static final long serialVersionUID = 1933172873766920599L;

	public WebHookConfigNotFoundException(String message, Exception ex) {
		super(message, ex);
	}
	
	public WebHookConfigNotFoundException(String message) {
		super(message);
	}

}
