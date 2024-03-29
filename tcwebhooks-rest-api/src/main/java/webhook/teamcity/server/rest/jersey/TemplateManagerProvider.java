package webhook.teamcity.server.rest.jersey;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.payload.WebHookTemplateManager;


@Service
@SuppressWarnings("squid:S1191")
public class TemplateManagerProvider implements JerseyInjectableBeanProvider {
  @Override
  public Class<?> getBeanClass() {
    return WebHookTemplateManager.class;
  }
}