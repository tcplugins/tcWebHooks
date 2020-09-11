package webhook.teamcity.test.jerseyprovider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

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
import jetbrains.buildServer.serverSide.SecurityContextEx;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.data.TemplateDataProvider;
import webhook.teamcity.server.rest.data.TemplateFinder;
import webhook.teamcity.server.rest.data.WebHookFinder;
import webhook.teamcity.server.rest.data.WebHookParameterFinder;
import webhook.teamcity.server.rest.request.Constants;
import webhook.teamcity.server.rest.util.webhook.WebHookManager;
import webhook.teamcity.test.springmock.MockProjectManager;

@Provider
public class TemplateDataProviderTestContextProvider implements InjectableProvider<Context, Type>, Injectable<TemplateDataProvider> {
  private TemplateDataProvider dataProvider;
  private final SBuildServer sBuildServer;
  private final PermissionChecker permissionChecker;
  private final ProjectManager projectManager;
  private TemplateFinder templateFinder;
  private WebHookPayloadManager payloadManager;
  @Context WebHookTemplateManager templateManager;
  private WebHookManager webHookManager;
  private WebHookFinder webHookFinder;
  private WebHookParameterFinder webHookParameterFinder;
  private final ProjectIdResolver projectIdResolver;
  private final SecurityContextEx securityContext;
  private final AuthorityHolder authorityHolder;
  
  
  public TemplateDataProviderTestContextProvider() {
	  System.out.println("We are here: Trying to provide a testable DataProvider instance");
	  sBuildServer = mock(SBuildServer.class);
	  permissionChecker = mock(PermissionChecker.class);
	  projectManager = new MockProjectManager();
	  projectIdResolver = mock(ProjectIdResolver.class);
	  securityContext = mock(SecurityContextEx.class);
	  authorityHolder = mock(AuthorityHolder.class);
	  when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder);
	  when(authorityHolder.isPermissionGrantedForAnyProject(eq(Permission.EDIT_PROJECT))).thenReturn(true);
	  //templateFinder = mock(TemplateFinder.class);
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<TemplateDataProvider> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(TemplateDataProvider.class)) {
      return this;
    }
    return null;
  }

  public TemplateDataProvider getValue() {
	  if (dataProvider != null){
		  return dataProvider;
	  }
	  payloadManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookPayloadManager.class);
	  templateFinder = ContextLoader.getCurrentWebApplicationContext().getBean(TemplateFinder.class);
	  webHookManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookManager.class);
	  webHookFinder = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookFinder.class);
	  webHookParameterFinder = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookParameterFinder.class);
	  //projectIdResolver = ContextLoader.getCurrentWebApplicationContext().getBean(ProjectIdResolver.class);
	  
	  dataProvider = new TemplateDataProvider(sBuildServer, new TestUrlHolder(), permissionChecker, payloadManager, templateManager, templateFinder, projectManager, webHookFinder, projectIdResolver, securityContext);
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