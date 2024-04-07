package webhook.teamcity.test.jerseyprovider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.springframework.web.context.ContextLoader;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;

@Provider
public class WebHookTemplateManagerTestProvider implements InjectableProvider<Context, Type>, Injectable<WebHookTemplateManager> {
  WebHookTemplateManager webHookTemplateManager;
  WebHookPayloadManager webHookPayloadManager;
  WebHookTemplateJaxHelper webHookTemplateJaxHelper;
private ProjectIdResolver projectIdResolver;
  
  public WebHookTemplateManagerTestProvider() throws IOException {
	  System.out.println("We are here: Trying to provide a testable WebHookTemplateManager instance");
	  	//webHookTemplateManager = new WebHookTemplateManager(webHookPayloadManager);
	  	

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
	  
	  webHookPayloadManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookPayloadManager.class);
	  webHookTemplateManager = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookTemplateManager.class);
	  webHookTemplateJaxHelper = ContextLoader.getCurrentWebApplicationContext().getBean(WebHookTemplateJaxHelper.class);
	  projectIdResolver = ContextLoader.getCurrentWebApplicationContext().getBean(ProjectIdResolver.class);
	  
	  if (webHookTemplateManager != null){
		  System.out.println("WebHookTemplateManagerTestProvider: Providing (existing) value " + webHookTemplateManager.toString());
		  return webHookTemplateManager;
	  }
		File tempDir;
		try {
			tempDir = File.createTempFile("tempWebHooksDir", "", new File("target/"));
			tempDir.mkdir();
			webHookTemplateManager = new WebHookTemplateManager(webHookPayloadManager, webHookTemplateJaxHelper, projectIdResolver);
			webHookTemplateManager.setConfigFilePath(tempDir.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("WebHookTemplateManagerTestProvider: Providing (new) value " + webHookTemplateManager.toString());
		return webHookTemplateManager;
  }

}