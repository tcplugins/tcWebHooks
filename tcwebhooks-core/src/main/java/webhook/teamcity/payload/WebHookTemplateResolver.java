package webhook.teamcity.payload;

import java.util.List;

import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import webhook.teamcity.BuildStateEnum;

public class WebHookTemplateResolver {
	
	private WebHookTemplateManager webHookTemplateManager;

	public WebHookTemplateResolver(WebHookTemplateManager webHookTemplateManager) {
		this.webHookTemplateManager = webHookTemplateManager;
	}
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SBuildType buildType, String webhookFormat, String templateName){
		// TODO: This needs to be more build aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForBuild(buildType)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getTemplateForState(state);
			}
		}
		return null;
	}
	public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SBuildType buildType, String webhookFormat, String templateName){
		// TODO: This needs to be more build aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForBuild(buildType)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getBranchTemplateForState(state);
			}
		}
		return null;
	}
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getTemplateForState(state);
			}
		}
		return null;
	}
	
	public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getBranchTemplateForState(state);
			}
		}
		return null;
	}
	
	public WebHookTemplateContent findWebHookBranchOrNonBranchTemplate(String stateString, SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more project aware.
		if (stateString.endsWith("Branch")) {
			String sBuildState = stateString.substring(0,stateString.length() - "Branch".length());
			BuildStateEnum state =	BuildStateEnum.findBuildState(sBuildState);
			if (state != null){
				for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
					if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
						return template.getBranchTemplateForState(state);
					}
				}
			}
		} else {
			BuildStateEnum state =	BuildStateEnum.findBuildState(stateString);
			if (state != null){
				for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
					if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
						return template.getTemplateForState(state);
					}
				}
			}
		}
		return null;
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
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equalsIgnoreCase(templateName)){
				return true;
			}
		}
		return false;
	}
	
	public boolean templateNonBranchSupportsFormatAndState(BuildStateEnum myBuildState,
			SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getSupportedBuildStates().contains(myBuildState);
			}
		}
		return false;
	}
	public boolean templateForBranchSupportsFormatAndState(BuildStateEnum myBuildState,
			SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookPayloadTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getSupportedBranchBuildStates().contains(myBuildState);
			}
		}
		return false;
	}

	public boolean templateSupportsFormatAndState(BuildStateEnum myBuildState,
			SProject project, String webhookFormat, String templateName) {
		return (
				templateNonBranchSupportsFormatAndState(myBuildState, project, webhookFormat, templateName)
				&&  templateForBranchSupportsFormatAndState(myBuildState, project, webhookFormat, templateName));
	}

}
