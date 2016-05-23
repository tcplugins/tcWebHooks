package webhook.teamcity.server.rest.model.template;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.PathTransformer;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.util.BeanFactory;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplate;
import webhook.teamcity.payload.WebHookTemplateFileChangeHandler;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.WebHookWebLinks;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public class TemplateTest {
	
	@Mock
	ApplicationContext ctx;

	@Mock
	ServiceLocator serviceLocator;
	
	PathTransformer pathTransformer;
	
	@Mock
	SBuildServer mockServer;
	
	@Mock
	RootUrlHolder rootUrlHolder;
	
	WebHookTemplateManager wtm;
	WebHookPayloadManager wpm;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		when(mockServer.getRootUrl()).thenReturn("http://test.url");
		wpm = new WebHookPayloadManager(mockServer);
		wtm = new WebHookTemplateManager(wpm);
		
		ServerPaths serverPaths = new ServerPaths(new File("../tcwebhooks-core/src/test/resources/testXmlTemplate"));
		WebHookTemplateFileChangeHandler changeListener = new WebHookTemplateFileChangeHandler(serverPaths, wtm, wpm);
		changeListener.register();
		changeListener.handleConfigFileChange();
		
		when(rootUrlHolder.getRootUrl()).thenReturn("http://some.test.server/");
		when(serviceLocator.getSingletonService(WebHookWebLinks.class)).thenReturn(new WebHookWebLinks(rootUrlHolder));
		
		pathTransformer = new PathTransformer() {
			@Override
			public String transform(String path) {
				return path;
			}
		};
		
	}
	
	@Test
	public void testTemplateWebHookTemplateEntityFieldsBeanContext() {
		WebHookTemplateEntity template = wtm.getTemplateEntity("testXMLtemplate");
		BeanFactory factory = new BeanFactory(ctx);
		BeanContext beanContext = new BeanContext(factory, serviceLocator, new WebHookApiUrlBuilder(pathTransformer));
		
		Template t = new Template(template, new Fields(null), beanContext);
		
		
		
		//fail("Not yet implemented");
	}

}
