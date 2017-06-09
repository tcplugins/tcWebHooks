package webhook.teamcity.test.jerseyprovider;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import webhook.teamcity.server.rest.data.TemplateValidator;
import webhook.teamcity.server.rest.jersey.TemplateValidatorProvider;

@Provider
public class TemplateValidatorTestContextProvider extends TemplateValidatorProvider
		implements InjectableProvider<Context, Type>, Injectable<TemplateValidator> {

	public TemplateValidatorTestContextProvider() {
		super();
		System.out.println("We are here: Trying to provide a testable TemplateValidator instance");
	}

}