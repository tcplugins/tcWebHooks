package webhook.teamcity.server.rest.request;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.AuthorizationFailedException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.server.rest.data.WebHookDataProvider;
import webhook.teamcity.server.rest.data.WebHookFinder;
import webhook.teamcity.server.rest.errors.WebHookPermissionException;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhook;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.WebHookConfig;

@Path(WebHooksRequest.API_WEBHOOKS_URL)
public class WebHooksRequest {

	@Context
	@NotNull
	private WebHookDataProvider myDataProvider;

	@Context
	@NotNull
	private ServiceLocator myServiceLocator;
	
	@Context
	@NotNull
	private BeanContext myBeanContext;
	
	@Context
	@NotNull
	private PermissionChecker myPermissionChecker;

	public static final String API_WEBHOOKS_URL = Constants.API_URL;
	
	@NotNull
	public static String getWebHookHref(String projectExternalId, WebHookConfig webhook) {
		return API_WEBHOOKS_URL + "/" + projectExternalId + "/"+ WebHookFinder.getLocator(webhook);
	}

	@GET
	@Path("/{projectId}")
	@Produces({ "application/xml", "application/json" })
	public ProjectWebhooks serveWebHooks(@PathParam("projectId") String projectExternalId,
			@QueryParam("fields") String fields) {
		
		SProject sProject = resolveProject(projectExternalId);
		checkWebHookReadPermission(sProject.getProjectId());
		return this.myDataProvider.getWebHookManager().getWebHookList(projectExternalId, new Fields(fields), myBeanContext);
	}
	
	@GET
	@Path("/{projectId}/{webhookLocator}")
	@Produces({ "application/xml", "application/json" })
	public ProjectWebhook serveWebHook(
			@PathParam("projectId") String projectExternalId, 
			@PathParam("webhookLocator") String webhookLocator,
			@QueryParam("fields") String fields
		)
	{
		SProject sProject = resolveProject(projectExternalId);
		checkWebHookReadPermission(sProject.getProjectId());
		return this.myDataProvider.getWebHookFinder().findWebHookById(projectExternalId, webhookLocator, new Fields(fields), myBeanContext);
	}

	private void checkWebHookReadPermission(String projectInternalId) {
		try {
			myPermissionChecker.checkProjectPermission(Permission.VIEW_PROJECT, projectInternalId);
		} catch (AuthorizationFailedException e) {
			throw new WebHookPermissionException(
					"Reading webhooks requires 'VIEW_PROJECT' permission.");
		}
	}

	private SProject resolveProject(String projectExternalId) {
		SProject sProject = this.myDataProvider.getProjectManager().findProjectByExternalId(projectExternalId);
		if (sProject == null) {
			throw new NotFoundException("No project matching " + projectExternalId);
		}
		return sProject;
	}

	private void checkWebHookWritePermission(String projectInternalId) {
		try {
			myPermissionChecker.checkProjectPermission(Permission.EDIT_PROJECT, projectInternalId);
		} catch (AuthorizationFailedException e) {
			throw new WebHookPermissionException(
					"Updating webhooks requires 'EDIT_PROJECT' permission.");
		}
	}


}
