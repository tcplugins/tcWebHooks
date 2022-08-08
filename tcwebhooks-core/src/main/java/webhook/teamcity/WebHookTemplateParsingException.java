package webhook.teamcity;

import lombok.Getter;

@Getter
public class WebHookTemplateParsingException extends WebHookContentResolutionException {

	private static final long serialVersionUID = -3373028723068101280L;

	public WebHookTemplateParsingException(String message) {
		super(message, WEBHOOK_TEMPLATE_PARSING_EXCEPTION_ERROR_CODE);
	}
	
	public WebHookTemplateParsingException(String message, int errorCode) {
		super(message, errorCode);
	}

}
