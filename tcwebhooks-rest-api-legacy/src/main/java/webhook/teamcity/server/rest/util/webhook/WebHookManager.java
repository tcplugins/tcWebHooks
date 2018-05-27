package webhook.teamcity.server.rest.util.webhook;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import webhook.teamcity.server.rest.data.WebHookFinder;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhook;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookProjectSettings;

public class WebHookManager {
	
	private final WebHookFinder webHookFinder;
	

	public WebHookManager(WebHookFinder webHookFinder) {
		this.webHookFinder = webHookFinder;
	}
	
	public ProjectWebhooks build(WebHookProjectSettings webHookProjectSettings, final String projectExternalId, final @NotNull Fields fields, @NotNull final BeanContext beanContext){
		ProjectWebhooks projectWebhooks = new ProjectWebhooks();
		projectWebhooks.setEnabled(webHookProjectSettings.isEnabled());
		projectWebhooks.setProjectId(projectExternalId);
		for (WebHookConfig config : webHookProjectSettings.getWebHooksConfigs()) {
			projectWebhooks.addWebhook(new ProjectWebhook(config, projectExternalId, fields, beanContext));
		}
		projectWebhooks.setCount(ValueWithDefault.decideIncludeByDefault(fields.isIncluded("count"), webHookProjectSettings.getWebHooksConfigs().size()));
		return projectWebhooks;
	}
	
	public ProjectWebhooks getWebHookList(final String projectExternalId, final @NotNull Fields fields, @NotNull final BeanContext beanContext) {
		WebHookProjectSettings webHookProjectSettings = webHookFinder.getWebHookProjectSettings(projectExternalId);
		return build(webHookProjectSettings, projectExternalId, fields, beanContext);
	}

}
