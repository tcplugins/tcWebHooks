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

import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.util.mainconfig.MainConfigManager;
import webhook.teamcity.settings.WebHookMainSettings;

@Provider
public class WebHookTemplateManagerTestProvider implements InjectableProvider<Context, Type>, Injectable<WebHookTemplateManager> {
  private final WebHookTemplateManager webHookTemplateManager;
  @Context WebHookPayloadManager webHookPayloadManager;
  
  public WebHookTemplateManagerTestProvider() {
	  System.out.println("We are here: Trying to provide a testable WebHookMainSettings instance");
	  	webHookTemplateManager = new WebHookTemplateManager(webHookPayloadManager);
  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<WebHookTemplateManager> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(WebHookTemplateManager.class)) {
      return this;
    }
    return null;
  }

  public WebHookTemplateManager getValue() {
    return webHookTemplateManager;
  }
  
  private Element getFullConfigElement(){
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);
		try {
			Document doc = builder.build("../tcwebhooks-core/src/test/resources/webhook-templates.xml");
			return doc.getRootElement();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}