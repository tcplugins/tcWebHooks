package webhook.teamcity.test.jerseyprovider;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class MockingHttpServletRequestContextProvider implements InjectableProvider<Context, Type>, Injectable<HttpServletRequest> {
  private final HttpServletRequest request;
  
  public MockingHttpServletRequestContextProvider() {
	  request = mock(HttpServletRequest.class);
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<HttpServletRequest> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(HttpServletRequest.class)) {
      return this;
    }
    return null;
  }

  public HttpServletRequest getValue() {
    return request;
  }
  
}