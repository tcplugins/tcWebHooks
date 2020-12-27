package webhook.teamcity.server.rest.request;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.data.Locator;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.AuthorizationFailedException;
import jetbrains.buildServer.server.rest.errors.NotFoundException;
import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.server.rest.data.WebHookConfigurationValidator;
import webhook.teamcity.server.rest.data.WebHookDataProvider;
import webhook.teamcity.server.rest.data.WebHookFinder;
import webhook.teamcity.server.rest.data.WebHookParameterFinder;
import webhook.teamcity.server.rest.data.WebHookParameterValidator;
import webhook.teamcity.server.rest.data.WebHookTemplateConfigWrapper;
import webhook.teamcity.server.rest.errors.UnprocessableEntityException;
import webhook.teamcity.server.rest.errors.WebHookPermissionException;
import webhook.teamcity.server.rest.model.template.ErrorResult;
import webhook.teamcity.server.rest.model.template.Template;
import webhook.teamcity.server.rest.model.webhook.ProjectWebHookFilter;
import webhook.teamcity.server.rest.model.webhook.ProjectWebHookFilters;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhook;
import webhook.teamcity.server.rest.model.webhook.ProjectWebhooks;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.WebHookSearchFilter;
import webhook.teamcity.settings.WebHookSearchFilter.WebHookSearchFilterBuilder;
import webhook.teamcity.settings.WebHookUpdateResult;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.project.WebHookParameter;

@Path(WebHooksRequest.API_WEBHOOKS_URL)
public class WebHooksRequest {

	@Context
	@NotNull
	private WebHookDataProvider myDataProvider;
	
	@Context
	@NotNull
	private WebHookConfigurationValidator myWebHookValidator;

	@Context
	@NotNull
	private ServiceLocator myServiceLocator;
	
	@Context
	@NotNull
	private BeanContext myBeanContext;
	
	@Context
	@NotNull
	private PermissionChecker myPermissionChecker;

	public static final String API_WEBHOOKS_URL = Constants.API_URL + "/configurations";
	private static final String NO_WEBHOOK_FOUND_BY_THAT_ID = "No webhook found by that id";
	private static final String WEBHOOK_CONTAINED_INVALID_DATA = "Template contained invalid data";


	
	@NotNull
	public static String getWebHookHref(String projectExternalId, WebHookConfig webhook) {
		return API_WEBHOOKS_URL + "/" + projectExternalId + "/"+ WebHookFinder.getLocator(webhook);
	}
	
	@NotNull
	public static String getWebHookParameterHref(String projectExternalId, WebHookConfig webhook, WebHookParameter webhookParameter) {
		return API_WEBHOOKS_URL + "/" + projectExternalId + "/"+ WebHookFinder.getLocator(webhook) + "/parameters/" + WebHookParameterFinder.getLocator(webhookParameter);
	}
	@NotNull
	public static String getWebHookFilterHref(String projectExternalId, WebHookConfig webhook, Integer filterId) {
		return API_WEBHOOKS_URL + "/" + projectExternalId + "/"+ WebHookFinder.getLocator(webhook) + "/filters/id:" + filterId;
	}
	public static String getWebHookFiltersHref(String projectExternalId, WebHookConfig webhook) {
		return API_WEBHOOKS_URL + "/" + projectExternalId + "/"+ WebHookFinder.getLocator(webhook) + "/filters";
	}

	@GET
	@Produces({ "application/xml", "application/json" })
	public ProjectWebhooks serveWebHooks(@QueryParam("locator") String locatorStr, @QueryParam("fields") String fields) {
		WebHookSearchFilterBuilder searchFilterBuilder = WebHookSearchFilter.builder();
		ProjectWebhooks projectWebhooks = new ProjectWebhooks();
		if (Objects.isNull(locatorStr)) {
			searchFilterBuilder.show("all");
		} else {
			Locator locator = new Locator(locatorStr, "project", "id", "template", "tag");
		
			
			for (String dimension : new String[]{"project", "id", "template", "tag"}) {
				String value = locator.getSingleDimensionValue(dimension);
				if ("id".equals(dimension)) {
					searchFilterBuilder.webhookId(value);
				} else if ("project".equals(dimension)) { 
					searchFilterBuilder.projectExternalId(value);
				} else if ("template".equals(dimension)) {
					searchFilterBuilder.templateId(value);
				} else if ("tag".equals(dimension)) {
					searchFilterBuilder.tags(Collections.singleton(value));
				}
			}
			locator.checkLocatorFullyProcessed();
		}
		List<ProjectWebhook> found = myDataProvider.getWebHookFinder().searchForWebHooks(searchFilterBuilder.build(), new Fields(fields), myBeanContext);
		projectWebhooks.setWebhooks(found);
		projectWebhooks.setCount(found.size());
		return projectWebhooks;
	}
	
	@GET
	@Path("/{projectId}")
	@Produces({ "application/xml", "application/json" })
	public ProjectWebhooks serveProjectWebHooks(
			@PathParam("projectId") String projectExternalId,
			@QueryParam("fields") String fields) 
	{
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
		return this.myDataProvider.getWebHookFinder().findWebHookById(projectExternalId, webhookLocator, new Fields(fields), myBeanContext); // Throws permissionDenied or notFound
		
	}
	
	@PUT
	@Path("/{projectId}/{webhookLocator}")
	@Consumes({ "application/xml", "application/json" })
	@Produces({ "application/xml", "application/json" })
	public ProjectWebhook replaceWebHook(@PathParam("projectId") String projectExternalId, @PathParam("webhookLocator") String webhookLocator, ProjectWebhook webhook, @QueryParam("fields") String fields) {
		SProject sProject  = resolveProject(projectExternalId);
		checkWebHookWritePermission(sProject.getProjectId());
		WebHookConfig existingWebhookConfig = this.myDataProvider.getWebHookFinder().getWebHookConfigById(projectExternalId, webhookLocator);
		
		if (existingWebhookConfig == null) {
			throw new NotFoundException(NO_WEBHOOK_FOUND_BY_THAT_ID);
		}
		ErrorResult validationResult = myWebHookValidator.validateUpdatedWebHook(projectExternalId, webhook, new ErrorResult());

		if (!existingWebhookConfig.getUniqueKey().equals(webhook.getId())) {
			validationResult.addError("id", "The webhookId in the webhook does not match the webhookId in the URL.");
		}
		
		if (validationResult.isErrored()) {
			throw new UnprocessableEntityException(WEBHOOK_CONTAINED_INVALID_DATA, validationResult);
		}
		WebHookConfig config = webhook.toWebHookConfig(this.myDataProvider.getProjectIdResolver(), this.myDataProvider.getBuildTypeIdResolver());
		WebHookUpdateResult result = myDataProvider.getWebHookFinder().getWebHookProjectSettings(projectExternalId).updateWebHook(config);
		return new ProjectWebhook(result.getWebHookConfig(), sProject.getExternalId(), new Fields(fields), myBeanContext, this.myDataProvider.getWebHookFinder().getBuildTypeExternalIds(config.getEnabledBuildTypesSet()));
	}
	
	@GET
	@Path("/{projectId}/{webhookLocator}/filters")
	@Produces({ "application/xml", "application/json" })
	public ProjectWebHookFilters serveWebHookFilters(
			@PathParam("projectId") String projectExternalId, 
			@PathParam("webhookLocator") String webhookLocator,
			@QueryParam("fields") String fields
		)
	{
		SProject sProject = resolveProject(projectExternalId);
		checkWebHookReadPermission(sProject.getProjectId());
		return this.myDataProvider.getWebHookFinder().findWebHookById(projectExternalId, webhookLocator, new Fields(fields), myBeanContext).getFilters(); // Throws permissionDenied or notFound
	}
	
	@GET
	@Path("/{projectId}/{webhookLocator}/filters/{filterLocator}")
	@Produces({ "application/xml", "application/json" })
	public ProjectWebHookFilter serveWebHookFilter(
			@PathParam("projectId") String projectExternalId, 
			@PathParam("webhookLocator") String webhookLocator,
			@PathParam("filterLocator") String filterLocator,
			@QueryParam("fields") String fields
			)
	{
		SProject sProject = resolveProject(projectExternalId);
		checkWebHookReadPermission(sProject.getProjectId());
		ProjectWebHookFilters filters = this.myDataProvider.getWebHookFinder().findWebHookById(projectExternalId, webhookLocator, new Fields(fields), myBeanContext).getFilters(); // Throws permissionDenied or notFound
		
		final Locator locator = new Locator(filterLocator, "id", Locator.LOCATOR_SINGLE_VALUE_UNUSED_NAME);
		Integer filterId = null;
		if (locator.isSingleValue()) {
			// no dimensions found, assume it's an id
			filterId = Integer.valueOf(locator.getSingleValue());
		} else if (locator.getSingleDimensionValue("id") != null){
			filterId = Integer.valueOf(locator.getSingleDimensionValue("id"));
		}
		if (Objects.nonNull(filterId) && Objects.nonNull(filters.getFilters()) && filterId > 0 && filters.getFilters().size() >= filterId) {
			return filters.getFilters().get(filterId -1);
		}
		throw new NotFoundException("Could not find a webhook filter with that id");
	}

	private void checkWebHookReadPermission(String projectInternalId) {
		try {
			myPermissionChecker.checkProjectPermission(Permission.EDIT_PROJECT, projectInternalId);
		} catch (AuthorizationFailedException e) {
			throw new WebHookPermissionException(
					"Reading webhooks requires 'EDIT_PROJECT' permission.");
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
