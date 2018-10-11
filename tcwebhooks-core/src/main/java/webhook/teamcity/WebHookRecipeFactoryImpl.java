package webhook.teamcity;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookRecipeFactoryImpl {
	
	private final ProjectManager projectManager;
	private final WebHookTemplateManager webHookTemplateManager;

	public WebHookRecipeFactoryImpl(
			ProjectManager projectManager,
			WebHookTemplateManager webHookTemplateManager
	) 
	{
		this.projectManager = projectManager;
		this.webHookTemplateManager = webHookTemplateManager;
	}
	
	WebHookRecipe build(WebHookConfig webHookConfig, SBuild sBuild) {
		
		WebHookRecipe recipe = new WebHookRecipe();
		recipe.setWebHookConfig(webHookConfig);
		//recipe.setBuildStateEnum(buildStateEnum);
		//recipe.setSBuild(sBuild);
		//recipe.setSProject(this.projectManager.findProjectById(sBuild.getProjectId()));
		//webHookConfig.getPayloadTemplate()
		recipe.setVariableMessageBuilder(variableMessageBuilder);
		//recipe.setWebHookPayloadContent(webHookPayloadContent);
		//recipe.setWebHookPayloadTemplate(webHookPayloadTemplate);
		//recipe.setWebHookProxyConfig(webHookProxyConfig);
		//recipe.setWebHookTemplateContent(webHookTemplateContent);
		
		return recipe;
	}

}
