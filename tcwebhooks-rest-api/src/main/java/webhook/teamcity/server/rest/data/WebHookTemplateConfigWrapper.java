package webhook.teamcity.server.rest.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

@Getter @AllArgsConstructor
public class WebHookTemplateConfigWrapper {
	
	private WebHookTemplateConfig templateConfig;
	
	private String externalProjectId;
	
	private WebHookTemplateManager.TemplateState status;
	
	private WebHookTemplateStates buildStatesWithTemplate; 

}
