package webhook.teamcity.server.rest.jersey;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import webhook.teamcity.WebHookPluginDataResolver;

@Provider
public class WebHookPluginDataResolverProvider implements InjectableProvider<Context, Type>, Injectable<WebHookPluginDataResolver> {
  private final WebHookPluginDataResolver webHookPluginDataResolver;
  
  /**
   * Injected by Spring
   * @param webHookPluginDataResolver
   */
  public WebHookPluginDataResolverProvider(@NotNull final WebHookPluginDataResolver WebHookPluginDataResolver) {
	  this.webHookPluginDataResolver = WebHookPluginDataResolver;
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookPluginDataResolver> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookPluginDataResolver.class)) {
      return this;
    }
    return null;
  }

  public WebHookPluginDataResolver getValue() {
    return webHookPluginDataResolver;
  }
 
}