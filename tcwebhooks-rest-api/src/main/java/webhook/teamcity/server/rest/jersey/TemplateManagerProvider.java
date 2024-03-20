package webhook.teamcity.server.rest.jersey;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.payload.WebHookTemplateManager;


@SuppressWarnings("squid:S1191")
public class TemplateManagerProvider implements JerseyInjectableBeanProvider {
  @Override
  public Class<?> getBeanClass() {
    return WebHookTemplateManager.class;
  }
}