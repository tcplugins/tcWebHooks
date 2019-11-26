package webhook.teamcity.server.rest.data;

import java.util.regex.Pattern;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.template.Template.TemplateItem;
import webhook.teamcity.server.rest.model.template.Template.WebHookTemplateStateRest;
import webhook.teamcity.server.rest.model.template.ErrorResult;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

public class TemplateValidator {
	private static final String PROJECT_ID_KEY = "projectId";
	private final WebHookTemplateManager myTemplateManager;
	private final PermissionChecker myPermissionChecker;
	private final ProjectManager myProjectManager;
	
	public TemplateValidator(WebHookTemplateManager templateManager, PermissionChecker permissionChecker,
			ProjectManager projectManager) {
		this.myTemplateManager = templateManager;
		this.myPermissionChecker = permissionChecker;
		this.myProjectManager = projectManager;
	}

	public ErrorResult validateNewTemplate(String projectId, Template requestTemplate, ErrorResult result) {
		
		if (requestTemplate.id == null || requestTemplate.id.trim().isEmpty()) {
			result.addError("id-empty", "The template id cannot be empty. It is used to identify the template and is referenced by webhook configuration");
		}
		
		if (requestTemplate.id != null && ! Pattern.matches("^[A-Za-z0-9_.-]+$", requestTemplate.id) ) {
			result.addError("id-name", "The template id can only be 'A-Za-z0-9_.-'. It is used to identify the template and is referenced by webhook configuration");
		}
		
		if (requestTemplate.format == null || requestTemplate.format.trim().isEmpty()) {
			result.addError("format", "The template format cannot be empty.");
		}
		
		if (requestTemplate.rank == null || requestTemplate.rank < 0 || requestTemplate.rank > 1000) {
			result.addError("rank", "The template rank cannot be empty and must be between 0 and 1000.");
		}
		
		validateProjectId(projectId, result);
		validateTemplateIdAndProjectId(projectId, requestTemplate, result);
		
		if (requestTemplate.defaultTemplate != null) {
			validateDefaultTemplateItem(requestTemplate.defaultTemplate, result);
		}
		
		if (requestTemplate.getTemplates() != null) {
			for (TemplateItem templateItem : requestTemplate.getTemplates()) {
				validateTemplateItem(templateItem, templateItem, result);
			}
		}
		return result;
		
	}
	
	private ErrorResult validateProjectId(String projectId, ErrorResult result) {
		if (projectId != null && !projectId.isEmpty()) {
			SProject sProject = null;
			try {
				sProject = myProjectManager.findProjectByExternalId(projectId);
				
			} catch (AccessDeniedException ex) {
				result.addError(PROJECT_ID_KEY, "The TeamCity project is not visible to your user");
			}
			if (sProject == null) {
				result.addError(PROJECT_ID_KEY, "The projectId must refer to a valid TeamCity project");
			} else {
				if (! myPermissionChecker.isPermissionGranted(Permission.EDIT_PROJECT, sProject.getProjectId())) {
					result.addError(PROJECT_ID_KEY, "The TeamCity project is not writable by your user");
				}
			}
		} else {
			result.addError(PROJECT_ID_KEY, "The projectId cannot be empty");
		}
		return result;
	}
	
	private ErrorResult validateTemplateIdAndProjectId(String projectId, Template requestTemplate, ErrorResult result) {
		if (!result.isErrored()) { // Skip if we already have errors.
			WebHookPayloadTemplate template = myTemplateManager.getTemplate(requestTemplate.id);
			if (template != null) {
				SProject sProject = null;
				try {
					if (projectId != null ) {
						sProject = myProjectManager.findProjectById(projectId); 
					} else {
						sProject = myProjectManager.findProjectById("_Root");
					}
					result.addError("id-duplicate", "The template id is in use by another template registered to a project with id '" + sProject.getExternalId() + "'");
				} catch (AccessDeniedException ex) {
					result.addError("id-duplicate", "The template id is in use by another template registered to a project that you're not permissioned to view");
				}
			}
		}
		return result;
	}
	
	public ErrorResult validateTemplate(WebHookTemplateConfig webHookTemplateConfig, Template requestTemplate, ErrorResult result) {
		
		if ( ! webHookTemplateConfig.getId().equals(requestTemplate.id)) {
			result.addError("id", "Sorry, it's not possible to change the id of an existing template. Please create a new template (or a copy) with a new id and delete this one.");
			
		}
		
		if (requestTemplate.defaultTemplate != null) {
			result.addError("defaultTemplate", "Sorry, it's not possible to update templateItems when updating a template. Please update the templateItem specifically.");
		}
		
		  if (requestTemplate.getTemplates() != null) {
			result.addError("templateItem", "Sorry, it's not possible to update templateItems when updating a template. Please update the templateItem specifically.");
		}

		return result;
	}
	
	public ErrorResult validateTemplateItem(TemplateItem templateItem, TemplateItem requestTemplateItem, ErrorResult result) {
		if (!"_new".equals(requestTemplateItem.getId()) && !templateItem.getId().equals(requestTemplateItem.getId())) {
			result.addError("id", "The id field must match the existing one.");
		}

		validateTemplateText(requestTemplateItem, result);
		
		for (WebHookTemplateStateRest requestItemState : requestTemplateItem.getBuildStates()) {
			if (BuildStateEnum.findBuildState(requestItemState.getType()) == null){ 
				result.addError(requestItemState.getType(), requestItemState.getType() + " is an not a valid buildState");
			}
		}
		
		for (WebHookTemplateStateRest itemState : templateItem.getBuildStates()) {
			WebHookTemplateStateRest requestItemState = requestTemplateItem.findConfigForBuildState(itemState.getType());
				
			if (requestItemState != null && itemState.isEnabled() != requestItemState.isEnabled() && !Boolean.TRUE.equals(itemState.getEditable())) { 
				result.addError(itemState.getType(), itemState.getType() + " is not editable for this templateItem");						
			}
		}
		return result;
	}

	public ErrorResult validateDefaultTemplateItem(TemplateItem requestTemplateItem, ErrorResult result) {
		return validateTemplateText(requestTemplateItem, result);
	}

	private ErrorResult validateTemplateText(TemplateItem requestTemplateItem, ErrorResult result) {
		if (requestTemplateItem.getTemplateText().getContent() == null 
				|| requestTemplateItem.getTemplateText().getContent().trim().isEmpty()) {
			result.addError("templateText", "The template text content must not be null or empty.");
		}
		
		if (! requestTemplateItem.getTemplateText().getUseTemplateTextForBranch()
				&& (
						requestTemplateItem.getBranchTemplateText().getContent() == null 
					||  requestTemplateItem.getBranchTemplateText().getContent().trim().isEmpty()
					)
			) {
			result.addError("branchTemplateText", "The branch template text content must not be null or empty if 'useTemplateTextForBranch' is false.");
		}
		return result;
	}

	public ErrorResult validateNewTemplate(Template newTemplate, ErrorResult errorResult) {
		return validateNewTemplate(WebHookPayloadTemplate.DEFAULT_TEMPLATE_PROJECT_ID, newTemplate, errorResult);
	}
}
