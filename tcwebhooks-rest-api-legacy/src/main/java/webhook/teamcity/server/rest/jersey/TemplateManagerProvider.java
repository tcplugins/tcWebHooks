package webhook.teamcity.server.rest.jersey;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.payload.WebHookTemplateManager;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
@SuppressWarnings("squid:S1191")
public class TemplateManagerProvider implements InjectableProvider<Context, Type>, Injectable<WebHookTemplateManager> {
  private final WebHookTemplateManager templateManager;

  public TemplateManagerProvider(
			@NotNull final WebHookTemplateManager templateManager
		  ) {
	  this.templateManager = templateManager;
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookTemplateManager> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookTemplateManager.class)) {
      return this;
    }
    return null;
  }

  public WebHookTemplateManager getValue() {
    return templateManager;
  }

}