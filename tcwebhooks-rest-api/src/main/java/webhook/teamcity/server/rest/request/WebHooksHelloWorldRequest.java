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

package webhook.teamcity.server.rest.request;

import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.data.BuildTypeFinder;
import jetbrains.buildServer.server.rest.data.Locator;
import jetbrains.buildServer.server.rest.request.Constants;
import jetbrains.buildServer.serverSide.TeamCityProperties;

import org.jetbrains.annotations.NotNull;

/**
 * @author Yegor.Yarko
 *         Date: 20.06.2010
 */
@Path(Constants.API_URL + "/webhooks")
@Singleton
public class WebHooksHelloWorldRequest {
  @Context @NotNull private ServiceLocator myServiceLocator;
  @Context @NotNull private BuildTypeFinder myBuildTypeFinder;
  
  @GET
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public String serveProjectsConvenienceCopy(@QueryParam("locator") String buildTypeLocator) {
    return "Wooot";
  }

  @GET
  @Path("/hello")
  @Produces({MediaType.TEXT_PLAIN})
  public String sayHello() {
	  return "Wooot";
  }
  

}