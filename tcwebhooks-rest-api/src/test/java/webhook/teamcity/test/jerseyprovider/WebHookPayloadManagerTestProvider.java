package webhook.teamcity.test.jerseyprovider;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.springframework.web.context.ContextLoader;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import webhook.teamcity.payload.WebHookPayloadManager;

@Provider
public class WebHookPayloadManagerTestProvider
		implements InjectableProvider<Context, Type>, Injectable<WebHookPayloadManager> {
	private WebHookPayloadManager webHookPayloadManager;

	public WebHookPayloadManagerTestProvider() {
		System.out.println("We are here: Trying to provide a testable WebHookPayloadManager instance");

	}

	public ComponentScope getScope() {
		return ComponentScope.Singleton;
	}

	public Injectable<WebHookPayloadManager> getInjectable(final ComponentContext ic, final Context context,
			final Type type) {
		if (type.equals(WebHookPayloadManager.class)) {
			System.out.println("WebHookPayloadManagerTestProvider: Providing injectable");
			return this;
		}
		return null;
	}

	@Override
	public WebHookPayloadManager getValue() {
		System.out.println("WebHookPayloadManagerTestProvider: Providing value " + webHookPayloadManager.toString());
		webHookPayloadManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookPayloadManager.class);
		return webHookPayloadManager;
	}

}