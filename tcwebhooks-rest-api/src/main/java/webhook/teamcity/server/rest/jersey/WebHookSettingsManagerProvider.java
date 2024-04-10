package webhook.teamcity.server.rest.jersey;

import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Service;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.settings.WebHookSettingsManager;

@Service
public class WebHookSettingsManagerProvider implements JerseyInjectableBeanProvider {
  @Override
  public Class<?> getBeanClass() {
    return WebHookSettingsManager.class;
  }
}
