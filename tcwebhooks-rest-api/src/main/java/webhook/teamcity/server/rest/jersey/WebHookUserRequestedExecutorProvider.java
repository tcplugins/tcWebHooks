package webhook.teamcity.server.rest.jersey;

import org.springframework.stereotype.Service;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;

@Service
public class WebHookUserRequestedExecutorProvider implements JerseyInjectableBeanProvider {
	
	@Override
	public Class<?> getBeanClass() {
		return WebHookUserRequestedExecutor.class;
	}

}
