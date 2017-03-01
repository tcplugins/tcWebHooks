package webhook.teamcity.test.jerseyprovider;

import static org.mockito.Mockito.mock;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.mockito.Mock;
import org.springframework.web.context.ContextLoader;

import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.format.WebHookPayloadJsonTemplate;
import webhook.teamcity.server.rest.util.mainconfig.MainConfigManager;
import webhook.teamcity.settings.WebHookMainSettings;

@Provider
public class WebHookPayloadManagerTestProvider implements InjectableProvider<Context, Type>, Injectable<WebHookPayloadManager> {
  private WebHookPayloadManager webHookPayloadManager;
  private final SBuildServer sBuildServer = mock(SBuildServer.class);
  
  public WebHookPayloadManagerTestProvider() {
	  System.out.println("We are here: Trying to provide a testable WebHookPayloadManager instance");

  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookPayloadManager> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookPayloadManager.class)) {
    	System.out.println("WebHookPayloadManagerTestProvider: Providing injectable");
      return this;
    }
    return null;
  }

@Override
public WebHookPayloadManager getValue() {
	System.out.println("WebHookPayloadManagerTestProvider: Providing value " + webHookPayloadManager.toString());
	webHookPayloadManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookPayloadManager.class);
	return webHookPayloadManager;
}

}