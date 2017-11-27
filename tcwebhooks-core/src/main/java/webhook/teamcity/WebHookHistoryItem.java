package webhook.teamcity;

import org.apache.commons.codec.language.bm.Rule.RPattern;
import org.apache.commons.httpclient.HttpStatus;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import webhook.WebHook;
import webhook.WebHookExecutionStats;

@Data @AllArgsConstructor
public class WebHookHistoryItem {
	
	WebHook webHook;
	String projectId;
	String buildTypeId;
	Long buildId;
	int httpStatus =-1;
	String httpStatusDescription = "";
	WebHookExecutionStats webHookExecutionStats;
	WebHookErrorStatus webhookErrorStatus;
	
	@Data @AllArgsConstructor
	public static class WebHookErrorStatus {
		Exception exception;
		String message;
		int errorCode;
	}
	
	public WebHookHistoryItem(WebHook webHook, SBuild sBuild, WebHookErrorStatus errorStatus) {
		this.webHook = webHook;
		this.projectId = sBuild.getProjectId();
		this.buildTypeId = sBuild.getBuildTypeId();
		this.buildId = sBuild.getBuildId();
		this.webHookExecutionStats = webHook.getExecutionStats();
		if (errorStatus != null) {
			this.webhookErrorStatus = errorStatus;
			this.httpStatus = errorStatus.getErrorCode();
			this.httpStatusDescription = errorStatus.getMessage();
		} else if (webHook.getExecutionStats().getHttpStatusCode() != null){
			this.httpStatus = webHook.getExecutionStats().getHttpStatusCode();
			this.httpStatusDescription = this.httpStatus > 0 ? HttpStatus.getStatusText(this.httpStatus) : "WebHook Configuration Error";
		}
	}
	
	public WebHookHistoryItem(WebHook webHook, SBuildType sBuildType, WebHookErrorStatus errorStatus) {
		this.webHook = webHook;
		this.projectId = sBuildType.getProjectId();
		this.buildTypeId = sBuildType.getBuildTypeId();
		this.webHookExecutionStats = webHook.getExecutionStats();
		if (errorStatus != null) {
			this.webhookErrorStatus = errorStatus;
			this.httpStatus = errorStatus.getErrorCode();
			this.httpStatusDescription = errorStatus.getMessage();
		} else if (webHook.getExecutionStats().getHttpStatusCode() != null){
			this.httpStatus = webHook.getExecutionStats().getHttpStatusCode();
			this.httpStatusDescription = this.httpStatus > 0 ? HttpStatus.getStatusText(this.httpStatus) : "WebHook Configuration Error";
		}
	}
	
	public WebHookHistoryItem(WebHook webHook, SProject project, WebHookErrorStatus errorStatus) {
		this.webHook = webHook;
		this.projectId = project.getProjectId();
		this.webHookExecutionStats = webHook.getExecutionStats();
		if (errorStatus != null) {
			this.webhookErrorStatus = errorStatus;
			this.httpStatus = errorStatus.getErrorCode();
			this.httpStatusDescription = errorStatus.getMessage();
		} else if (webHook.getExecutionStats().getHttpStatusCode() != null){
			this.httpStatus = webHook.getExecutionStats().getHttpStatusCode();
			this.httpStatusDescription = this.httpStatus > 0 ? HttpStatus.getStatusText(this.httpStatus) : "WebHook Configuration Error";
		}
	}

}
