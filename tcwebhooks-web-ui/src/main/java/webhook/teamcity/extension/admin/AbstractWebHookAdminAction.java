package webhook.teamcity.extension.admin;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.util.SessionUser;

public abstract class AbstractWebHookAdminAction {
	
	public static final String ACTION_TYPE = "action";
	public static final String UPDATE_STATISTICS = "updateStatistics";
	public static final String REFRESH_STATISTICS = "refreshStatistics";
	public static final String UPDATE_ANALYTICS = "updateAnalytics";

	public AbstractWebHookAdminAction() {
		super();
	}

	public boolean getParameterIsEnabled(HttpServletRequest request, String paramName, String errorMessage) throws UnexpectedActionException, UnexpectedActionValueException {
		String returnValue = StringUtil.nullIfEmpty(request.getParameter(paramName));
		if (returnValue == null) {
			throw new UnexpectedActionException(errorMessage);
		} else if (!returnValue.equalsIgnoreCase("enabled") && !returnValue.equalsIgnoreCase("disabled")) {
			throw new UnexpectedActionValueException(errorMessage);
		}
		return returnValue.equalsIgnoreCase("enabled");
	}
	
	public abstract String getAction();
	
	protected boolean isAdmin(HttpServletRequest request) {
		SUser user = SessionUser.getUser(request);
		return user != null && user.isSystemAdministratorRoleGranted();
	}

	public boolean canProcess(@NotNull HttpServletRequest request) {
		return getAction().equals(request.getParameter(ACTION_TYPE));
	}
	
	@SuppressWarnings("serial")
	public class UnexpectedActionException extends Exception {
		public UnexpectedActionException(String message) {
			super(message);
		}
	}
	@SuppressWarnings("serial")
	public class UnexpectedActionValueException extends Exception {
		public UnexpectedActionValueException(String message) {
			super(message);
		}
	}

}