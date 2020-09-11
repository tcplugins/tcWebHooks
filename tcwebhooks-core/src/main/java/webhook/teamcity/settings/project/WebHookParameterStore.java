package webhook.teamcity.settings.project;

import java.util.List;

import javax.annotation.Nullable;

import jetbrains.buildServer.serverSide.SProject;

public interface WebHookParameterStore {
	
	/**
	 * Get a parameter from this specified project. Will return null if not defined in project 
	 * @param sProject
	 * @param parameterName
	 * @return {@link WebHookParameter}
	 */
	@Nullable
	public WebHookParameter getWebHookParameter(SProject sProject, String parameterName);
	@Nullable
	public WebHookParameter getWebHookParameterById(SProject sProject, String parameterId);
	@Nullable
	public WebHookParameter findWebHookParameter(SProject sProject, String parameterName);
	public List<WebHookParameter> getAllWebHookParameters(SProject sProject);
	public List<WebHookParameter> getOwnWebHookParameters(SProject sProject);
	
	public WebHookParameter addWebHookParameter(String projectInternalId, WebHookParameter webhookParameter);
	public boolean updateWebHookParameter(String projectInternalId, WebHookParameter webhookParameter, String description);
	public WebHookParameter removeWebHookParameter(String projectInternalId, WebHookParameter webhookParameter);
	public WebHookParameter removeWebHookParameter(SProject project, String name);

}
