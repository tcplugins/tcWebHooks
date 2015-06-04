package webhook.teamcity.server.rest.request;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import jetbrains.buildServer.server.rest.request.Constants;
import jetbrains.buildServer.server.rest.util.BeanContext;

import org.jetbrains.annotations.NotNull;

import webhook.teamcity.server.rest.model.mainconfig.Webhooks;
import webhook.teamcity.server.rest.util.mainconfig.MainConfigManager;

import com.sun.jersey.spi.resource.Singleton;

/**
 * @author Net Wolf UK Date: 27.01.2015
 */

@Path(WebHooksConfigRequest.BASE_URI)
@Singleton
public class WebHooksConfigRequest implements ApiRequest {
	static final String BASE_URI = Constants.API_URL + "/webhooks/server";
	private static final String NAME = WebHooksConfigRequest.class.getName();
	@Context @NotNull private MainConfigManager mainConfigManager;
//	@Context @NotNull private BeanContext myBeanContext;
	@Context @NotNull private UriInfo uriInfo;

	@GET 	// An @GET implies @Path("/")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Webhooks showConfig() {
		return mainConfigManager.build(uriInfo);
	}

	@GET
	@Path("/json")
	@Produces({ MediaType.APPLICATION_JSON })
	public Webhooks showConfigAsJson() {
		return mainConfigManager.build(uriInfo);
	}
	
	@PUT
	public Response updateConfig(Webhooks webhooks){
		mainConfigManager.updateMainConfig(webhooks);
		return Response.noContent().build();
	}

	public static String getUri() {
		return BASE_URI;
	}

}