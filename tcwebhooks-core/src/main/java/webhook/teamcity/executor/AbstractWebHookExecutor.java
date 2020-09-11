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
	
	public AbstractWebHookExecutor(
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
		Loggers.SERVER.debug("AbstractWebHookExecutor :: Starting runner for webhook: " + whc.getUniqueKey() 
						+  " : " + webhook.getExecutionStats().getTrackingIdAsString());
		
		try {
			this.webhook = getWebHookContent();
			
			doPost(webhook, whc.getPayloadTemplate());
			Loggers.ACTIVITIES.debug(CLASS_NAME + whc.getPayloadTemplate());
			this.webHookHistoryItem = buildWebHookHistoryItem(null);
			webHookHistoryRepository.addHistoryItem(this.webHookHistoryItem);

		} catch (WebHookExecutionException ex){
			webhook.getExecutionStats().setErrored(true);
			webhook.getExecutionStats().setRequestCompleted(ex.getErrorCode(), ex.getMessage());
			Loggers.SERVER.error(CLASS_NAME + webhook.getExecutionStats().getTrackingIdAsString() + " :: " + ex.getMessage());
			Loggers.SERVER.debug(CLASS_NAME + webhook.getExecutionStats().getTrackingIdAsString() + " :: URL: " + webhook.getUrl(), ex);
			this.webHookHistoryItem = buildWebHookHistoryItem(new WebHookErrorStatus(ex, ex.getMessage(), ex.getErrorCode()));
			webHookHistoryRepository.addHistoryItem(webHookHistoryItem);
		
		} catch (Exception ex){
			webhook.getExecutionStats().setErrored(true);
			webhook.getExecutionStats().setRequestCompleted(WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE + ex.getMessage());
			Loggers.SERVER.error(CLASS_NAME + webhook.getExecutionStats().getTrackingIdAsString() + " :: " + ex.getMessage());
			Loggers.SERVER.debug(CLASS_NAME + webhook.getExecutionStats().getTrackingIdAsString() + " :: URL: " + webhook.getUrl(), ex);
			this.webHookHistoryItem = buildWebHookHistoryItem(new WebHookErrorStatus(ex, ex.getMessage(), 
					WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE));
			webHookHistoryRepository.addHistoryItem(this.webHookHistoryItem);
		}
		
		Loggers.SERVER.debug("AbstractWebHookExecutor :: Finishing runner for webhook: " + whc.getUniqueKey() 
		+  " : " + webhook.getExecutionStats().getTrackingIdAsString());
	}

	
	
	
	/** doPost
	 * 
	 * @param wh
	 * @param payloadTemplate
	 */
	public static void doPost(WebHook wh, String payloadTemplate) {
		try {
			if (Boolean.TRUE.equals(wh.isEnabled())){
				wh.post();
				Loggers.SERVER.info(CLASS_NAME + " :: WebHook triggered : " 
						+ wh.getUrl() + " using template " + payloadTemplate 
						+ " returned " + wh.getStatus() 
						+ " " + wh.getErrorReason());	
				Loggers.SERVER.debug(CLASS_NAME + ":doPost :: content dump: " + wh.getPayload());
				if (Loggers.SERVER.isDebugEnabled()) Loggers.SERVER.debug("WebHook execution stats: " + wh.getExecutionStats().toString());
				if (Boolean.TRUE.equals(wh.isErrored())){
					Loggers.SERVER.error(wh.getErrorReason());
				}
				if (wh.getStatus() == null) {
					Loggers.SERVER.warn(CLASS_NAME + wh.getParam("projectId") + " WebHook (url: " + wh.getUrl() + " proxy: " + wh.getProxyHost() + ":" + wh.getProxyPort()+") returned HTTP status " + wh.getStatus().toString());
					throw new WebHookHttpExecutionException("WebHook endpoint returned null response code");
				} else if (wh.getStatus() < HttpStatus.SC_OK || wh.getStatus() >= HttpStatus.SC_MULTIPLE_CHOICES) {
					Loggers.SERVER.warn(CLASS_NAME + wh.getParam("projectId") + " WebHook (url: " + wh.getUrl() + " proxy: " + wh.getProxyHost() + ":" + wh.getProxyPort()+") returned HTTP status " + wh.getStatus().toString());
					throw new WebHookHttpResponseException("WebHook endpoint returned non-2xx response (" + EnglishReasonPhraseCatalog.INSTANCE.getReason(wh.getStatus(), null) +")", wh.getStatus());
				}
			} else {
				if (Loggers.SERVER.isDebugEnabled()) Loggers.SERVER.debug("WebHook NOT triggered: " + wh.getDisabledReason() + " " +  wh.getParam("buildStatus") + " " + wh.getUrl());	
			}
		} catch (FileNotFoundException e) {
			Loggers.SERVER.warn(CLASS_NAME + ":doPost :: " 
					+ "A FileNotFoundException occurred while attempting to execute WebHook (" + wh.getUrl() + "). See the following debug stacktrace");
			Loggers.SERVER.debug(e);
			throw new WebHookHttpExecutionException("A FileNotFoundException occurred while attempting to execute WebHook (" + wh.getUrl() + ")", e);
		} catch (IOException e) {
			Loggers.SERVER.warn(CLASS_NAME + ":doPost :: " 
					+ "An IOException occurred while attempting to execute WebHook (" + wh.getUrl() + "). See the following debug stacktrace");
			Loggers.SERVER.debug(e);
			throw new WebHookHttpExecutionException("Error " + e.getMessage() + " occurred while attempting to execute WebHook.", e);
		}
		
	}

	protected abstract WebHook getWebHookContent();
	protected abstract WebHookHistoryItem buildWebHookHistoryItem(WebHookErrorStatus errorStatus);

}
