package webhook.teamcity.server.rest.request;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.AuthorizationFailedException;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.server.rest.errors.WebHookPermissionException;
import webhook.teamcity.server.rest.model.mainconfig.Webhooks;
import webhook.teamcity.server.rest.util.mainconfig.MainConfigManager;

/**
 * @author Net Wolf UK Date: 27.01.2015
 */

@Path(WebHooksConfigRequest.BASE_URI)
public class WebHooksConfigRequest implements ApiRequest {
	static final String BASE_URI = Constants.API_URL + "/server";
	private static final Permission mainConfigReadPermission = Permission.CHANGE_SERVER_SETTINGS;
	private static final Permission mainConfigEditPermission = Permission.CHANGE_SERVER_SETTINGS;
	@Context @NotNull private MainConfigManager mainConfigManager;
	@Context @NotNull private UriInfo uriInfo;
	@Context @NotNull public PermissionChecker myPermissionChecker;

	@GET 	// An @GET implies @Path("/")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Webhooks showConfig() {
		checkMainConfigReadPermission();
		return mainConfigManager.build(uriInfo);
	}

	@PUT
	public Response updateConfig(Webhooks webhooks){
		checkMainConfigWritePermission();
		mainConfigManager.updateMainConfig(webhooks);
		return Response.noContent().build();
	}

	public static String getUri() {
		return BASE_URI;
	}
	
	private void checkMainConfigReadPermission() {
		try {
			myPermissionChecker.checkGlobalPermission(mainConfigReadPermission);
		} catch (AuthorizationFailedException e) {
			throw new WebHookPermissionException("Reading main settings requires permission: 'CHANGE_SERVER_SETTINGS'");
		}
	}
	
	private void checkMainConfigWritePermission() {
		try {
			myPermissionChecker.checkGlobalPermission(mainConfigEditPermission);
		} catch (AuthorizationFailedException e) {
			throw new WebHookPermissionException("Writing main settings requires permission 'CHANGE_SERVER_SETTINGS'");
		}
	}

}