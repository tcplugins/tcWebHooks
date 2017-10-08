package webhook.teamcity.server.rest.data;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateBranchText;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateState;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateText;

@Getter @AllArgsConstructor
public class WebHookTemplateItemConfigWrapper {
	
	private WebHookTemplateItemRest templateItem;
	private WebHookTemplateStates buildStatesWithTemplate;
	
	public WebHookTemplateItemConfigWrapper(WebHookTemplateItem item, WebHookTemplateStates buildStatesWithTemplate) {
		this.templateItem = new WebHookTemplateItemRest(item);
		this.buildStatesWithTemplate = buildStatesWithTemplate;
	}

	@Getter @AllArgsConstructor
	public static class WebHookTemplateItemRest {
	
		WebHookTemplateText templateText;
		WebHookTemplateBranchText branchTemplateText;
		String id;
		List<WebHookTemplateState> states = new ArrayList<>();
		
		public WebHookTemplateItemRest(WebHookTemplateItem webHookTemplateItem) {
			this.templateText = webHookTemplateItem.getTemplateText();
			this.branchTemplateText = webHookTemplateItem.getBranchTemplateText();
			this.id = webHookTemplateItem.getId().toString();
			this.states.addAll(webHookTemplateItem.getStates());
		}
		
	}
}
