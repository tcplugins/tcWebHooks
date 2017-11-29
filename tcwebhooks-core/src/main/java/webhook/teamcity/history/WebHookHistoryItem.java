package webhook.teamcity.history;

import org.joda.time.LocalDateTime;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import webhook.WebHookExecutionStats;

@Data @AllArgsConstructor
public class WebHookHistoryItem {
	
	String projectId;
	String buildTypeId;
	Long buildId;
	WebHookExecutionStats webHookExecutionStats;
	WebHookErrorStatus webhookErrorStatus;
	LocalDateTime timestamp;
	
	@Data @AllArgsConstructor
	public static class WebHookErrorStatus {
		Exception exception;
		String message;
		int errorCode;
	}
	
	public WebHookHistoryItem(WebHookExecutionStats webHookExecutionStats, SBuild sBuild, WebHookErrorStatus errorStatus) {
		this.projectId = sBuild.getProjectId();
		this.buildTypeId = sBuild.getBuildTypeId();
		this.buildId = sBuild.getBuildId();
		this.webHookExecutionStats = webHookExecutionStats;
		checkAndSetHttpStatusInfo(webHookExecutionStats, errorStatus);
	}
	
	public WebHookHistoryItem(WebHookExecutionStats webHookExecutionStats, SBuildType sBuildType, WebHookErrorStatus errorStatus) {
		this.projectId = sBuildType.getProjectId();
		this.buildTypeId = sBuildType.getBuildTypeId();
		this.webHookExecutionStats = webHookExecutionStats;
		checkAndSetHttpStatusInfo(webHookExecutionStats, errorStatus);
	}
	
	public WebHookHistoryItem(WebHookExecutionStats webHookExecutionStats, SProject project, WebHookErrorStatus errorStatus) {
		this.projectId = project.getProjectId();
		this.webHookExecutionStats = webHookExecutionStats;
		checkAndSetHttpStatusInfo(webHookExecutionStats, errorStatus);
	}

	private void checkAndSetHttpStatusInfo(WebHookExecutionStats webHookExecutionStats, WebHookErrorStatus errorStatus) {
		if (webHookExecutionStats.getRequestCompletedTimeStamp() != null) {
			this.timestamp = LocalDateTime.fromDateFields(webHookExecutionStats.getRequestCompletedTimeStamp());
		} else {
			this.timestamp = LocalDateTime.fromDateFields(webHookExecutionStats.getInitTimeStamp());
		}
		if (errorStatus != null) {
			this.webhookErrorStatus = errorStatus;
		}
	}

}
