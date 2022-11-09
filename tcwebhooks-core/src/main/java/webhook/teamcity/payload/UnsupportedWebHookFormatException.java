package webhook.teamcity.payload;

import webhook.teamcity.WebHookContentResolutionException;

public class UnsupportedWebHookFormatException extends WebHookContentResolutionException {

	private static final long serialVersionUID = -3763862515669344112L;

	public UnsupportedWebHookFormatException(String formatName) {
		super("No WebHook format '" + formatName + "' was found", UNSUPPORTED_WEBHOOK_FORMAT_EXCEPTION_ERROR_CODE);
	}

}
