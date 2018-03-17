package webhook.teamcity.testing;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.joda.time.LocalDateTime;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.WebHook;
import webhook.WebHookExecutionStats;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookExecutionException;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.history.GeneralisedWebAddress;
import webhook.teamcity.history.GeneralisedWebAddressType;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;
import webhook.teamcity.settings.entity.WebHookTemplateJaxHelper;
import webhook.teamcity.settings.entity.WebHookTemplates;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public class WebHookUserRequestedExecutorImpl {
	
	private static final String WEB_HOOK_USER_REQUESTED_EXECUTOR_IMPL = "WebHookUserRequestedExecutorImpl";
	private final SBuildServer myServer;
	private final WebHookMainSettings myMainSettings;
	private final WebHookConfigFactory myWebHookConfigFactory;
	private final WebHookTemplateResolver myWebHookTemplateResolver;
	private final WebHookPayloadManager myWebHookPayloadManager;
	private final WebHookFactory myWebHookFactory;
	private final WebHookHistoryItemFactory myWebHookHistoryItemFactory;
	private final WebAddressTransformer myWebAddressTransformer;
	
	public WebHookUserRequestedExecutorImpl(
			SBuildServer server,
			WebHookMainSettings mainSettings,
			ProjectSettingsManager projectSettingsManager,
			WebHookConfigFactory webHookConfigFactory,
			WebHookFactory webHookFactory,
			WebHookTemplateResolver webHookTemplateResolver,
			WebHookPayloadManager webHookPayloadManager,
			WebHookHistoryItemFactory webHookHistoryItemFactory,
			WebAddressTransformer webAddressTransformer
			) {
		myServer = server;
		myMainSettings = mainSettings;
		myWebHookConfigFactory = webHookConfigFactory;
		myWebHookFactory = webHookFactory;
		myWebHookTemplateResolver = webHookTemplateResolver;
		myWebHookPayloadManager = webHookPayloadManager;
		myWebHookHistoryItemFactory = webHookHistoryItemFactory;
		myWebAddressTransformer = webAddressTransformer;
	}
	
	public WebHookHistoryItem requestWebHookExecution(WebHookExecutionRequest webHookExecutionRequest) {
		WebHookConfig webHookConfig = myWebHookConfigFactory.build(webHookExecutionRequest);
		
		WebHookContentBuilder contentBuilder = new WebHookContentBuilder(myWebHookPayloadManager, myWebHookTemplateResolver);
		WebHook wh = myWebHookFactory.getWebHook(webHookConfig, 
												myMainSettings.getProxyConfigForUrl(
														webHookConfig.getUrl()
														)
												);
		return executeWebHook(webHookExecutionRequest.getBuildId(), webHookExecutionRequest.getTestBuildState(), webHookConfig, contentBuilder, wh);	

		
	}

	/** Method that builds a template from the webHookTemplateExecutionRequest and then 
	 *  executes the webhook.
	 *  
	 *   Webhook config could be a URL from the user, or a webhook config id.
	 *   
	 * @param webHookTemplateExecutionRequest
	 * @return
	 */
	public WebHookHistoryItem requestWebHookExecution(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest) {
		WebHookConfig webHookConfig = null;

		try {
			webHookConfig = myWebHookConfigFactory.build(webHookTemplateExecutionRequest);
		} catch (WebHookConfigNotFoundException e) {
			SBuild sbuild = myServer.findBuildInstanceById(webHookTemplateExecutionRequest.getBuildId());
			WebHookExecutionStats stats = new WebHookExecutionStats();
			stats.setEnabled(false);
			stats.setErrored(true);
			stats.setStatusCode(WebHookExecutionException.WEBHOOK_CONFIGURATION_NOT_FOUND_EXCEPTION_ERROR_CODE);
			stats.setStatusReason(e.getMessage());
			return new WebHookHistoryItem(
					webHookTemplateExecutionRequest.getProjectId(), 
					myServer.getProjectManager().findProjectById(webHookTemplateExecutionRequest.getProjectId()),
					sbuild.getBuildTypeId(),
					sbuild.getBuildTypeName(),
					sbuild.getBuildTypeExternalId(),
					webHookTemplateExecutionRequest.getBuildId(),
					webHookConfig,
					stats,
					new WebHookErrorStatus(e, e.getMessage(), WebHookExecutionException.WEBHOOK_CONFIGURATION_NOT_FOUND_EXCEPTION_ERROR_CODE),
					new LocalDateTime(),
					getGeneralisedWebAddress(webHookTemplateExecutionRequest)
					);
		}
		
		// If we got a URL from the user, override the one on the config.
		if (webHookTemplateExecutionRequest.getUrl() != null && !webHookTemplateExecutionRequest.getUrl().trim().isEmpty()) {
			webHookConfig.setUrl(webHookTemplateExecutionRequest.getUrl());
		}
		
		WebHook wh = myWebHookFactory.getWebHook(webHookConfig, 
				myMainSettings.getProxyConfigForUrl(
						webHookConfig.getUrl()
						)
				);
		
		// We need an alternative WebHookTemplateManager. We'll use the injected payload manager, but create our
		// own jaxHelper. It's only used to persist the template, which we won't do in this stage.
		WebHookTemplateManager webHookTemplateManager = new WebHookTemplateManager(myWebHookPayloadManager, new NoOpJaxHelper());
		
		WebHookTemplateConfig webHookTemplateConfig = webHookTemplateExecutionRequest.toConfig();
		WebHookTemplateResolver webHookTemplateResolver = new NonDiscrimatoryTemplateResolver(webHookTemplateManager, webHookTemplateConfig);
		
		WebHookContentBuilder contentBuilder = new WebHookContentBuilder(myWebHookPayloadManager, webHookTemplateResolver );
		
		webHookTemplateManager.registerTemplateFormatFromXmlConfig(webHookTemplateConfig);
		
		return executeWebHook(webHookTemplateExecutionRequest.getBuildId(), webHookTemplateExecutionRequest.getTestBuildState(), webHookConfig, contentBuilder, wh);
	}

	private GeneralisedWebAddress getGeneralisedWebAddress(
			WebHookTemplateExecutionRequest webHookTemplateExecutionRequest) {
		try {
			if (webHookTemplateExecutionRequest.getUrl() != null && !webHookTemplateExecutionRequest.getUrl().trim().isEmpty()) {
				return myWebAddressTransformer.getGeneralisedHostName(new URL(webHookTemplateExecutionRequest.getUrl()));
			} else {
				return myWebAddressTransformer.getGeneralisedHostName(new URL("http://undefined"));
			}
		} catch (MalformedURLException e) {
			return GeneralisedWebAddress.build("undefined", GeneralisedWebAddressType.HOST_ADDRESS);
		}
	}

	private WebHookHistoryItem executeWebHook(Long buildId, BuildStateEnum testBuildState,
			WebHookConfig webHookConfig, WebHookContentBuilder contentBuilder, WebHook wh) {
		SBuild sRunningBuild = myServer.findBuildInstanceById(buildId);
		try {
			wh = contentBuilder.buildWebHookContent(wh, webHookConfig, sRunningBuild, 
						testBuildState, false);
			
			Loggers.SERVER.debug("#### CONTENT #### " + wh.getPayload());
			WebHookListener.doPost(wh, webHookConfig.getPayloadFormat());
			return myWebHookHistoryItemFactory.getWebHookHistoryItem(
							webHookConfig,
							wh.getExecutionStats(), 
							sRunningBuild,
							null);
		} catch (WebHookExecutionException ex){
			wh.getExecutionStats().setErrored(true);
			wh.getExecutionStats().setRequestCompleted(ex.getErrorCode(), ex.getMessage());
			Loggers.SERVER.error(WEB_HOOK_USER_REQUESTED_EXECUTOR_IMPL + ex.getMessage());
			Loggers.SERVER.debug(ex);
			return myWebHookHistoryItemFactory.getWebHookHistoryItem(
							webHookConfig,
							wh.getExecutionStats(), 
							sRunningBuild,
							new WebHookErrorStatus(ex, ex.getMessage(), ex.getErrorCode())
				);
		} catch (Exception ex){
			wh.getExecutionStats().setErrored(true);
			wh.getExecutionStats().setRequestCompleted(WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE, WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_MESSAGE + ex.getMessage());
			Loggers.SERVER.error(WEB_HOOK_USER_REQUESTED_EXECUTOR_IMPL + wh.getExecutionStats().getTrackingIdAsString() + " :: " + ex.getMessage());
			Loggers.SERVER.debug(WEB_HOOK_USER_REQUESTED_EXECUTOR_IMPL + wh.getExecutionStats().getTrackingIdAsString() + " :: URL: " + wh.getUrl(), ex);
			return myWebHookHistoryItemFactory.getWebHookHistoryItem(
							webHookConfig,
							wh.getExecutionStats(), 
							sRunningBuild,
							new WebHookErrorStatus(ex, ex.getMessage(), WebHookExecutionException.WEBHOOK_UNEXPECTED_EXCEPTION_ERROR_CODE)
					);					
		}
	}

	private static class NonDiscrimatoryTemplateResolver extends WebHookTemplateResolver {

		private WebHookTemplateConfig myWebHookTemplateConfig;

		public NonDiscrimatoryTemplateResolver(WebHookTemplateManager webHookTemplateManager, WebHookTemplateConfig webHookTemplateConfig) {
			super(webHookTemplateManager);
			myWebHookTemplateConfig = webHookTemplateConfig;
		}
		
		@Override
		public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SBuildType buildType,
				String webhookFormat, String templateName) {
			if (myWebHookTemplateConfig.getDefaultTemplate().isUseTemplateTextForBranch()) {
				return WebHookTemplateContent.create(
						state.getShortName(), 
						myWebHookTemplateConfig.getDefaultTemplate().getTemplateContent(), 
						true, 
						myWebHookTemplateConfig.getPreferredDateTimeFormat()
					);
			}
			return WebHookTemplateContent.create(
					state.getShortName(), 
					myWebHookTemplateConfig.getDefaultBranchTemplate().getTemplateContent(), 
					true, 
					myWebHookTemplateConfig.getPreferredDateTimeFormat()
				);
		}
		
		@Override
		public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SBuildType buildType,
				String webhookFormat, String templateName) {
			return WebHookTemplateContent.create(
					state.getShortName(), 
					myWebHookTemplateConfig.getDefaultBranchTemplate().getTemplateContent(), 
					true, 
					myWebHookTemplateConfig.getPreferredDateTimeFormat()
				);
			}
		
	}
}
