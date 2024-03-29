package webhook.teamcity.server.rest.jersey;

import org.springframework.stereotype.Service;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.WebHookPluginDataResolver;

@Service
public class WebHookPluginDataResolverProvider implements JerseyInjectableBeanProvider {
  @Override
  public Class<?> getBeanClass() {
    return WebHookPluginDataResolver.class;
  }
}
