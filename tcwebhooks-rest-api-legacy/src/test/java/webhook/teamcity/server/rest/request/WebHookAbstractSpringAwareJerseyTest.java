package webhook.teamcity.server.rest.request;

import org.springframework.web.context.ContextLoaderListener;

import com.riffpie.common.testing.AbstractSpringAwareJerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class WebHookAbstractSpringAwareJerseyTest extends AbstractSpringAwareJerseyTest {
	
	public WebHookAbstractSpringAwareJerseyTest() {
		super(new WebAppDescriptor.Builder("webhook.teamcity.test.jerseyprovider",  "webhook.teamcity.server.rest.request", "webhook.teamcity.server.rest.model", "webhook.teamcity.settings")
        .contextPath("testing")
        .contextParam("contextConfigLocation", "classpath:/TestSpringContext.xml")
        .contextListenerClass(ContextLoaderListener.class)
        .build());
	}

}
