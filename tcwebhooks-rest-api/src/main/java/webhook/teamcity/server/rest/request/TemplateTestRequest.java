package webhook.teamcity.server.rest.request;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jetbrains.annotations.NotNull;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.AuthorizationFailedException;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.server.rest.data.TemplateDataProvider;
import webhook.teamcity.server.rest.errors.TemplatePermissionException;
import webhook.teamcity.server.rest.errors.UnprocessableEntityException;
import webhook.teamcity.server.rest.model.template.ErrorResult;
import webhook.teamcity.server.rest.model.template.TemplateTestExecutionRequest;
import webhook.teamcity.server.rest.model.template.TemplateTestHistoryItem;
import webhook.teamcity.server.rest.model.template.TemplateTestHistoryItem.ErrorStatus;
import webhook.teamcity.server.rest.request.validator.ExecuteWebHookTemplateRequestValidator;
import webhook.teamcity.server.rest.request.validator.PreviewWebHookTemplateRequestValidator;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.testing.model.WebHookRenderResult;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

@Path(TemplateTestRequest.API_TEMPLATE_TEST_URL)
@Api(value = "Template testing", description = "Execute or preview webhook template rendering with live server context.")
public class TemplateTestRequest {
	
	public static final String API_TEMPLATE_TEST_URL = Constants.API_URL + "/test/template";
	private static final Permission templateTestPermission = Permission.CHANGE_SERVER_SETTINGS;
	
	@Context @NotNull private TemplateDataProvider myTemplateDataProvider;
	@Context @NotNull private PermissionChecker myPermissionChecker;
	
	@POST
	@Path("/execute")
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	@ApiOperation(value = "Execute webhook template test", notes = "Renders and executes the supplied template test payload against the selected build context and returns execution history details.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Template executed", response = TemplateTestHistoryItem.class),
		@ApiResponse(code = 422, message = "Validation failed", response = ErrorResult.class),
		@ApiResponse(code = 403, message = "Insufficient permissions")
	})
	public TemplateTestHistoryItem executeWebHookTemplateRequest(
			@ApiParam(value = "Template execution request payload", required = true) TemplateTestExecutionRequest executionRequest) {
		
		checkTemplateTestPermission();
		
		Validator v = new ExecuteWebHookTemplateRequestValidator();
		Errors e = new BeanPropertyBindingResult(executionRequest, "executionRequest");
		v.validate(executionRequest, e);
		
		if (e.hasErrors()) {
			throw new UnprocessableEntityException("validation error", buildErrors(e));
		}
		
        WebHookTemplateExecutionRequest templateExecutionRequest = WebHookTemplateExecutionRequest
        		.builder()
        		.buildId(Long.valueOf(executionRequest.getBuildId()))
        		.projectExternalId(executionRequest.getProjectExternalId())
        		.defaultBranchTemplate(new WebHookTemplateConfig.WebHookTemplateBranchText(executionRequest.getBranchTemplateText()))
        		.defaultTemplate(new WebHookTemplateConfig.WebHookTemplateText(executionRequest.isUseTemplateTextForBranch(), executionRequest.getTemplateText()))
        		.format(executionRequest.getFormat())
        		.url(executionRequest.getUrl())
        		.uniqueKey(executionRequest.getWebhookId())
        		.testBuildState(BuildStateEnum.findBuildState(executionRequest.getBuildStateName()))
        		.build();
		
        WebHookHistoryItem webHookHistoryItem = myTemplateDataProvider.getWebHookUserRequestedExecutor().requestWebHookExecution(templateExecutionRequest);
        
        ErrorStatus error = null;
        if (webHookHistoryItem.getWebhookErrorStatus() != null) {
        	error = new ErrorStatus(webHookHistoryItem.getWebhookErrorStatus().getMessage(), webHookHistoryItem.getWebhookErrorStatus().getErrorCode());
        }
        
        return TemplateTestHistoryItem
					.builder()
					.dateTime(webHookHistoryItem.getTimestamp().toString())
					.trackingId(webHookHistoryItem.getWebHookExecutionStats().getTrackingIdAsString())
					.url(getUrl(webHookHistoryItem))
					.executionTime(String.valueOf(webHookHistoryItem.getWebHookExecutionStats().getTotalExecutionTime()) + " ms")
					.statusCode(webHookHistoryItem.getWebHookExecutionStats().getStatusCode())
					.statusReason(webHookHistoryItem.getWebHookExecutionStats().getStatusReason())
					.error(error)
					.build();
	}

	@POST
	@Path("/preview")
	@Consumes({"application/xml", "application/json"})
	@Produces({"text/html"})
	@ApiOperation(value = "Preview webhook template output", notes = "Renders template output without performing webhook execution. Returns rendered HTML preview.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Template rendered preview"),
		@ApiResponse(code = 406, message = "Template rendering failed"),
		@ApiResponse(code = 422, message = "Validation failed", response = ErrorResult.class),
		@ApiResponse(code = 403, message = "Insufficient permissions")
	})
	public Response previewWebHookTemplateRequest(
			@ApiParam(value = "Template execution request payload", required = true) TemplateTestExecutionRequest executionRequest) {
		
		checkTemplateTestPermission();
		
		Validator v = new PreviewWebHookTemplateRequestValidator();
		Errors e = new BeanPropertyBindingResult(executionRequest, "previewRequest");
		v.validate(executionRequest, e);
		
		if (e.hasErrors()) {
			throw new UnprocessableEntityException("validation error", buildErrors(e));
		}
		
		WebHookTemplateExecutionRequest templateExecutionRequest = WebHookTemplateExecutionRequest
				.builder()
				.buildId(Long.valueOf(executionRequest.getBuildId()))
				.projectExternalId(executionRequest.getProjectExternalId())
				.defaultBranchTemplate(new WebHookTemplateConfig.WebHookTemplateBranchText(executionRequest.getBranchTemplateText()))
				.defaultTemplate(new WebHookTemplateConfig.WebHookTemplateText(executionRequest.isUseTemplateTextForBranch(), executionRequest.getTemplateText()))
				.format(executionRequest.getFormat())
				.testBuildState(BuildStateEnum.findBuildState(executionRequest.getBuildStateName()))
				.url("")
				.build();
		
		WebHookRenderResult webHookRenderResult = myTemplateDataProvider.getWebHookUserRequestedExecutor().requestWebHookPreview(templateExecutionRequest);
		
		if (!webHookRenderResult.getErrored()) {
			Response.ResponseBuilder rb = Response.ok(webHookRenderResult.getHtml());
			return rb.build();
		} else {
			Response.ResponseBuilder rb = Response.status(Status.NOT_ACCEPTABLE).entity(webHookRenderResult.getHtml());
			return rb.header("x-tcwehooks-template-rendering-errored", "true").build();
		}
	}
	
	private void checkTemplateTestPermission() {
		try {
			myPermissionChecker.checkGlobalPermission(templateTestPermission);
		} catch (AuthorizationFailedException e) {
			throw new TemplatePermissionException("Testing templates requires permission 'CHANGE_SERVER_SETTINGS'");
		}
	}

	private ErrorResult buildErrors(Errors validationErrors) {
		ErrorResult e = new ErrorResult();
		for (FieldError error : validationErrors.getFieldErrors()) {
			e.addError(error.getField(), error.getCode());
		}
		return e;
	}

	private String getUrl(WebHookHistoryItem webHookConfig) {
		if (webHookConfig != null &&
				webHookConfig.getWebHookExecutionStats() != null &&
				webHookConfig.getWebHookExecutionStats().getUrl() != null &&
				!webHookConfig.getWebHookExecutionStats().getUrl().trim().isEmpty() )
		{
			return webHookConfig.getWebHookExecutionStats().getUrl();
			
		} else if (webHookConfig != null &&
				webHookConfig.getUrl() != null &&
				webHookConfig.getUrl().trim().isEmpty()) {
			return webHookConfig.getUrl();
		}
		return "";
	}
	
}
