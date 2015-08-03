package webhook.teamcity.payload;

import java.util.List;

import webhook.teamcity.BuildStateEnum;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

public class WebHookTemplateResolver {
	
	private WebHookTemplateManager webHookTemplateManager;
	private SBuildServer server;

	public WebHookTemplateResolver(SBuildServer server, WebHookTemplateManager webHookTemplateManager) {
		this.server = server;
		this.webHookTemplateManager = webHookTemplateManager;
	}
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SBuildType buildType, String webhookFormat, String templateName){
		// TODO: This needs to be more build aware.
		for (WebHookTemplate template : findWebHookTemplatesForBuild(buildType)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getTemplateForState(state);
			}
		}
		return null;
	}
	public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SBuildType buildType, String webhookFormat, String templateName){
		// TODO: This needs to be more build aware.
		for (WebHookTemplate template : findWebHookTemplatesForBuild(buildType)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getBranchTemplateForState(state);
			}
		}
		return null;
	}
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getTemplateForState(state);
			}
		}
		return null;
	}
	
	public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more project aware.
		for (WebHookTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equals(templateName)){
				return template.getBranchTemplateForState(state);
			}
		}
		return null;
	}
	
	public List<WebHookTemplate> findWebHookTemplatesForBuild(SBuildType buildTypeId){
		// TODO: This needs to be more build aware.
		return webHookTemplateManager.getRegisteredTemplates();
	}
	
	
	public List<WebHookTemplate> findWebHookTemplatesForProject(SProject projectId){
		// TODO: This needs to be more project aware.
		return webHookTemplateManager.getRegisteredTemplates();
	}
	
	public boolean templateIsValid(SProject project, String webhookFormat, String templateName){
		for (WebHookTemplate template : findWebHookTemplatesForProject(project)){
			if (template.supportsPayloadFormat(webhookFormat) && template.getTemplateShortName().equalsIgnoreCase(templateName)){
				return true;
			}
		}
		return false;
	}

}
