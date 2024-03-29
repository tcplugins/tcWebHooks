/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import webhook.teamcity.Loggers;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.util.WebHookBeanContext;
import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.RequestPathTransformInfo;
import jetbrains.buildServer.server.rest.jersey.SimplePathTransformer;
import jetbrains.buildServer.server.rest.util.BeanFactory;

/**
 * @author Yegor.Yarko
 *         Date: 15.11.2009
 */
@Provider
@SuppressWarnings("squid:S1191")
public class WebHookBeanContextProvider implements Feature {
  @Override
  public boolean configure(FeatureContext context) {
  	Loggers.SERVER.info("tcWebHooksRest: WebHookBeanContextProvider configure called");

    context.register(new AbstractBinder() {
      @Override
      protected void configure() {
        Loggers.SERVER.info("tcWebHooksRest: WebHookBeanContextProvider AbstractBinder#configure called");
        bindFactory(WebHookBeanContextFactory.class)
                .to(WebHookBeanContext.class)
                .in(RequestScoped.class);
      }
    });

    return true;
  }

  public static class WebHookBeanContextFactory implements Factory<WebHookBeanContext> {
    @Inject private RequestPathTransformInfo myRequestPathTransformInfo;
    @Inject private BeanFactory myFactory;
    @Inject private ServiceLocator myServiceLocator;
    @Inject private HttpHeaders headers;
    @Inject private HttpServletRequest request;

    @Override
    public WebHookBeanContext provide() {
    	Loggers.SERVER.info("tcWebHooksRest: here trying to create beanContext from factory " + myFactory.toString());
      return new WebHookBeanContext(myFactory, myServiceLocator, new WebHookApiUrlBuilder(new SimplePathTransformer(request, headers, myRequestPathTransformInfo)));
    }

    @Override
    public void dispose(WebHookBeanContext instance) {
    	Loggers.SERVER.info("tcWebHooksRest: dispose called");
    }
  }
}
