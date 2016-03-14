package webhook.teamcity.server.rest.jersey.test;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.ServiceLocator;

import org.mockito.Mock;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class ServiceLocatorTestContextProvider implements InjectableProvider<Context, Type>, Injectable<ServiceLocator> {
  @Mock private ServiceLocator serviceLocator;
  
  public ServiceLocatorTestContextProvider() {
	  System.out.println("We are here: Trying to provide a testable ServiceLocator instance");
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<ServiceLocator> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(ServiceLocator.class)) {
      return this;
    }
    return null;
  }

  public ServiceLocator getValue() {
    return serviceLocator;
  }
  
}