package webhook.teamcity.server.rest.jersey;

import javax.ws.rs.ext.Provider;
import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.settings.WebHookSettingsManager;

public class WebHookSettingsManagerProvider implements JerseyInjectableBeanProvider {
  @Override
  public Class<?> getBeanClass() {
    return WebHookSettingsManager.class;
  }
}
