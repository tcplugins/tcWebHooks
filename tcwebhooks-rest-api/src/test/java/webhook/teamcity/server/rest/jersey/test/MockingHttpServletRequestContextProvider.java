package webhook.teamcity.server.rest.jersey.test;

import static org.mockito.Mockito.mock;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.server.rest.util.mainconfig.MainConfigManager;
import webhook.teamcity.settings.WebHookMainSettings;

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