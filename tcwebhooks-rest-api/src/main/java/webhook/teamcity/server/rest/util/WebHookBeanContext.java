package webhook.teamcity.server.rest.util;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.ApiUrlBuilder;
import jetbrains.buildServer.server.rest.util.BeanFactory;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.server.rest.WebHookApiUrlBuilder;

public class WebHookBeanContext {
  private final BeanFactory myFactory;
  @NotNull private final ServiceLocator myServiceLocator;
  @NotNull private final WebHookApiUrlBuilder myApiUrlBuilder;

  public WebHookBeanContext(final BeanFactory factory, @NotNull final ServiceLocator serviceLocator, @NotNull WebHookApiUrlBuilder apiUrlBuilder) {
    myFactory = factory;
    myServiceLocator = serviceLocator;
    myApiUrlBuilder = apiUrlBuilder;
  }

  public <T> void autowire(T t){
    myFactory.autowire(t);
  }

  @NotNull
  public <T> T getSingletonService(@NotNull Class<T> serviceClass) {
    return myServiceLocator.getSingletonService(serviceClass);
  }

  @NotNull
  public WebHookApiUrlBuilder getApiUrlBuilder(){
        return myApiUrlBuilder;
  }

  @NotNull
  public ServiceLocator getServiceLocator(){
        return myServiceLocator;
  }

  @NotNull
  public WebHookApiUrlBuilder getContextService(@NotNull Class<ApiUrlBuilder> serviceClass) {
        return myApiUrlBuilder;
  }
}