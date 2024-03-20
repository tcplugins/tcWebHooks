package webhook.teamcity.server.rest.jersey;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import org.springframework.stereotype.Service;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;

@Service
public class WebHookParameterStoreFactoryProvider implements JerseyInjectableBeanProvider {
    @Override
    public Class<?> getBeanClass() {
        return WebHookParameterStoreFactory.class;
    }
}
