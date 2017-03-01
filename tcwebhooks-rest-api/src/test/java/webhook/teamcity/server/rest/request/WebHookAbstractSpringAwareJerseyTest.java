package webhook.teamcity.server.rest.request;

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.web.context.ContextLoaderListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
	
	public void prettyPrint(Object responseMsg){
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	System.out.println(gson.toJson(responseMsg));
	}

}
