package webhook.teamcity.server.rest.jersey;

import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectableBeanProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;

@Component
@Configuration
public class WebHookParameterStoreProvider implements JerseyInjectableBeanProvider {
	private final WebHookParameterStoreFactory myFactory;

	public WebHookParameterStoreProvider(WebHookParameterStoreFactory factory) {
		myFactory = factory;
	}

	@Bean
	public WebHookParameterStore getBean() {
		return myFactory.getWebHookParameterStore();
	}


	@Override
	public Class<?> getBeanClass() {
		return WebHookParameterStore.class;
	}
}