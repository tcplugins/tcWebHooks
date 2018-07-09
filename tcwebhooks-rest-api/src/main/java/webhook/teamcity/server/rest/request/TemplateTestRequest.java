package webhook.teamcity.server.rest.request;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.errors.AuthorizationFailedException;
import jetbrains.buildServer.serverSide.auth.Permission;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.server.rest.errors.TemplatePermissionException;
import webhook.teamcity.server.rest.model.template.TemplateTestExecutionRequest;
import webhook.teamcity.server.rest.model.template.TemplateTestHistoryItem;
import webhook.teamcity.server.rest.model.template.TemplateTestHistoryItem.ErrorStatus;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.testing.WebHookUserRequestedExecutor;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

@Path(TemplateTestRequest.API_TEMPLATE_TEST_URL)
public class TemplateTestRequest {
	
	public static final String API_TEMPLATE_TEST_URL = Constants.API_URL + "/test/template";
	private static final Permission templateTestPermission = Permission.CHANGE_SERVER_SETTINGS;
	
	@Context @NotNull private WebHookUserRequestedExecutor myWebHookUserRequestedExecutor;
	@Context @NotNull private PermissionChecker myPermissionChecker;
	
	@POST
	@Consumes({"application/xml", "application/json"})
	@Produces({"application/xml", "application/json"})
	public TemplateTestHistoryItem executeWebHookTemplateRequest(TemplateTestExecutionRequest executionRequest) {
		
		checkTemplateTestPermission();
		
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
		
        WebHookHistoryItem webHookHistoryItem = myWebHookUserRequestedExecutor.requestWebHookExecution(templateExecutionRequest);
        
        ErrorStatus error = null;
        if (webHookHistoryItem.getWebhookErrorStatus() != null) {
        	error = new ErrorStatus(webHookHistoryItem.getWebhookErrorStatus().getMessage(), webHookHistoryItem.getWebhookErrorStatus().getErrorCode());
        }
        
        return TemplateTestHistoryItem
					.builder()
					.datetime(webHookHistoryItem.getTimestamp().toString())
					.trackingId(webHookHistoryItem.getWebHookExecutionStats().getTrackingIdAsString())
					.url(webHookHistoryItem.getWebHookConfig().getUrl())
					.executionTime(String.valueOf(webHookHistoryItem.getWebHookExecutionStats().getTotalExecutionTime()) + " ms")
					.statusCode(webHookHistoryItem.getWebHookExecutionStats().getStatusCode())
					.statusReason(webHookHistoryItem.getWebHookExecutionStats().getStatusReason())
					.error(error)
					.build();
	}
	
	private void checkTemplateTestPermission() {
		try {
			myPermissionChecker.checkGlobalPermission(templateTestPermission);
		} catch (AuthorizationFailedException e) {
			throw new TemplatePermissionException("Testing templates requires permission 'CHANGE_SERVER_SETTINGS'");
		}
	}

}
