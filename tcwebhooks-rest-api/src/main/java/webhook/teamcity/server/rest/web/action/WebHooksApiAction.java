package webhook.teamcity.server.rest.web.action;

import static webhook.teamcity.server.rest.web.WebHookRestApiActionController.*;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.util.StringUtil;

public abstract class WebHooksApiAction {

	public WebHooksApiAction() {
		super();
	}

	public String getParameterAsStringOrNull(HttpServletRequest request, String paramName, String errorMessage) throws MissingPathException {
		String returnValue = StringUtil.nullIfEmpty(request.getParameter(paramName));
		if (returnValue == null || "".equals(returnValue.trim())) {
			throw new MissingPathException(errorMessage);
		}
		return returnValue;
	}
	
	public abstract String getApiAction();

	public boolean canProcess(@NotNull HttpServletRequest request) {
		return getApiAction().equals(request.getParameter(ACTION_TYPE));
	}
	
	@SuppressWarnings("serial")
	public class MissingPathException extends Exception {
		public MissingPathException(String message) {
			super(message);
		}
	}

}