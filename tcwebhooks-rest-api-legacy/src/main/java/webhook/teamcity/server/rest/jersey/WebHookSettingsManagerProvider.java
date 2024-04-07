package webhook.teamcity.server.rest.jersey;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import webhook.teamcity.settings.WebHookSettingsManager;

@Provider
public class WebHookSettingsManagerProvider implements InjectableProvider<Context, Type>, Injectable<WebHookSettingsManager> {
  private final WebHookSettingsManager webHookSettingsManager;
  
  /**
   * Injected by Spring
   * @param webHookSettingsManager
   */
  public WebHookSettingsManagerProvider(
			@NotNull final WebHookSettingsManager webHookSettingsManager
		  ) {
	  this.webHookSettingsManager = webHookSettingsManager;
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookSettingsManager> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookSettingsManager.class)) {
      return this;
    }
    return null;
  }

  public WebHookSettingsManager getValue() {
    return webHookSettingsManager;
  }
 
}