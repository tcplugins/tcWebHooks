package webhook.teamcity.test.jerseyprovider;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.springframework.web.context.ContextLoader;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import jetbrains.buildServer.serverSide.ProjectManager;
import webhook.teamcity.TeamCityCoreFacadeImpl;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreImpl;

@Provider
public class WebHookParameterStoreTestContextProvider implements InjectableProvider<Context, Type>, Injectable<WebHookParameterStore> {
	
	  public ComponentScope getScope() {
	    return ComponentScope.Singleton;
	  }

	  public Injectable<WebHookParameterStore> getInjectable(final ComponentContext ic, final Context context, final Type type) {
	    if (type.equals(WebHookParameterStore.class)) {
	      return this;
	    }
	    return null;
	  }

	  public WebHookParameterStore getValue() {
		ProjectManager projectManager = ContextLoader.getCurrentWebApplicationContext().getBean(ProjectManager.class);
	    return new WebHookParameterStoreImpl(new TeamCityCoreFacadeImpl(projectManager));
	  }

}