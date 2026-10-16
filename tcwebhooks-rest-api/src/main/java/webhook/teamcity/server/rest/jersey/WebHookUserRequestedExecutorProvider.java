package webhook.teamcity.server.rest.jersey;

import org.springframework.stereotype.Component;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;

@Component
public class WebHookUserRequestedExecutorProvider implements JerseyInjectableBeanProvider {
	
	private final WebHookUserRequestedExecutor myExecutor;

	public WebHookUserRequestedExecutorProvider(WebHookUserRequestedExecutor executor) {
		myExecutor = executor;
	}

	public WebHookUserRequestedExecutor getBean() {
		return this.myExecutor;
	}

	@Override
	public Class<?> getBeanClass() {
		return WebHookUserRequestedExecutor.class;
	}

}
