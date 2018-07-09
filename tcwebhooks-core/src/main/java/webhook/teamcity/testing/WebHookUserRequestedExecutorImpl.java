package webhook.teamcity.testing;

import java.net.MalformedURLException;
import java.net.URL;

import org.joda.time.LocalDateTime;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
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
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public class WebHookUserRequestedExecutorImpl implements WebHookUserRequestedExecutor {
	
	private static final String WEB_HOOK_USER_REQUESTED_EXECUTOR_IMPL = "WebHookUserRequestedExecutorImpl";
	private final SBuildServer myServer;
	private final WebHookMainSettings myMainSettings;
	private final WebHookConfigFactory myWebHookConfigFactory;
	private final WebHookTemplateResolver myWebHookTemplateResolver;
	private final WebHookPayloadManager myWebHookPayloadManager;
	private final WebHookFactory myWebHookFactory;
	private final WebHookHistoryItemFactory myWebHookHistoryItemFactory;
	private final WebHookHistoryRepository myWebHookHistoryRepository;
	private final WebAddressTransformer myWebAddressTransformer;
	
	public WebHookUserRequestedExecutorImpl(
			SBuildServer server,
			WebHookMainSettings mainSettings,
			WebHookConfigFactory webHookConfigFactory,
			WebHookFactory webHookFactory,
			WebHookTemplateResolver webHookTemplateResolver,
			WebHookPayloadManager webHookPayloadManager,
			WebHookHistoryItemFactory webHookHistoryItemFactory,
			WebHookHistoryRepository webHookHistoryRepository,
			WebAddressTransformer webAddressTransformer
			) {
		myServer = server;
		myMainSettings = mainSettings;
		myWebHookConfigFactory = webHookConfigFactory;
		myWebHookFactory = webHookFactory;
		myWebHookTemplateResolver = webHookTemplateResolver;
		myWebHookPayloadManager = webHookPayloadManager;
		myWebHookHistoryItemFactory = webHookHistoryItemFactory;
		myWebHookHistoryRepository = webHookHistoryRepository;
		myWebAddressTransformer = webAddressTransformer;
	}
	
	@Override
	public WebHookHistoryItem requestWebHookExecution(WebHookExecutionRequest webHookExecutionRequest) {
		WebHookConfig webHookConfig = myWebHookConfigFactory.build(webHookExecutionRequest);
		
		WebHookContentBuilder contentBuilder = new WebHookContentBuilder(myWebHookPayloadManager, myWebHookTemplateResolver);
		WebHook wh = myWebHookFactory.getWebHook(webHookConfig, 
												myMainSettings.getProxyConfigForUrl(
														webHookConfig.getUrl()
														)
												);
		WebHookHistoryItem webHookHistoryItem = executeWebHook(webHookExecutionRequest.getBuildId(), webHookExecutionRequest.getTestBuildState(), webHookConfig, contentBuilder, wh);
		myWebHookHistoryRepository.addHistoryItem(webHookHistoryItem);
		return webHookHistoryItem; 

		
	}

	public WebHookHistoryItem requestWebHookExecution(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest) {
		WebHookConfig webHookConfig = null;

		try {
			if (webHookTemplateExecutionRequest.getUniqueKey() == null  && webHookTemplateExecutionRequest.getUrl() == null) {
				throw new WebHookConfigNotFoundException("Neither existing webhook id or URL found.");
			} else if (webHookTemplateExecutionRequest.getUniqueKey() == null) {
				webHookConfig = myWebHookConfigFactory.buildSimple(webHookTemplateExecutionRequest);
			} else {
				webHookConfig = myWebHookConfigFactory.build(webHookTemplateExecutionRequest);
			}
		} catch (WebHookConfigNotFoundException e) {
			SBuild sbuild = myServer.findBuildInstanceById(webHookTemplateExecutionRequest.getBuildId());
			WebHookExecutionStats stats = new WebHookExecutionStats();
			stats.setEnabled(false);
			stats.setErrored(true);
			stats.setStatusCode(WebHookExecutionException.WEBHOOK_CONFIGURATION_NOT_FOUND_EXCEPTION_ERROR_CODE);
			stats.setStatusReason(e.getMessage());
			
			WebHookHistoryItem webHookHistoryItem = new WebHookHistoryItem(
					webHookTemplateExecutionRequest.getProjectExternalId(), 
					myServer.getProjectManager().findProjectByExternalId(webHookTemplateExecutionRequest.getProjectExternalId()).getName(),
					sbuild.getBuildTypeId(),
					sbuild.getBuildTypeName(),
					sbuild.getBuildTypeExternalId(),
					webHookTemplateExecutionRequest.getBuildId(),
					webHookConfig,
					stats,
					new WebHookErrorStatus(e, e.getMessage(), WebHookExecutionException.WEBHOOK_CONFIGURATION_NOT_FOUND_EXCEPTION_ERROR_CODE),
					new LocalDateTime(),
					getGeneralisedWebAddress(webHookTemplateExecutionRequest),
					true
					);
			myWebHookHistoryRepository.addHistoryItem(webHookHistoryItem);
			return webHookHistoryItem; 
		}
		
		// If we got a URL from the user, override the one on the config.
		if (webHookTemplateExecutionRequest.getUrl() != null && !webHookTemplateExecutionRequest.getUrl().trim().isEmpty()) {
			webHookConfig.setUrl(webHookTemplateExecutionRequest.getUrl());
		}
		
		// Enable the build state we are testing, just in case it was not already enabled.
		if (! webHookConfig.getBuildStates().enabled(webHookTemplateExecutionRequest.getTestBuildState())) {
			webHookConfig.getBuildStates().enable(webHookTemplateExecutionRequest.getTestBuildState());
		}
		
		// Add a header to indicate that it was a test.
		webHookConfig.getHeaders().add(WebHookHeaderConfig.create("x-tcwebhooks-user-initiated-test", "true"));
		
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
		
		WebHookHistoryItem webHookHistoryItem = executeWebHook(webHookTemplateExecutionRequest.getBuildId(), webHookTemplateExecutionRequest.getTestBuildState(), webHookConfig, contentBuilder, wh); 
		myWebHookHistoryRepository.addHistoryItem(webHookHistoryItem);
		return webHookHistoryItem; 
		
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
						testBuildState, true);
			
			Loggers.SERVER.debug("#### CONTENT #### " + wh.getPayload());
			WebHookListener.doPost(wh, webHookConfig.getPayloadFormat());
			return myWebHookHistoryItemFactory.getWebHookHistoryTestItem(
							webHookConfig,
							wh.getExecutionStats(), 
							sRunningBuild,
							null);
		} catch (WebHookExecutionException ex){
			wh.getExecutionStats().setErrored(true);
			wh.getExecutionStats().setRequestCompleted(ex.getErrorCode(), ex.getMessage());
			Loggers.SERVER.error(WEB_HOOK_USER_REQUESTED_EXECUTOR_IMPL + ex.getMessage());
			Loggers.SERVER.debug(ex);
			return myWebHookHistoryItemFactory.getWebHookHistoryTestItem(
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
			return myWebHookHistoryItemFactory.getWebHookHistoryTestItem(
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
