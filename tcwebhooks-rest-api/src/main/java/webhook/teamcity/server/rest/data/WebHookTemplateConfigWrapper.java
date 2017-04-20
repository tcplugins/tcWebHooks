package webhook.teamcity.server.rest.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

@Getter @AllArgsConstructor
public class WebHookTemplateConfigWrapper {
	
	private WebHookTemplateConfig entity;
	
	private WebHookTemplateManager.TemplateState status;

}
