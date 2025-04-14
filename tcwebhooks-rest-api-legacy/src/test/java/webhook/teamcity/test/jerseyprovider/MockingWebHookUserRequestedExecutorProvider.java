package webhook.teamcity.test.jerseyprovider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jdom.JDOMException;
import org.joda.time.LocalDateTime;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.intellij.openapi.diagnostic.Logger;
import webhook.WebHookExecutionStats;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;
import webhook.testframework.util.ConfigLoaderUtil;

@Provider
public class MockingWebHookUserRequestedExecutorProvider implements InjectableProvider<Context, Type>, Injectable<WebHookUserRequestedExecutor> {
	private static final Logger LOG = Logger.getInstance(MockingWebHookUserRequestedExecutorProvider.class.getName());
	private final WebHookUserRequestedExecutor request;

	public MockingWebHookUserRequestedExecutorProvider() {
		request = mock(WebHookUserRequestedExecutor.class);
		System.out.println("We are here: Trying to provide a testable WebHookUserRequestedExecutorProvider instance");
		when(request.requestWebHookExecution(any(WebHookTemplateExecutionRequest.class)))
				.thenReturn(getTestHistoryItem());
	}

	private WebHookHistoryItem getTestHistoryItem() {

		WebHookConfig webHookConfig = null;
		try {
			webHookConfig = ConfigLoaderUtil.getFirstWebHookInConfig(
					new File("../tcwebhooks-core/src/test/resources/project-settings-test-all-states-enabled.xml"));
		} catch (IOException | JDOMException e1) {
			LOG.debug(e1);
		}
		WebHookExecutionStats stats = new WebHookExecutionStats("http://localhost/somewhere");
		
    	stats.setRequestStarting();
        stats.setRequestCompleted(200, "OK");
        stats.setTeardownCompleted();

		return new WebHookHistoryItem("projectId", "projectName", "buildTypeId", "buildTypeName", "buildTypeExternalId",
				12345L, webHookConfig, stats, null, LocalDateTime.now(), null, true);
	}

	public ComponentScope getScope() {
		return ComponentScope.Singleton;
	}

	public Injectable<WebHookUserRequestedExecutor> getInjectable(final ComponentContext ic, final Context context,
			final Type type) {
		if (type.equals(WebHookUserRequestedExecutor.class)) {
			return this;
		}
		return null;
	}

	public WebHookUserRequestedExecutor getValue() {
		System.out.println("getValue called on MockingWebHookUserRequestedExecutorProvider");
		return request;
	}

}