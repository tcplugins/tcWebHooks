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

package webhook.teamcity.test.jerseyprovider;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.mockito.Mock;

import static org.mockito.Mockito.*;
import webhook.teamcity.server.rest.WebHookApiUrlBuilder;
import webhook.teamcity.server.rest.WebHookWebLinks;
import webhook.teamcity.server.rest.util.BeanContext;
import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.PathTransformer;
import jetbrains.buildServer.server.rest.RequestPathTransformInfo;
import jetbrains.buildServer.server.rest.util.BeanFactory;

/**
 * @author Yegor.Yarko
 *         Date: 15.11.2009
 */
@Provider
public class MockingBeanContextProvider implements InjectableProvider<Context, Type>, Injectable<BeanContext> {
  
  @Mock private RequestPathTransformInfo myRequestPathTransformInfo;

  //TODO: using request-specific field in singleton provider
  //TODO: may lead to concurrency issue as this instance is
  //TODO: created by spring not by Jersey!
  @Context private HttpHeaders headers;
  @Context private HttpServletRequest request;

  @Mock private BeanFactory myFactory;
  
  @Mock private ServiceLocator myServiceLocator;
  
  public MockingBeanContextProvider() {
	System.out.println("We are here: Trying to provide a test BeanContext instance");
	myServiceLocator = mock(ServiceLocator.class);
	when(myServiceLocator.getSingletonService(WebHookWebLinks.class)).thenReturn(new WebHookWebLinks(new TemplateDataProviderTestContextProvider.TestUrlHolder()));
  }

  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }

  public Injectable<BeanContext> getInjectable(final ComponentContext ic, final Context context, final Type type) {
    if (type.equals(BeanContext.class)) {
      return this;
    }
    return null;
  }

  public BeanContext getValue() {
    //return new BeanContext(myFactory, myServiceLocator, new WebHookApiUrlBuilder(new SimplePathTransformer(request, headers, myRequestPathTransformInfo)));
    return new BeanContext(myFactory, myServiceLocator, new WebHookApiUrlBuilder(new PathTransformer() {
		
		@Override
		public String transform(String path) {
			return path;
		}
	}));
  }
}
