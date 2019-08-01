package webhook.teamcity.payload;

import java.util.List;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.payload.template.TemplateNotFoundException;
import webhook.teamcity.payload.template.UnSupportedBuildStateException;

public class WebHookTemplateResolver {
	
	private static final String BRANCH_TYPE_BRANCH = "branch";
	private static final String BRANCH_TYPE_NON_BRANCH = "nonBranch";
	private final WebHookTemplateManager webHookTemplateManager;
	protected final WebHookPayloadManager payloadManager;

	public WebHookTemplateResolver(WebHookTemplateManager webHookTemplateManager, WebHookPayloadManager payloadManager) {
		this.webHookTemplateManager = webHookTemplateManager;
		this.payloadManager = payloadManager;
		
	}
	
	public WebHookPayload getTemplatePayloadFormat(String templateId) {
		return this.payloadManager.getFormat(this.webHookTemplateManager.getTemplateConfig(templateId, TemplateState.BEST).getFormat());
	}
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SBuildType buildType, String templateName){
		// TODO: This needs to be more build aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForBuild(buildType)){
			if (template.getTemplateId().equals(templateName)){
				return template.getTemplateForState(state);
			}
		}
		throw new TemplateNotFoundException(state, buildType, templateName, BRANCH_TYPE_NON_BRANCH);
	}
	public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SBuildType buildType, String templateName){
		// TODO: This needs to be more build aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForBuild(buildType)){
			if (template.getTemplateId().equals(templateName)){
				return template.getBranchTemplateForState(state);
			}
		}
		throw new TemplateNotFoundException(state, buildType, templateName, BRANCH_TYPE_BRANCH);
	}
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SProject project, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.getTemplateId().equals(templateName)){
				return template.getTemplateForState(state);
			}
		}
		throw new TemplateNotFoundException(state, project, templateName, BRANCH_TYPE_NON_BRANCH);
	}
	
	public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateId().equals(templateName)){
				return template.getBranchTemplateForState(state);
			}
		}
		throw new TemplateNotFoundException(state, project, templateName, BRANCH_TYPE_BRANCH);
	}
	
	public WebHookTemplateContent findWebHookBranchOrNonBranchTemplate(String stateString, SProject project, String templateName){
		// TODO: This needs to be more project aware.
		String branchType = BRANCH_TYPE_NON_BRANCH;
		if (stateString.endsWith("Branch")) {
			branchType = BRANCH_TYPE_BRANCH;
			String sBuildState = stateString.substring(0,stateString.length() - "Branch".length());
			BuildStateEnum state =	BuildStateEnum.findBuildState(sBuildState);
			if (state != null){
				for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
					if (template.getTemplateId().equals(templateName)){
						return template.getBranchTemplateForState(state);
					}
				}
			}
		} else {
			BuildStateEnum state =	BuildStateEnum.findBuildState(stateString);
			if (state != null){
				for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
					if (template.getTemplateId().equals(templateName)){
						return template.getTemplateForState(state);
					}
				}
			}
		}
		throw new UnSupportedBuildStateException(BuildStateEnum.findBuildState(stateString), branchType, project.getProjectId(), templateName);
	}
	
	public List<WebHookPayloadTemplate> findWebHookTemplatesForBuild(SBuildType buildTypeId){
		// TODO: This needs to be more build aware.
		return webHookTemplateManager.getRegisteredTemplates();
	}
	
	
	public List<WebHookPayloadTemplate> findWebHookTemplatesForProject(SProject projectId){
		// TODO: This needs to be more project aware.
		return webHookTemplateManager.getRegisteredTemplates();
	}
	
	public boolean templateIsValid(SProject project, String webhookFormat, String templateName){
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateId().equalsIgnoreCase(templateName)){
				return true;
			}
		}
		return false;
	}
	
	public boolean templateNonBranchSupportsState(BuildStateEnum myBuildState,
			SProject project, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.getTemplateId().equals(templateName)){
				return template.getSupportedBuildStates().contains(myBuildState);
			}
		}
		return false;
	}
	public boolean templateForBranchSupportsState(BuildStateEnum myBuildState,
			SProject project, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.getTemplateId().equals(templateName)){
				return template.getSupportedBranchBuildStates().contains(myBuildState);
			}
		}
		return false;
	}

	public boolean templateSupportsFormatAndState(BuildStateEnum myBuildState,
			SProject project, String templateName) {
		return (
				templateNonBranchSupportsState(myBuildState, project, templateName)
				&&  templateForBranchSupportsState(myBuildState, project, templateName));
	}

}
