package webhook.teamcity.server.rest.request;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import jetbrains.buildServer.server.rest.errors.OperationException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.model.PagerData;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.server.rest.data.WebHookParameterDataProvider;
import webhook.teamcity.server.rest.data.WebHookParameterFinder;
import webhook.teamcity.server.rest.data.WebHookParameterValidator;
import webhook.teamcity.server.rest.errors.BadRequestException;
import webhook.teamcity.server.rest.errors.UnprocessableEntityException;
import webhook.teamcity.server.rest.errors.WebHookPermissionException;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameter;
import webhook.teamcity.server.rest.model.parameter.ProjectWebhookParameters;
import webhook.teamcity.server.rest.model.template.ErrorResult;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.project.WebHookParameter;
import webhook.teamcity.settings.project.WebHookParameterStore;

@Path(WebHookParametersRequest.API_PARAMETERS_URL)
public class WebHookParametersRequest {

	@Context
	@NotNull
	private WebHookParameterDataProvider myDataProvider;
	
	@Context
	@NotNull
	private WebHookParameterValidator myWebHookParameterValidator;
	
	@Context
	@NotNull
	private WebHookParameterStore myWebHookParameterStore;

	@Context
	@NotNull
	private ServiceLocator myServiceLocator;
	
	@Context
	@NotNull
	private BeanContext myBeanContext;
	
	@Context
	@NotNull
	private PermissionChecker myPermissionChecker;

	public static final String API_PARAMETERS_URL = Constants.API_URL + "/parameters";
	private static final String PARAMETER_CONTAINED_INVALID_DATA = "Parameter contained invalid data";

	
	@NotNull
	public static String getWebHookParameterHref(String projectExternalId, WebHookParameter webhookParameter) {
		return API_PARAMETERS_URL + "/" + projectExternalId + "/"+ WebHookParameterFinder.getLocator(webhookParameter);
	}
	
	@NotNull
	public static String getHref(String projectExternalId) {
		return API_PARAMETERS_URL + "/" + projectExternalId;
	}

	@GET
	@Path("/{projectId}")
	@Produces({ "application/xml", "application/json" })
	public ProjectWebhookParameters serveParameters(@PathParam("projectId") String projectExternalId,
			@QueryParam("fields") String fields) {
		
		SProject sProject = resolveProject(projectExternalId);
		checkParameterReadPermission(sProject.getProjectId());
		return this.myDataProvider.getWebHookParameterFinder().getAllWebHookParameters(sProject, new PagerData(getHref(sProject.getExternalId())), new Fields(fields), myBeanContext);
	}
	
	@GET
	@Path("/{projectId}/{parameterLocator}")
	@Produces({ "application/xml", "application/json" })
	public ProjectWebhookParameter serveParameter(
			@PathParam("projectId") String projectExternalId, 
			@PathParam("parameterLocator") String parameterLocator,
			@QueryParam("fields") String fields
		)
	{
		SProject sProject = resolveProject(projectExternalId);
		checkParameterReadPermission(sProject.getProjectId());
		return this.myDataProvider.getWebHookParameterFinder().findWebhookParameter(sProject, parameterLocator, new Fields(fields), myBeanContext);
	}
	
	@POST
	@Path("/{projectId}")
	@Produces({ "application/xml", "application/json" })
	@Consumes({"application/xml", "application/json"})
	public ProjectWebhookParameter createParameter(
			@PathParam("projectId") String projectExternalId, 
			@QueryParam("fields") String fields,
			ProjectWebhookParameter newParameter
			)
	{
		SProject sProject = resolveProject(projectExternalId);
		checkParameterWritePermission(sProject.getProjectId());
		ErrorResult validationResult = myWebHookParameterValidator.validateNewParameter(sProject.getExternalId(), newParameter, new ErrorResult());
		if (validationResult.isErrored()) {
			throw new UnprocessableEntityException(PARAMETER_CONTAINED_INVALID_DATA, validationResult);
		}
		if (this.myDataProvider.getWebHookParameterFinder().findWebhookParameter(sProject, newParameter.getName()) != null) {
			throw new BadRequestException("Parameter name already exists in this project");
		}
		WebHookParameter persistedParameter = myWebHookParameterStore.addWebHookParameter(sProject.getProjectId(), newParameter);
		return new ProjectWebhookParameter(persistedParameter, new Fields(fields), myBeanContext.getApiUrlBuilder().getProjectParameterHref(projectExternalId, persistedParameter));
	}
	
	@PUT
	@Path("/{projectId}/{parameterId}")
	@Produces({ "application/xml", "application/json" })
	@Consumes({"application/xml", "application/json"})
	public ProjectWebhookParameter updateParameter(
			@PathParam("projectId") String projectExternalId, 
			@PathParam("parameterId") String parameterId, 
			@QueryParam("fields") String fields,
			ProjectWebhookParameter updatedParameter
			)
	{
		SProject sProject = resolveProject(projectExternalId);
		checkParameterWritePermission(sProject.getProjectId());
		updatedParameter.setId(parameterId);
		ErrorResult validationResult = myWebHookParameterValidator.validateUpdatedParameter(sProject.getExternalId(), updatedParameter, new ErrorResult());
		if (validationResult.isErrored()) {
			throw new UnprocessableEntityException(PARAMETER_CONTAINED_INVALID_DATA, validationResult);
		}
		WebHookParameter existingParameter = this.myDataProvider.getWebHookParameterFinder().findWebhookParameter(sProject, parameterId, new Fields(fields), myBeanContext); 
		if (existingParameter == null) {
			throw new NotFoundException("No Parameter with that ID exists in this project");
		}
		
		updatedParameter.setId(existingParameter.getId());
		WebHookParameter webHookParameterByName = myWebHookParameterStore.findWebHookParameter(sProject, updatedParameter.getName());
		if (webHookParameterByName != null && !webHookParameterByName.getId().equals(updatedParameter.getId())) {
			validationResult.addError("name", String.format("An existing parameter with id '%s' exists with the same name '%s'", webHookParameterByName.getId(), webHookParameterByName.getName()));
			throw new UnprocessableEntityException(PARAMETER_CONTAINED_INVALID_DATA, validationResult);
		}
		
		if (myWebHookParameterStore.updateWebHookParameter(sProject.getProjectId(), updatedParameter, "")) {
			WebHookParameter webHookParameterById = myWebHookParameterStore.getWebHookParameterById(sProject, existingParameter.getId());
			return new ProjectWebhookParameter(webHookParameterById, new Fields(fields), myBeanContext.getApiUrlBuilder().getProjectParameterHref(projectExternalId, webHookParameterById));
		} else {
			throw new OperationException("An error occured updating the prarameter");
		}
	}

	@DELETE
	@Path("/{projectId}/{parameterLocator}")
	@Produces({ "application/xml", "application/json" })
	public ProjectWebhookParameter deleteParameter(
			@PathParam("projectId") String projectExternalId, 
			@PathParam("parameterLocator") String parameterLocator, 
			@QueryParam("fields") String fields
			)
	{
		SProject sProject = resolveProject(projectExternalId);
		checkParameterWritePermission(sProject.getProjectId());
		ProjectWebhookParameter webhookParameter = this.myDataProvider.getWebHookParameterFinder().findWebhookParameter(sProject, parameterLocator, new Fields(fields), myBeanContext);
		this.myWebHookParameterStore.removeWebHookParameter(sProject.getProjectId(), webhookParameter);
		return webhookParameter;
	}
	private void checkParameterReadPermission(String projectInternalId) {
		try {
			myPermissionChecker.checkProjectPermission(Permission.VIEW_PROJECT, projectInternalId);
		} catch (AuthorizationFailedException e) {
			throw new WebHookPermissionException(
					"Reading parameters requires 'VIEW_PROJECT' permission.");
		}
	}

	private SProject resolveProject(String projectExternalId) {
		SProject sProject = this.myDataProvider.getProjectManager().findProjectByExternalId(projectExternalId);
		if (sProject == null) {
			throw new NotFoundException("No project matching " + projectExternalId);
		}
		return sProject;
	}

	private void checkParameterWritePermission(String projectInternalId) {
		try {
			myPermissionChecker.checkProjectPermission(Permission.EDIT_PROJECT, projectInternalId);
		} catch (AuthorizationFailedException e) {
			throw new WebHookPermissionException(
					"Updating parameters requires 'EDIT_PROJECT' permission.");
		}
	}


}
