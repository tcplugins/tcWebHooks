package webhook.teamcity.executor;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.impl.EnglishReasonPhraseCatalog;

import lombok.Getter;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookExecutionException;
import webhook.teamcity.WebHookHttpExecutionException;
import webhook.teamcity.WebHookHttpResponseException;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookConfig;

public abstract class AbstractWebHookExecutor implements WebHookRunner {
	
	private static final String CLASS_NAME = "AbstractWebHookExecutor :: ";
	protected final WebHookContentBuilder webHookContentBuilder;
	protected final WebHookHistoryRepository webHookHistoryRepository;
	protected final WebHookHistoryItemFactory webHookHistoryItemFactory;
	protected final WebHookConfig whc;
	protected final BuildStateEnum state;
	protected boolean overrideIsEnabled = false;
	
	@Getter
	private WebHookHistoryItem webHookHistoryItem;
	
	protected WebHook webhook;
	protected boolean isTest;
	
	protected AbstractWebHookExecutor(
			WebHookContentBuilder webHookContentBuilder,
			WebHookHistoryRepository webHookHistoryRepository,
			WebHookHistoryItemFactory webHookHistoryItemFactory,
			WebHookConfig whc,
			BuildStateEnum state,
			boolean overrideIsEnabled,
			WebHook webhook,
			boolean isTest
			) {
		this.webHookContentBuilder = webHookContentBuilder;
		this.webHookHistoryRepository = webHookHistoryRepository;
		this.webHookHistoryItemFactory = webHookHistoryItemFactory;
		this.whc = whc;
		this.state = state;
		this.overrideIsEnabled = overrideIsEnabled;
		this.webhook = webhook;
		this.isTest = isTest;
	}
	
	@Override
	public void run() {
		Loggers.SERVER.debug("AbstractWebHookExecutor :: Starting runner for webhook: " + webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey() + " : " + state.getShortName());
		
		try {
			this.webhook = getWebHookContent();
			
			doPost(webhook, whc.getPayloadTemplate());
			this.webHookHistoryItem = buildWebHookHistoryItem(null);
			webHookHistoryRepository.addHistoryItem(this.webHookHistoryItem);

		} catch (WebHookExecutionException ex){
			webhook.getExecutionStats().setErrored(true);
			webhook.getExecutionStats().setRequestCompleted(ex.getErrorCode(), ex.getMessage());
			Loggers.SERVER.error(
					String.format("%s trackingId: %s :: projectId: %s :: webhookId: %s :: templateId: %s, errorCode: %s, errorMessage: %s", 
							CLASS_NAME, 
							webhook.getExecutionStats().getTrackingIdAsString(),
							whc.getProjectExternalId(),
							whc.getUniqueKey(),
							whc.getPayloadTemplate(),
							ex.getErrorCode(),
							ex.getMessage()));
			Loggers.SERVER.debug(CLASS_NAME + webhook.getExecutionStats().getTrackingIdAsString() + " :: URL: " + webhook.getUrl(), ex);
			this.webHookHistoryItem = buildWebHookHistoryItem(new WebHookErrorStatus(ex, ex.getMessage(), ex.getErrorCode()));
			webHookHistoryRepository.addHistoryItem(webHookHistoryItem);
			errorCallback(ex);
		
		} catch (Exception ex){
			webhook.getExecutionStats().setErrored(true);
			webhook.getExecutionStats().setRequestCompleted(WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE + ex.getMessage());
			Loggers.SERVER.error(
					String.format("%s trackingId: %s :: projectId: %s :: webhookId: %s :: templateId: %s, errorCode: %s, errorMessage: %s", 
							CLASS_NAME, 
							webhook.getExecutionStats().getTrackingIdAsString(),
							whc.getProjectExternalId(),
							whc.getUniqueKey(),
							whc.getPayloadTemplate(),
							WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE,
							ex.getMessage()));			Loggers.SERVER.debug(CLASS_NAME + webhook.getExecutionStats().getTrackingIdAsString() + " :: URL: " + webhook.getUrl(), ex);
			this.webHookHistoryItem = buildWebHookHistoryItem(new WebHookErrorStatus(ex, ex.getMessage(), 
					WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE));
			webHookHistoryRepository.addHistoryItem(this.webHookHistoryItem);
			errorCallback(new RuntimeException(ex));
		}
		
		Loggers.SERVER.debug("AbstractWebHookExecutor :: Finishing runner for webhook: " + webhook.getExecutionStats().getTrackingIdAsString() + " : " + whc.getUniqueKey() + " : " + state.getShortName());
	}

	
	
	
	/** doPost
	 * 
	 * @param wh
	 * @param payloadTemplate
	 */
	public static void doPost(WebHook wh, String payloadTemplate) {
		boolean shouldHideSecureData = wh.shouldHideSecureData();
		try {
			if (Boolean.TRUE.equals(wh.isEnabled())){
				wh.post();
				Loggers.SERVER.info(CLASS_NAME + " :: WebHook triggered : " 
						+ determineSecureUrl(wh, shouldHideSecureData) + " using template " + payloadTemplate 
						+ " returned " + wh.getStatus() 
						+ " " + wh.getErrorReason());
				if (Loggers.SERVER.isDebugEnabled()) {
					if (shouldHideSecureData) {
						Loggers.SERVER.debug(CLASS_NAME + ":doPost :: Hiding content payload because it may contain secured values. To log content to this log file uncheck 'Secure Values' in the WebHook edit dialog.");
					} else if (wh.getExecutionStats().isSecureValueAccessed()) {
						Loggers.SERVER.debug(CLASS_NAME + ":doPost :: Logging content payload even though it may contain secured values. To hide content in this log file check 'Secure Values' in the WebHook edit dialog.\n--- begin webhook payload ---\n" + wh.getPayload() + "\n--- end webhook payload ---");
						Loggers.SERVER.debug("WebHook execution stats: " + wh.getExecutionStats().toString());
					} else {
						Loggers.SERVER.debug(CLASS_NAME + ":doPost :: Logging content payload because it contains no secured values.\n--- begin webhook payload ---\n" + wh.getPayload() + "\n--- end webhook payload ---");
					}
				}
				if (Boolean.TRUE.equals(wh.isErrored())){
					Loggers.SERVER.error(wh.getErrorReason());
				}
				if (wh.getStatus() == null) {
					Loggers.SERVER.warn(CLASS_NAME + wh.getParam("projectId") + " WebHook (url: " + determineSecureUrl(wh, shouldHideSecureData) + " proxy: " + wh.getProxyHost() + ":" + wh.getProxyPort()+") returned HTTP status " + wh.getStatus().toString());
					throw new WebHookHttpExecutionException("WebHook endpoint returned null response code");
				} else if (wh.getStatus() < HttpStatus.SC_OK || wh.getStatus() >= HttpStatus.SC_MULTIPLE_CHOICES) {
					Loggers.SERVER.warn(CLASS_NAME + wh.getParam("projectId") + " WebHook (url: " + determineSecureUrl(wh, shouldHideSecureData) + " proxy: " + wh.getProxyHost() + ":" + wh.getProxyPort()+") returned HTTP status " + wh.getStatus().toString());
					throw new WebHookHttpResponseException("WebHook endpoint returned non-2xx response (" + EnglishReasonPhraseCatalog.INSTANCE.getReason(wh.getStatus(), null) +")", wh.getStatus());
				}
			} else {
				if (Loggers.SERVER.isDebugEnabled()) Loggers.SERVER.debug("WebHook NOT triggered: " + wh.getDisabledReason() + " " +  wh.getParam("buildStatus") + " " + determineSecureUrl(wh, shouldHideSecureData));	
			}
		} catch (FileNotFoundException e) {
			Loggers.SERVER.warn(CLASS_NAME + ":doPost :: " 
					+ "A FileNotFoundException occurred while attempting to execute WebHook (" + determineSecureUrl(wh, shouldHideSecureData) + "). See the following debug stacktrace");
			Loggers.SERVER.debug(e);
			throw new WebHookHttpExecutionException("A FileNotFoundException occurred while attempting to execute WebHook (" + determineSecureUrl(wh, shouldHideSecureData) + ")", e);
		} catch (IOException e) {
			Loggers.SERVER.warn(CLASS_NAME + ":doPost :: " 
					+ "An IOException occurred while attempting to execute WebHook (" + determineSecureUrl(wh, shouldHideSecureData) + "). See the following debug stacktrace");
			Loggers.SERVER.debug(e);
			throw new WebHookHttpExecutionException("Error " + e.getMessage() + " occurred while attempting to execute WebHook.", e);
		}
		
	}

	private static String determineSecureUrl(WebHook wh, boolean shouldHideSecureData) {
		return shouldHideSecureData ? "********" : wh.getUrl();
	}
	protected abstract WebHook getWebHookContent();

	protected void errorCallback(RuntimeException exception) {
		// Do nothing by default
	}

}
