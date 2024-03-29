package webhook.teamcity.server.rest.jersey;

import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Service;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;

@Service
@SuppressWarnings("squid:S1191")
public class WebHookUserRequestedExecutorProvider implements JerseyInjectableBeanProvider {

  @Override
  public Class<?> getBeanClass() {
    return WebHookUserRequestedExecutor.class;
  }
}
