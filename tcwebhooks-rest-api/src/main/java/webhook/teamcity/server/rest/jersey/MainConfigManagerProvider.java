package webhook.teamcity.server.rest.jersey;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import webhook.teamcity.server.rest.util.mainconfig.MainConfigManager;

@Provider
public class MainConfigManagerProvider implements InjectableProvider<Context, Type>, Injectable<MainConfigManager> {
  private final MainConfigManager mainConfigManager;

  public MainConfigManagerProvider(final MainConfigManager manager) {
	  mainConfigManager = manager;
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<MainConfigManager> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(MainConfigManager.class)) {
      return this;
    }
    return null;
  }

  public MainConfigManager getValue() {
    return mainConfigManager;
  }
}