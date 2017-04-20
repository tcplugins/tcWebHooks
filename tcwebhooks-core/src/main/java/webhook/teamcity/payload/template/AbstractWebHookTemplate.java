package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;

public abstract class AbstractWebHookTemplate implements WebHookPayloadTemplate {
	
	protected WebHookTemplateManager manager;
	int rank = 10; // Default to 10.

	@Override
	public void setTemplateManager(WebHookTemplateManager webhookTemplateManager) {
		this.manager = webhookTemplateManager;
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	
	public void register(WebHookPayloadTemplate template){
		this.manager.registerTemplateFormatFromSpring(template);
	}

}
