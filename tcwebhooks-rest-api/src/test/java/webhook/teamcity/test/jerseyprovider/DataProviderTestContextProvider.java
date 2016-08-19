package webhook.teamcity.test.jerseyprovider;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import javax.security.sasl.SaslServer;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jetbrains.annotations.NotNull;
import org.mockito.Mock;
import org.springframework.web.context.ContextLoader;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.PathTransformer;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.request.Constants;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class DataProviderTestContextProvider implements InjectableProvider<Context, Type>, Injectable<DataProvider> {
  private DataProvider dataProvider;
  private final SBuildServer sBuildServer;
  private final PermissionChecker permissionChecker;
  private TemplateFinder templateFinder;
  WebHookPayloadManager payloadManager;
  @Context WebHookTemplateManager templateManager;
  
  public DataProviderTestContextProvider() {
	  System.out.println("We are here: Trying to provide a testable DataProvider instance");
	  sBuildServer = mock(SBuildServer.class);
	  permissionChecker = mock(PermissionChecker.class);
	  //templateFinder = mock(TemplateFinder.class);
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
	  if (dataProvider != null){
		  return dataProvider;
	  }
	  payloadManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookPayloadManager.class);
	  templateFinder = ContextLoader.getCurrentWebApplicationContext().getBean(TemplateFinder.class);
	  dataProvider = new DataProvider(sBuildServer, new TestUrlHolder(), permissionChecker, payloadManager, templateManager, templateFinder);
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