package webhook.teamcity.server.rest.jersey;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import jetbrains.buildServer.server.rest.RequestPathTransformInfo;
import jetbrains.buildServer.server.rest.jersey.ApiUrlBuilderProvider.ApiUrlBuilderFactory;
import jetbrains.buildServer.server.rest.jersey.SimplePathTransformer;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;

@Provider
@SuppressWarnings("squid:S6813")
public class WebHookApiUrlBuilderProvider implements Feature {
	  @Override
	  public boolean configure(FeatureContext context) {
	    context.register(new AbstractBinder() {
	      @Override
	      protected void configure() {
	        bindFactory(ApiUrlBuilderFactory.class)
	          .to(WebHookApiUrlBuilder.class)
	          .in(RequestScoped.class);
	      }
	    });

	    return true;
	  }

	  /**
	   * Factory is bound per-lookup, so fields 'headers' and 'request' are being injected
	   * here per request, which is exactly what we want.
	   */
	  public static class WebHookApiUrlBuilderFactory implements Factory<WebHookApiUrlBuilder> {
	    @Inject
	    private HttpHeaders headers;

	    @Inject
	    private HttpServletRequest request;

	    @Inject
	    private RequestPathTransformInfo requestPathTransformInfo;

	    @Override
	    public WebHookApiUrlBuilder provide() {
	      return new WebHookApiUrlBuilder(new SimplePathTransformer(request, headers, requestPathTransformInfo));
	    }

	    @Override
	    public void dispose(WebHookApiUrlBuilder instance) { /* no cleanup required */}
	  }
}
