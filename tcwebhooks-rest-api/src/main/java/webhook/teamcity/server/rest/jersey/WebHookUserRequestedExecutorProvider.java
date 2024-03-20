package webhook.teamcity.server.rest.jersey;

import javax.ws.rs.ext.Provider;
import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;

@SuppressWarnings("squid:S1191")
public class WebHookUserRequestedExecutorProvider implements JerseyInjectableBeanProvider {

  @Override
  public Class<?> getBeanClass() {
    return WebHookUserRequestedExecutor.class;
  }
}
