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
		for (WebHookTemplate template :webHookTemplateManager.findAllTemplatesForFormat(webhookFormat)){
			if (template.getTemplateShortName().equals(templateName)){
				return template.getTemplateForState(state);
			}
		}
		return null;
	}
	
	public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SProject project, String webhookFormat, String templateName){
		// TODO: This needs to be more build aware.
		for (WebHookTemplate template :webHookTemplateManager.findAllTemplatesForFormat(webhookFormat)){
			if (template.getTemplateShortName().equals(templateName)){
				return template.getBranchTemplateForState(state);
			}
		}
		return null;
	}
	
	public List<WebHookTemplate> findWebHookTemplatesForBuild(String buildTypeId){
		// TODO: This needs to be more build aware.
		return webHookTemplateManager.getRegisteredTemplates();
	}
	
	
	public List<WebHookTemplate> findWebHookTemplatesForProject(String projectId){
		// TODO: This needs to be more project aware.
		return webHookTemplateManager.getRegisteredTemplates();
	}
	

}
