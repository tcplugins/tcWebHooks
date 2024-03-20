package webhook.teamcity.server.rest.jersey;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.WebHookPluginDataResolver;

public class WebHookPluginDataResolverProvider implements JerseyInjectableBeanProvider {
  @Override
  public Class<?> getBeanClass() {
    return WebHookPluginDataResolver.class;
  }
}
