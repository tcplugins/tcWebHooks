package webhook.teamcity.executor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.mute.MuteInfo;
import jetbrains.buildServer.users.SUser;
import lombok.AllArgsConstructor;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookExecutionException;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookConfig;

@AllArgsConstructor
public class WebHookThreadingExecutorImpl implements WebHookThreadingExecutor {
	private static final Logger LOG = Logger.getInstance(WebHookThreadingExecutorImpl.class.getName());

	private static final String CLASS_NAME = "WebHookThreadingExecutorImpl :: ";

	private final WebHookRunnerFactory webHookRunnerFactory;
	private final WebHookHistoryRepository webHookHistoryRepository;
	private final WebHookThreadingExecutorFactory webHookThreadingExecutorFactory;

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SQueuedBuild sQueuedBuild,
			BuildStateEnum state, String user, String comment, boolean isTest) {
		LOG.debug("WebHookThreadingExecutorImpl :: About to schedule runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey()+ " : " + state.getShortName());

		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, sQueuedBuild, state, user, comment, isTest);
		try {
			webHookThreadingExecutorFactory.getExecutorService().execute(runner);
		} catch (RejectedExecutionException ex) {
			handleException(webhook, whc, runner, state, ex, WebHookExecutionException.WEBHOOK_EXECUTION_QUEUE_FULL, WebHookExecutionException.WEBHOOK_EXECUTION_QUEUE_FULL_MESSAGE);
		} catch (Exception ex) {
			handleException(webhook, whc, runner, state, ex, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE);
		}

		LOG.debug("WebHookThreadingExecutorImpl :: Finished scheduling runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey()+ " : " + state.getShortName());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, BuildStateEnum state,
			WebHookResponsibilityHolder responsibilityHolder,
			boolean isTest) {
		LOG.debug("WebHookThreadingExecutorImpl :: About to schedule runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey()+ " : " + state.getShortName());

		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, state, responsibilityHolder, isTest);
		try {
			webHookThreadingExecutorFactory.getExecutorService().execute(runner);
		} catch (RejectedExecutionException ex) {
			handleException(webhook, whc, runner, state, ex, WebHookExecutionException.WEBHOOK_EXECUTION_QUEUE_FULL, WebHookExecutionException.WEBHOOK_EXECUTION_QUEUE_FULL_MESSAGE);
		} catch (Exception ex) {
			handleException(webhook, whc, runner, state, ex, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE);
		}

		LOG.debug("WebHookThreadingExecutorImpl :: Finished scheduling runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey() + " : " + state.getShortName());
	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, String user,
			String comment,
			boolean isTest, Map<String, String> extraAttributes) {
		LOG.debug("WebHookThreadingExecutorImpl :: About to schedule runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey()+ " : " + state.getShortName());

		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, sBuild, state, user, comment, isTest,
				extraAttributes);
		try {
			webHookThreadingExecutorFactory.getExecutorService().execute(runner);
		} catch (RejectedExecutionException ex) {
			handleException(webhook, whc, runner, state, ex, WebHookExecutionException.WEBHOOK_EXECUTION_QUEUE_FULL, WebHookExecutionException.WEBHOOK_EXECUTION_QUEUE_FULL_MESSAGE);
		} catch (Exception ex) {
			handleException(webhook, whc, runner, state, ex, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE);
		}

		LOG.debug("WebHookThreadingExecutorImpl :: Finished scheduling runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey() + " : " + state.getShortName());

	}

	@Override
	public void execute(WebHook webhook, WebHookConfig whc, SProject sProject,
			Map<MuteInfo, Collection<STest>> mutedOrUnmutedGroups, BuildStateEnum state, SUser user, boolean isTest) {

		LOG.debug("WebHookSerialExecutorImpl :: About to start runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey()+ " : " + state.getShortName());

		WebHookRunner runner = webHookRunnerFactory.getRunner(webhook, whc, sProject, state, mutedOrUnmutedGroups,
				isTest);
		try {
			webHookThreadingExecutorFactory.getExecutorService().execute(runner);
		} catch (RejectedExecutionException ex) {
			handleException(webhook, whc, runner, state, ex, WebHookExecutionException.WEBHOOK_EXECUTION_QUEUE_FULL, WebHookExecutionException.WEBHOOK_EXECUTION_QUEUE_FULL_MESSAGE);
		} catch (Exception ex) {
			handleException(webhook, whc, runner, state, ex, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE);
		}

		LOG.debug("WebHookSerialExecutorImpl :: Finished runner for webhook :: " +
				webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey()+ " : " + state.getShortName());

	}

	private void handleException(WebHook webhook, WebHookConfig whc, WebHookRunner runner, BuildStateEnum state, Exception ex, int webhookErrorCode, String webhookErrorMessage) {
	        webhook.getExecutionStats().setBuildState(state);
			webhook.getExecutionStats().setErrored(true);
			webhook.getExecutionStats().setRequestCompleted(
					webhookErrorCode,
					webhookErrorMessage + ex.getMessage());
			LOG.error(
					String.format(
							"%s trackingId: %s :: projectId: %s :: webhookId: %s :: templateId: %s, errorCode: %s, errorMessage: %s",
							CLASS_NAME,
							webhook.getExecutionStats().getTrackingIdAsString(),
							whc.getProjectExternalId(),
							whc.getUniqueKey(),
							whc.getPayloadTemplate(),
							webhookErrorCode,
							ex.getMessage()));
			LOG.debug(
					CLASS_NAME + webhook.getExecutionStats().getTrackingIdAsString() + " :: URL: " + webhook.getUrl(), ex);
			WebHookHistoryItem webHookHistoryItem = runner
					.buildWebHookHistoryItem(new WebHookErrorStatus(ex, ex.getMessage(),
							webhookErrorCode));
			webHookHistoryRepository.addHistoryItem(webHookHistoryItem);
		}
}
