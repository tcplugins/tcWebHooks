package webhook.teamcity.server.rest.jersey;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import webhook.teamcity.testing.WebHookUserRequestedExecutor;

@Provider
public class WebHookUserRequestedExecutorProvider implements InjectableProvider<Context, Type>, Injectable<WebHookUserRequestedExecutor> {
  private final WebHookUserRequestedExecutor webHookUserRequestedExecutor;
  
  /**
   * Injected by Spring
   * @param webHookUserRequestedExecutor
   */
  public WebHookUserRequestedExecutorProvider(
			@NotNull final WebHookUserRequestedExecutor webHookUserRequestedExecutor
		  ) {
	  this.webHookUserRequestedExecutor = webHookUserRequestedExecutor;
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookUserRequestedExecutor> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookUserRequestedExecutor.class)) {
      return this;
    }
    return null;
  }

  public WebHookUserRequestedExecutor getValue() {
    return webHookUserRequestedExecutor;
  }
 
}