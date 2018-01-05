package webhook.teamcity.history;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDateTime;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import webhook.WebHookExecutionStats;
import webhook.teamcity.reporting.GeneralisedWebAddress;
import webhook.teamcity.settings.WebHookConfig;

@Data @AllArgsConstructor
public class WebHookHistoryItem {
	
	@NotNull  private final String projectId;
	@Nullable private SProject sProject;
	@Nullable private String buildTypeId;
	@NotNull  private String buildTypeName;
	@NotNull  private String buildTypeExternalId;
	@Nullable private Long buildId;
	@NotNull  private final WebHookConfig webHookConfig;
	@NotNull  private final WebHookExecutionStats webHookExecutionStats;
	@Nullable private final WebHookErrorStatus webhookErrorStatus;
	@NotNull  private final LocalDateTime timestamp;
	@Nullable private GeneralisedWebAddress generalisedWebAddress;
	
	@Data @AllArgsConstructor
	public static class WebHookErrorStatus {
		Exception exception;
		String message;
		int errorCode;
	}
	
	public String getUrl() {
		if (generalisedWebAddress != null) {
			return generalisedWebAddress.getGeneralisedAddress();
		}
		return "unknown";
	}
	
	public String getUrlHash() {
		if (generalisedWebAddress != null) {
			return generalisedWebAddress.getHashedGeneralisedAddress();
		}
		return "unknown";
	}
	
	protected WebHookHistoryItem(WebHookConfig whc, WebHookExecutionStats webHookExecutionStats, SBuild sBuild, WebHookErrorStatus errorStatus) {
		this.projectId = sBuild.getProjectId();
		this.buildTypeId = sBuild.getBuildTypeId();
		this.buildId = sBuild.getBuildId();
		this.webHookConfig = whc;
		this.webHookExecutionStats = webHookExecutionStats;
		this.timestamp = findTimeStamp(webHookExecutionStats);
		this.webhookErrorStatus = checkAndSetHttpStatusInfo(errorStatus);
	}
	
	protected WebHookHistoryItem(WebHookConfig whc, WebHookExecutionStats webHookExecutionStats, SBuildType sBuildType, WebHookErrorStatus errorStatus) {
		this.projectId = sBuildType.getProjectId();
		this.buildTypeId = sBuildType.getBuildTypeId();
		this.webHookConfig = whc;
		this.webHookExecutionStats = webHookExecutionStats;
		this.timestamp = findTimeStamp(webHookExecutionStats);
		this.webhookErrorStatus = checkAndSetHttpStatusInfo(errorStatus);
	}
	
	protected WebHookHistoryItem(WebHookConfig whc, WebHookExecutionStats webHookExecutionStats, SProject project, WebHookErrorStatus errorStatus) {
		this.projectId = project.getProjectId();
		this.webHookConfig = whc;
		this.webHookExecutionStats = webHookExecutionStats;
		this.timestamp = findTimeStamp(webHookExecutionStats);
		this.webhookErrorStatus = checkAndSetHttpStatusInfo(errorStatus);
	}

	private WebHookErrorStatus checkAndSetHttpStatusInfo(WebHookErrorStatus errorStatus) {
		if (errorStatus != null) {
			return errorStatus;
		}
		return null;
	}

	private LocalDateTime findTimeStamp(WebHookExecutionStats webHookExecutionStats) {
		if (webHookExecutionStats.getRequestCompletedTimeStamp() != null) {
			return LocalDateTime.fromDateFields(webHookExecutionStats.getRequestCompletedTimeStamp());
		} else {
			return LocalDateTime.fromDateFields(webHookExecutionStats.getInitTimeStamp());
		}
	}

}
