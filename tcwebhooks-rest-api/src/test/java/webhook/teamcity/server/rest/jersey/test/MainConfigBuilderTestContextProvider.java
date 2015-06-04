package webhook.teamcity.server.rest.jersey.test;

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

import jetbrains.buildServer.serverSide.SBuildServer;
import webhook.teamcity.server.rest.util.mainconfig.MainConfigManager;
import webhook.teamcity.settings.WebHookMainSettings;

@Provider
public class MainConfigBuilderTestContextProvider implements InjectableProvider<Context, Type>, Injectable<MainConfigManager> {
  private final MainConfigManager mainConfigBuilder;
  private final WebHookMainSettings webHookMainSettings;
  private final SBuildServer sBuildServer = mock(SBuildServer.class);
  
  public MainConfigBuilderTestContextProvider() {
	  System.out.println("We are here: Trying to provide a testable WebHookMainSettings instance");
	  	webHookMainSettings = new WebHookMainSettings(sBuildServer);
	  	webHookMainSettings.register();
	  	webHookMainSettings.readFrom(getFullConfigElement());
	  	mainConfigBuilder = new MainConfigManager(webHookMainSettings);

  }

  public ComponentScope getScope() {
    return ComponentScope.Singleton;
  }

  public Injectable<MainConfigManager> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(MainConfigManager.class)) {
      return this;
    }
    return null;
  }

  public MainConfigManager getValue() {
    return mainConfigBuilder;
  }
  
  private Element getFullConfigElement(){
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);
		try {
			Document doc = builder.build("../tcwebhooks-core/src/test/resources/main-config-full.xml");
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