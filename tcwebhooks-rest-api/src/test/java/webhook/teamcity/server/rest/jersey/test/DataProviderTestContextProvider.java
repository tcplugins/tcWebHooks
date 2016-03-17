package webhook.teamcity.server.rest.jersey.test;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.mockito.Mock;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.PathTransformer;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.request.Constants;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class DataProviderTestContextProvider implements InjectableProvider<Context, Type>, Injectable<DataProvider> {
  private final DataProvider dataProvider;
  @Mock SBuildServer sBuildServer;
  @Mock PermissionChecker permissionChecker;
  @Mock WebHookPayloadManager payloadManager;
  @Mock WebHookTemplateManager templateManager;
  @Mock TemplateFinder templateFinder;
  
  public DataProviderTestContextProvider() {
	  System.out.println("We are here: Trying to provide a testable DataProvider instance");
	  dataProvider = new DataProvider(sBuildServer, new TestUrlHolder(), permissionChecker, payloadManager, templateManager, templateFinder); 

  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<DataProvider> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(DataProvider.class)) {
      return this;
    }
    return null;
  }

  public DataProvider getValue() {
    return dataProvider;
  }
  
  public static class TestUrlHolder implements RootUrlHolder {
			String url = Constants.API_URL;  
			
			@Override
			public void setRootUrl(String rootUrl) {
				this.url = rootUrl;
				
			}
			
			@Override
			public String getRootUrl() {
				return url;
			}
	}  
}