package webhook.teamcity.settings.project;

import java.util.Collections;
import java.util.List;

import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.exception.OperationUnsupportedException;

/**
 * An implementation of {@link WebHookParameterStore} which just throws {@link OperationUnsupportedException}.
 * This version is used for TeamCity 9 and below.
 */
public class WebHookParameterStoreNoOpImpl implements WebHookParameterStore {

	private static final String TEAMCITY_TOO_OLD_MESSAGE = "The version of TeamCity is too old to support this operation.";

	@Override
	public WebHookParameter getWebHookParameter(SProject sProject, String parameterName) {
		throw new OperationUnsupportedException(TEAMCITY_TOO_OLD_MESSAGE);
	}

	@Override
	public WebHookParameter getWebHookParameterById(SProject sProject, String parameterId) {
		throw new OperationUnsupportedException(TEAMCITY_TOO_OLD_MESSAGE);
	}

	@Override
	public WebHookParameter findWebHookParameter(SProject sProject, String parameterName) {
		throw new OperationUnsupportedException(TEAMCITY_TOO_OLD_MESSAGE);
	}

	@Override
	public List<WebHookParameter> getAllWebHookParameters(SProject sProject) {
		return Collections.emptyList();
	}

	@Override
	public List<WebHookParameter> getOwnWebHookParameters(SProject sProject) {
		return Collections.emptyList();
	}

	@Override
	public WebHookParameter addWebHookParameter(String projectInternalId, WebHookParameter webhookParameter) {
		throw new OperationUnsupportedException(TEAMCITY_TOO_OLD_MESSAGE);
	}

	@Override
	public boolean updateWebHookParameter(String projectInternalId, WebHookParameter webhookParameter, String description) {
		throw new OperationUnsupportedException(TEAMCITY_TOO_OLD_MESSAGE);
	}

	@Override
	public WebHookParameter removeWebHookParameter(String projectInternalId, WebHookParameter webhookParameter) {
		throw new OperationUnsupportedException(TEAMCITY_TOO_OLD_MESSAGE);
	}

	@Override
	public WebHookParameter removeWebHookParameter(SProject project, String name) {
		throw new OperationUnsupportedException(TEAMCITY_TOO_OLD_MESSAGE);
	}

}
