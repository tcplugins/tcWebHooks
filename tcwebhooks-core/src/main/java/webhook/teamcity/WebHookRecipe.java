package webhook.teamcity;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SProject;
import lombok.Data;
import webhook.WebHookProxyConfig;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.util.VariableMessageBuilder;
import webhook.teamcity.settings.WebHookConfig;

@Data
public class WebHookRecipe {
	
	WebHookConfig webHookConfig;
	//WebHookProxyConfig webHookProxyConfig;
	WebHookPayloadContent webHookPayloadContent;
	WebHookPayloadContent webHookPayloadResolvedContent;
	//WebHookTemplateContent webHookTemplateContent;
	//WebHookPayloadTemplate webHookPayloadTemplate;
	VariableMessageBuilder variableMessageBuilder;
	//BuildStateEnum buildStateEnum;
	//SBuild sBuild;
	//SProject sProject;

}
