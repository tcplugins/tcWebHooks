package webhook.teamcity.test.jerseyprovider;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.springframework.web.context.ContextLoader;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.data.DataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.WebHookFinder;
import webhook.teamcity.server.rest.request.Constants;
import webhook.teamcity.server.rest.util.webhook.WebHookManager;
import webhook.teamcity.test.springmock.MockProjectManager;

@Provider
public class DataProviderTestContextProvider implements InjectableProvider<Context, Type>, Injectable<DataProvider> {
  private DataProvider dataProvider;
  private final SBuildServer sBuildServer;
  private final PermissionChecker permissionChecker;
  private final ProjectManager projectManager;
  private TemplateFinder templateFinder;
  private WebHookPayloadManager payloadManager;
  @Context WebHookTemplateManager templateManager;
  private WebHookManager webHookManager;
  private WebHookFinder webHookFinder;
  
  
  public DataProviderTestContextProvider() {
	  System.out.println("We are here: Trying to provide a testable DataProvider instance");
	  sBuildServer = mock(SBuildServer.class);
	  permissionChecker = mock(PermissionChecker.class);
	  projectManager = new MockProjectManager();
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
	  webHookManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookManager.class);
	  webHookFinder = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookFinder.class);
	  
	  dataProvider = new DataProvider(sBuildServer, new TestUrlHolder(), permissionChecker, payloadManager, templateManager, templateFinder, projectManager, webHookManager, webHookFinder);
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