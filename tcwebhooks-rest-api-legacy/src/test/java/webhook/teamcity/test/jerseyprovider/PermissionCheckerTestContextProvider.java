package webhook.teamcity.test.jerseyprovider;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.data.PermissionChecker;

import org.mockito.Mock;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class PermissionCheckerTestContextProvider implements InjectableProvider<Context, Type>, Injectable<PermissionChecker> {
  @Mock PermissionChecker permissionChecker;
  
  public PermissionCheckerTestContextProvider() {
	  System.out.println("We are here: Trying to provide a testable PermissionChecker instance");
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<PermissionChecker> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(PermissionChecker.class)) {
      return this;
    }
    return null;
  }

  public PermissionChecker getValue() {
    return permissionChecker;
  }
  
}