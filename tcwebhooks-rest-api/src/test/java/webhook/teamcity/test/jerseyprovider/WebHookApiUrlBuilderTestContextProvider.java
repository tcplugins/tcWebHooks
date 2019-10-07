package webhook.teamcity.test.jerseyprovider;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.PathTransformer;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.request.Constants;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class WebHookApiUrlBuilderTestContextProvider implements InjectableProvider<Context, Type>, Injectable<WebHookApiUrlBuilder> {
  private final WebHookApiUrlBuilder webHookApiUrlBuilder;
  
  public WebHookApiUrlBuilderTestContextProvider() {
	  System.out.println("We are here: Trying to provide a testable WebHookApiUrlBuilder instance");
	  	webHookApiUrlBuilder = new WebHookApiUrlBuilder(new PathTransformer() {
			
			@Override
			public String transform(String path) {
				return Constants.API_URL + "/" + path;
			}
		});

  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookApiUrlBuilder> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookApiUrlBuilder.class)) {
      return this;
    }
    return null;
  }

  public WebHookApiUrlBuilder getValue() {
    return webHookApiUrlBuilder;
  }
  
}