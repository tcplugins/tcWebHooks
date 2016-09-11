package webhook.teamcity.server.rest.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

@Getter @AllArgsConstructor
public class WebHookTemplateEntityWrapper {
	
	private WebHookTemplateEntity entity;
	
	private WebHookTemplateManager.TemplateState status;

}
