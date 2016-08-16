package webhook.teamcity.test.jerseyprovider;

import javax.ws.rs.ext.Provider;

import jetbrains.buildServer.server.rest.RequestPathTransformInfo;
import jetbrains.buildServer.server.rest.jersey.AbstractSingletonBeanProvider;


@Provider
public class RequestPathTransformInfoProvider extends AbstractSingletonBeanProvider<RequestPathTransformInfo> {
  public RequestPathTransformInfoProvider() {
    super(new RequestPathTransformInfo(), RequestPathTransformInfo.class);
  }
}