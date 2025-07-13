package webhook.teamcity.settings.converter;

import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.testing.model.WebHookRenderResult;

public interface WebHookUserRequestedConfigRenderer {

    WebHookRenderResult requestWebHookConfigurationAsCode(WebHookConfig webHookConfig);

}
