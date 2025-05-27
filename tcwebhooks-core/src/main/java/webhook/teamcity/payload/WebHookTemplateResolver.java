package webhook.teamcity.payload;

import java.util.List;
import java.util.stream.Collectors;

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
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SBuildType buildType, String templateId){
		for (WebHookPayloadTemplate template : findWebHookTemplatesForBuild(buildType)){
			if (template.getTemplateId().equals(templateId)){
				return template.getTemplateForState(state);
			}
		}
		throw new TemplateNotFoundException(state, buildType, templateId, BRANCH_TYPE_NON_BRANCH);
	}
	public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SBuildType buildType, String templateId){
		for (WebHookPayloadTemplate template : findWebHookTemplatesForBuild(buildType)){
			if (template.getTemplateId().equals(templateId)){
				return template.getBranchTemplateForState(state);
			}
		}
		throw new TemplateNotFoundException(state, buildType, templateId, BRANCH_TYPE_BRANCH);
	}
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SProject project, String templateId){
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (templateId.equals(template.getTemplateId())){
				return template.getTemplateForState(state);
			}
		}
		throw new TemplateNotFoundException(state, project, templateId, BRANCH_TYPE_NON_BRANCH);
	}
	
	public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SProject project, String webhookFormat, String templateId){
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateId().equals(templateId)){
				return template.getBranchTemplateForState(state);
			}
		}
		throw new TemplateNotFoundException(state, project, templateId, BRANCH_TYPE_BRANCH);
	}
	
	public WebHookTemplateContent findWebHookBranchOrNonBranchTemplate(String stateString, SProject project, String templateId){
		String branchType = BRANCH_TYPE_NON_BRANCH;
		if (stateString.endsWith("Branch")) {
			branchType = BRANCH_TYPE_BRANCH;
			String sBuildState = stateString.substring(0,stateString.length() - "Branch".length());
			BuildStateEnum state =	BuildStateEnum.findBuildState(sBuildState);
			if (state != null){
				for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
					if (template.getTemplateId().equals(templateId)){
						return template.getBranchTemplateForState(state);
					}
				}
			}
		} else {
			BuildStateEnum state =	BuildStateEnum.findBuildState(stateString);
			if (state != null){
				for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
					if (template.getTemplateId().equals(templateId)){
						return template.getTemplateForState(state);
					}
				}
			}
		}
		throw new UnSupportedBuildStateException(BuildStateEnum.findBuildState(stateString), branchType, project.getProjectId(), templateId);
	}
	
	private List<WebHookPayloadTemplate> findWebHookTemplatesForBuild(SBuildType sBuildType){
		return findWebHookTemplatesForProject(sBuildType.getProject());
	}
	
	
	public List<WebHookPayloadTemplate> findWebHookTemplatesForProject(SProject sProject){
		List<String> projectIds = sProject.getProjectPath().stream().map(SProject::getProjectId).collect(Collectors.toList());
		return webHookTemplateManager.getRegisteredTemplates().stream().filter(template -> projectIds.contains(template.getProjectId())).collect(Collectors.toList());
	}
	
	public boolean templateIsValid(SProject project, String webhookFormat, String templateName){
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateId().equalsIgnoreCase(templateName)){
				return true;
			}
		}
		return false;
	}
	
	public boolean templateNonBranchSupportsState(BuildStateEnum myBuildState, SProject project, String templateName){
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.getTemplateId().equals(templateName)){
				return template.getSupportedBuildStates().contains(myBuildState);
			}
		}
		return false;
	}
	public boolean templateForBranchSupportsState(BuildStateEnum myBuildState, SProject project, String templateName){
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.getTemplateId().equals(templateName)){
				return template.getSupportedBranchBuildStates().contains(myBuildState);
			}
		}
		return false;
	}

	public boolean templateSupportsFormatAndState(BuildStateEnum myBuildState, SProject project, String templateName) {
		return (
				templateNonBranchSupportsState(myBuildState, project, templateName)
				&&  templateForBranchSupportsState(myBuildState, project, templateName));
	}

}
