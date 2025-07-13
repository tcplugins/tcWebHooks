package webhook.teamcity.extension.util;

import java.util.Objects;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import webhook.teamcity.auth.WebHookAuthenticatorProvider;
import webhook.teamcity.extension.bean.ErrorResult;
import webhook.teamcity.json.WebHookAuthenticationJson;
import webhook.teamcity.json.WebHookConfigurationJson;
import webhook.teamcity.json.WebHookFilterJson;

public class WebHookConfigurationValidator {
	private static final String PROJECT_ID_KEY = "projectId";
	private static final String AUTHENTICATION_TYPE_KEY = "authenticationType";
	private static final String AUTHENTICATION_PARAMS_KEY = "authenticationParams";
	private final ProjectManager myProjectManager;
	private final SecurityContext mySecurityContext;
	private final WebHookAuthenticatorProvider myWebHookAuthenticatorProvider;
	
	public WebHookConfigurationValidator(ProjectManager projectManager,
			SecurityContext securityContext,
			WebHookAuthenticatorProvider webHookAuthenticatorProvider) {
		this.myProjectManager = projectManager;
		this.mySecurityContext = securityContext;
		this.myWebHookAuthenticatorProvider = webHookAuthenticatorProvider;
	}

	public ErrorResult validateNewWebHook(String projectId, WebHookConfigurationJson newWebHook, ErrorResult result) {
		
		if (newWebHook.getId() == null || !newWebHook.getId().trim().equalsIgnoreCase("_new")) {
			result.addError("id-new", "The webhook id must be _new for new WebHooks.");
		}
		
		validateStandardWebHookFields(projectId, newWebHook, result);
		
		return result;
		
	}
	
	public ErrorResult validateDeleteWebHook(String projectId, String webHookId, ErrorResult result) {
		
		if (webHookId == null) {
			result.addError("id-delete", "The webhook id is required for deletion.");
		}
		validateProjectId(projectId, result);
		return result;
		
	}

	public ErrorResult validateUpdatedWebHook(String externalId, WebHookConfigurationJson updatedWebHook, ErrorResult result) {
		
		if (updatedWebHook.getId() == null || updatedWebHook.getId().trim().isEmpty()) {
			result.addError("id-empty", "The webhook id must not be empty.");
		}
		validateStandardWebHookFields(externalId, updatedWebHook, result);

		return result;
	}
	public ErrorResult validateStandardWebHookFields(String externalId, WebHookConfigurationJson updatedWebHook, ErrorResult result) {
		
		if (updatedWebHook.getUrl() == null || updatedWebHook.getUrl().trim().isEmpty()) {
			result.addError("url", "The URL cannot be empty.");
		}
		
		if (updatedWebHook.getTemplate() == null || updatedWebHook.getTemplate().trim().isEmpty()) {
			result.addError("template", "The webhook template cannot be empty.");
		}
		
		if (updatedWebHook.getTemplate() == null || updatedWebHook.getTemplate().trim().isEmpty()) {
			result.addError("template", "The webhook template cannot be empty.");
		}
		
		if (updatedWebHook.getEnabled() == null) {
			result.addError("enabled", "The 'enabled' flag cannot be empty and must be 'true' or 'false'.");
		}
		
		validateProjectId(externalId, result);
		validateAuthentication(updatedWebHook.getAuthentication(), result);
		
		
		if (Objects.nonNull(updatedWebHook.getFilters())) {
			for (WebHookFilterJson.Filter f : updatedWebHook.getFilters().getFilter()) {
				validateFilter(f, result);
			}
		}
		
		return result;
	}
	private ErrorResult validateProjectId(String projectExternalId, ErrorResult result) {
		if (projectExternalId != null && !projectExternalId.isEmpty()) {
			SProject sProject = null;
			try {
				sProject = myProjectManager.findProjectByExternalId(projectExternalId);
				
			} catch (AccessDeniedException ex) {
				result.addError(PROJECT_ID_KEY, "The TeamCity project is not visible to your user");
			}
			if (sProject == null) {
				result.addError(PROJECT_ID_KEY, "The projectId must refer to a valid TeamCity project");
			}
			if (sProject != null && !mySecurityContext.getAuthorityHolder().isPermissionGrantedForProject(sProject.getProjectId(), Permission.EDIT_PROJECT)) {
				result.addError(PROJECT_ID_KEY, "The TeamCity project is not writable by your user");
			}
		} else {
			result.addError(PROJECT_ID_KEY, "The projectId cannot be empty");
		}
		return result;
	}
	
	private ErrorResult validateAuthentication(WebHookAuthenticationJson projectWebHookAuthConfig, ErrorResult result) {
		if (projectWebHookAuthConfig != null) {
			if (!this.myWebHookAuthenticatorProvider.getRegisteredTypes().contains(projectWebHookAuthConfig.getType())) {
				result.addError(AUTHENTICATION_TYPE_KEY, "The authentication type is unknown");
			}
			
			if (!this.myWebHookAuthenticatorProvider.areAllRequiredParametersPresent(projectWebHookAuthConfig.toWebHookAuthConfig())) {
				result.addError(AUTHENTICATION_PARAMS_KEY, "The authentication configuration is missing required parameters");
			}
			
		}
		return result;
	}
	
	public static ErrorResult validateNewFilter(WebHookFilterJson.Filter newFilter, ErrorResult result) {
		
		if (newFilter.getId() != null) {
			result.addError("id-empty", "The filter id must be empty. It will be generated by Teamcity");
		}

		return validateFilter(newFilter, result);
	}

	public static ErrorResult validateFilter(WebHookFilterJson.Filter updatedFilter, ErrorResult result) {

		if (updatedFilter.getValue() == null || updatedFilter.getValue().trim().isEmpty()) {
			result.addError("value", "The filter value cannot be empty.");
		}
		if (updatedFilter.getRegex() == null || updatedFilter.getRegex().trim().isEmpty()) {
			result.addError("regex", "The filter regex cannot be empty.");
		}
		if (updatedFilter.getEnabled() == null) {
			result.addError("filter", "The filter enabled cannot be empty.");
		}
		
		return result;
	}
}
