package webhook.teamcity.server.rest.request;

import static org.junit.Assert.*;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoaderListener;

import com.riffpie.common.testing.AbstractSpringAwareJerseyTest;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

import webhook.teamcity.server.rest.model.template.Templates;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={RequestTestConfiguration.class})
//public class TemplateRequestTest extends AbstractSpringAwareJerseyTest {
public class TemplateRequestTest extends JerseyTest {
	
/*	public TemplateRequestTest() {
		super(new WebAppDescriptor.Builder("webhook.teamcity.test.jerseyprovider, webhook.teamcity.server.rest.request")
        .contextPath("testing")
        .contextParam("contextConfigLocation", "classpath:TestSpringContext.xml")
        .contextListenerClass(ContextLoaderListener.class)
        .build());
	}*/
	
	@Override
		protected AppDescriptor configure() {
			return new WebAppDescriptor.Builder("webhook.teamcity.test.jerseyprovider, webhook.teamcity.server.rest.request")
					.contextPath("testing")
					.build();
			// TODO Auto-generated method stub
			//return super.configure();
		}
	
	@Autowired
	SBuildServer server; 
	
	@Autowired
	TemplateRequest templateRequest;
	
/*	@Autowired
	TemplateRequest templateRequest; 
*/
	@Test
	public void testGetHref() {
		// = new TemplateRequest();
		Templates templates = templateRequest.serveTemplates("id");
		//fail("Not yet implemented");
	}

	@Test
	public void testGetTemplateHref() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDefaultTemplateTextHref() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDefaultBranchTemplateTextHref() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTemplateTextHref() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBranchTemplateTextHref() {
		fail("Not yet implemented");
	}

	@Test
	public void testServeTemplates() {
		fail("Not yet implemented");
	}

	@Test
	public void testServeTemplate() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateEmptyTemplate() {
		fail("Not yet implemented");
	}

	@Test
	public void testServeFullConfigTemplateFor() {
		fail("Not yet implemented");
	}

	@Test
	public void testServeTemplateContent() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateTemplateContent() {
		fail("Not yet implemented");
	}

}
