package webhook.teamcity.payload.template;

import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;

public abstract class AbstractWebHookTemplate implements WebHookTemplate {

    protected WebHookTemplateManager manager;
    int rank = 10; // Default to 10.

    @Override
    public void setTemplateManager(WebHookTemplateManager webhookTemplateManager) {
        this.manager = webhookTemplateManager;
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    @Override
    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public void register(WebHookTemplate template) {
        this.manager.registerTemplateFormatFromSpring(template);
    }

}
