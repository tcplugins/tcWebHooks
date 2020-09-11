package webhook.teamcity.testing;

import java.net.MalformedURLException;
import java.net.URL;

import org.joda.time.LocalDateTime;

import jetbrains.buildServer.responsibility.ResponsibilityEntry.State;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import webhook.WebHook;
import webhook.WebHookExecutionStats;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.WebHookExecutionException;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.executor.WebHookRunner;
import webhook.teamcity.executor.WebHookRunnerFactory;
import webhook.teamcity.history.GeneralisedWebAddress;
import webhook.teamcity.history.GeneralisedWebAddressType;
import webhook.teamcity.history.WebAddressTransformer;
import webhook.teamcity.history.WebHookHistoryItem;
import webhook.teamcity.history.WebHookHistoryItem.WebHookErrorStatus;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.template.render.WebHookStringRenderer;
import webhook.teamcity.payload.template.render.WebHookStringRenderer.WebHookHtmlRendererException;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookRenderResult;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public class WebHookUserRequestedExecutorImpl implements WebHookUserRequestedExecutor {

	private static final String A_TESTING_USER = "a testing user";
	private static final String A_TEST_EXECUTION_COMMENT = "A test execution comment";
	private static final String A_PREVIOUS_TESTING_USER = "a previous testing user";
	private final SBuildServer myServer;
	private final WebHookMainSettings myMainSettings;
	private final WebHookConfigFactory myWebHookConfigFactory;
	private final WebHookTemplateResolver myWebHookTemplateResolver;
	private final WebHookFactory myWebHookFactory;
	private final WebHookHistoryItemFactory myWebHookHistoryItemFactory;
	private final WebHookHistoryRepository myWebHookHistoryRepository;
	private final WebAddressTransformer myWebAddressTransformer;
	private final WebHookContentBuilder myWebHookContentBuilder;
	private final WebHookVariableResolverManager myWebHookVariableResolverManager;
	private final WebHookPayloadManager myWebHookPayloadManager;
	private final ProjectIdResolver myProjectIdResolver;
	
	private final WebHookParameterStore myWebHookParameterStore;

	public WebHookUserRequestedExecutorImpl(
			SBuildServer server,
			WebHookMainSettings mainSettings,
			WebHookConfigFactory webHookConfigFactory,
			WebHookFactory webHookFactory,
			WebHookTemplateResolver webHookTemplateResolver,
			WebHookPayloadManager webHookPayloadManager,
			WebHookHistoryItemFactory webHookHistoryItemFactory,
			WebHookHistoryRepository webHookHistoryRepository,
			WebAddressTransformer webAddressTransformer,
			WebHookContentBuilder webHookContentBuilder,
			WebHookVariableResolverManager webHookVariableResolverManager,
			ProjectIdResolver projectIdResolver,
			WebHookParameterStoreFactory webHookParameterStoreFactory
		) {
		myServer = server;
		myMainSettings = mainSettings;
		myWebHookConfigFactory = webHookConfigFactory;
		myWebHookFactory = webHookFactory;
		myWebHookTemplateResolver = webHookTemplateResolver;
		myWebHookHistoryItemFactory = webHookHistoryItemFactory;
		myWebHookHistoryRepository = webHookHistoryRepository;
		myWebAddressTransformer = webAddressTransformer;
		myWebHookContentBuilder = webHookContentBuilder;
		myWebHookVariableResolverManager = webHookVariableResolverManager;
		myWebHookPayloadManager = webHookPayloadManager;
		myProjectIdResolver = projectIdResolver;
		myWebHookParameterStore = webHookParameterStoreFactory.getWebHookParameterStore();
	}


	@Override
	public WebHookRenderResult requestWebHookPreview(WebHookExecutionRequest webHookExecutionRequest) {

		WebHookConfig webHookConfig = myWebHookConfigFactory.build(webHookExecutionRequest);
		WebHook wh = myWebHookFactory.getWebHook(webHookConfig,
				myMainSettings.getProxyConfigForUrl(
						webHookConfig.getUrl()
						)
				);

		if (   webHookExecutionRequest.getTestBuildState().equals(BuildStateEnum.BUILD_ADDED_TO_QUEUE)
			|| webHookExecutionRequest.getTestBuildState().equals(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE)) {

			wh = myWebHookContentBuilder.buildWebHookContent(
					wh,
					webHookConfig,
					new TestingSQueuedBuild(myServer.findBuildInstanceById(webHookExecutionRequest.getBuildId())),
					webHookExecutionRequest.getTestBuildState(),
					A_TESTING_USER,
					A_TEST_EXECUTION_COMMENT,
					true
				);

		} else if (webHookExecutionRequest.getTestBuildState().equals(BuildStateEnum.BUILD_PINNED)
			|| webHookExecutionRequest.getTestBuildState().equals(BuildStateEnum.BUILD_UNPINNED)) {

			wh = myWebHookContentBuilder.buildWebHookContent(
					wh,
					webHookConfig,
					myServer.findBuildInstanceById(webHookExecutionRequest.getBuildId()),
					webHookExecutionRequest.getTestBuildState(),
					A_TESTING_USER,
					A_TEST_EXECUTION_COMMENT,
					true
					);
		} else if (webHookExecutionRequest.getTestBuildState().equals(BuildStateEnum.RESPONSIBILITY_CHANGED)) {
			SBuildType sBuildType = myServer.findBuildInstanceById(webHookExecutionRequest.getBuildId()).getBuildType();
			WebHookResponsibilityHolder responsibilityHolder = WebHookResponsibilityHolder
																	.builder()
																	.sBuildType(sBuildType)
																	.sProject(sBuildType.getProject())
																	.responsibilityEntryOld(new TestingResponsibilityEntry(A_PREVIOUS_TESTING_USER, State.NONE))
																	.responsibilityEntryNew(new TestingResponsibilityEntry(A_TESTING_USER, State.TAKEN))
																	.build();
			wh = myWebHookContentBuilder.buildWebHookContent(
					wh, 
					webHookConfig, 
					responsibilityHolder, 
					webHookExecutionRequest.getTestBuildState(),
					true
					);
		} else {

			wh = myWebHookContentBuilder.buildWebHookContent(
					wh,
					webHookConfig,
					myServer.findBuildInstanceById(webHookExecutionRequest.getBuildId()),
					webHookExecutionRequest.getTestBuildState(),
					null,
					null,
					true
				);
		}
		WebHookPayload webHookPayload = myWebHookTemplateResolver.getTemplatePayloadFormat(webHookExecutionRequest.getTemplateId());
		WebHookStringRenderer renderer = webHookPayload.getWebHookStringRenderer();

		try {
			return new WebHookRenderResult(renderer.render(wh.getPayload()), webHookPayload.getFormatShortName());
		} catch (WebHookHtmlRendererException ex){
			Loggers.SERVER.info(ex);
			return new WebHookRenderResult(wh.getPayload(), ex);
		}
	}

	@Override
	public WebHookRenderResult requestWebHookPreview(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest) {

		WebHookConfig webHookConfig = myWebHookConfigFactory.buildSimple(webHookTemplateExecutionRequest);

		WebHook wh = myWebHookFactory.getWebHook(webHookConfig,
				myMainSettings.getProxyConfigForUrl(
						webHookConfig.getUrl()
						)
				);

		WebHookContentBuilder contentBuilder = createDummyContentBuilder(webHookTemplateExecutionRequest, myWebHookPayloadManager);

		if (   webHookTemplateExecutionRequest.getTestBuildState().equals(BuildStateEnum.BUILD_ADDED_TO_QUEUE)
			|| webHookTemplateExecutionRequest.getTestBuildState().equals(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE)) {

			wh = contentBuilder.buildWebHookContent(
					wh,
					webHookConfig,
					new TestingSQueuedBuild(myServer.findBuildInstanceById(webHookTemplateExecutionRequest.getBuildId())),
					webHookTemplateExecutionRequest.getTestBuildState(),
					A_TESTING_USER,
					A_TEST_EXECUTION_COMMENT,
					true
				);
		} else if (webHookTemplateExecutionRequest.getTestBuildState().equals(BuildStateEnum.BUILD_PINNED)
				|| webHookTemplateExecutionRequest.getTestBuildState().equals(BuildStateEnum.BUILD_UNPINNED)) {

				wh = myWebHookContentBuilder.buildWebHookContent(
						wh,
						webHookConfig,
						myServer.findBuildInstanceById(webHookTemplateExecutionRequest.getBuildId()),
						webHookTemplateExecutionRequest.getTestBuildState(),
						A_TESTING_USER,
						A_TEST_EXECUTION_COMMENT,
						true
						);
		} else if (webHookTemplateExecutionRequest.getTestBuildState().equals(BuildStateEnum.RESPONSIBILITY_CHANGED)) {
			SBuildType sBuildType = myServer.findBuildInstanceById(webHookTemplateExecutionRequest.getBuildId()).getBuildType();
			WebHookResponsibilityHolder responsibilityHolder = WebHookResponsibilityHolder
																	.builder()
																	.sBuildType(sBuildType)
																	.sProject(sBuildType.getProject())
																	.responsibilityEntryOld(new TestingResponsibilityEntry(A_PREVIOUS_TESTING_USER, State.NONE))
																	.responsibilityEntryNew(new TestingResponsibilityEntry(A_TESTING_USER, State.TAKEN))
																	.build();
			wh = myWebHookContentBuilder.buildWebHookContent(
					wh, 
					webHookConfig, 
					responsibilityHolder, 
					webHookTemplateExecutionRequest.getTestBuildState(),
					true
					);
		} else {

			wh = contentBuilder.buildWebHookContent(
					wh,
					webHookConfig,
					myServer.findBuildInstanceById(webHookTemplateExecutionRequest.getBuildId()),
					webHookTemplateExecutionRequest.getTestBuildState(),
					null,
					null,
					true
				);
		}

		WebHookStringRenderer renderer = myWebHookPayloadManager.getFormat(webHookTemplateExecutionRequest.getFormat()).getWebHookStringRenderer();

		try {
			return new WebHookRenderResult(renderer.render(wh.getPayload()), webHookTemplateExecutionRequest.getFormat());
		} catch (WebHookHtmlRendererException ex){
			Loggers.SERVER.info(ex);
			return new WebHookRenderResult(wh.getPayload(), ex);
		}

	}

	/**
	 * Creates a dummy content builder which resolves templates from the data
	 * passed into the test request.
	 * It creates a local {@link WebHookTemplateResolver}, and registers a new template based
	 * on the text in the {@link WebHookTemplateExecutionRequest}
	 *
	 * @param webHookTemplateExecutionRequest
	 * @return
	 */
	private WebHookContentBuilder createDummyContentBuilder(
			WebHookTemplateExecutionRequest webHookTemplateExecutionRequest, WebHookPayloadManager payloadManager) {

		// We need an alternative WebHookTemplateManager. We'll use the injected payload manager, but create our
		// own jaxHelper. The jaxHelper is only used to persist the template, which we won't do in this class.
		WebHookTemplateManager webHookTemplateManager = new WebHookTemplateManager(myWebHookPayloadManager, new NoOpJaxHelper(), myProjectIdResolver);

		WebHookTemplateConfig webHookTemplateConfig = webHookTemplateExecutionRequest.toConfig();
		WebHookTemplateResolver webHookTemplateResolver = new NonDiscrimatoryTemplateResolver(webHookTemplateManager, webHookTemplateConfig, payloadManager);
		webHookTemplateManager.registerTemplateFormatFromXmlConfig(webHookTemplateConfig);

		return new WebHookContentBuilder(myServer, webHookTemplateResolver, myWebHookVariableResolverManager, myWebHookParameterStore);
	}

	@Override
	public WebHookHistoryItem requestWebHookExecution(WebHookExecutionRequest webHookExecutionRequest) {
		WebHookConfig webHookConfig = myWebHookConfigFactory.build(webHookExecutionRequest);

		WebHookContentBuilder contentBuilder = new WebHookContentBuilder(myServer, myWebHookTemplateResolver, myWebHookVariableResolverManager, myWebHookParameterStore);
		WebHook wh = myWebHookFactory.getWebHook(webHookConfig,
												myMainSettings.getProxyConfigForUrl(
														webHookConfig.getUrl()
														)
												);
		return executeWebHook(webHookExecutionRequest.getBuildId(), webHookExecutionRequest.getTestBuildState(), webHookConfig, contentBuilder, wh);
	}

	public WebHookHistoryItem requestWebHookExecution(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest) {
		WebHookConfig webHookConfig = null;

		try {
			if ( (webHookTemplateExecutionRequest.getUniqueKey() == null || webHookTemplateExecutionRequest.getUniqueKey().trim().isEmpty()) && (webHookTemplateExecutionRequest.getUrl() == null || webHookTemplateExecutionRequest.getUrl().trim().isEmpty()) ) {
				throw new WebHookConfigNotFoundException("Neither existing webhook id or URL found.");
			} else if (webHookTemplateExecutionRequest.getUniqueKey() == null || webHookTemplateExecutionRequest.getUniqueKey().isEmpty()) {
				webHookConfig = myWebHookConfigFactory.buildSimple(webHookTemplateExecutionRequest);
			} else {
				webHookConfig = myWebHookConfigFactory.build(webHookTemplateExecutionRequest);
				webHookConfig.setPayloadFormat(webHookTemplateExecutionRequest.getFormat());
			}
		} catch (WebHookConfigNotFoundException e) {
			SBuild sbuild = myServer.findBuildInstanceById(webHookTemplateExecutionRequest.getBuildId());
			WebHookExecutionStats stats = new WebHookExecutionStats();
			stats.setEnabled(true);
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

		WebHookContentBuilder contentBuilder = createDummyContentBuilder(webHookTemplateExecutionRequest, myWebHookPayloadManager);

		WebHookHistoryItem webHookHistoryItem = executeWebHook(webHookTemplateExecutionRequest.getBuildId(), webHookTemplateExecutionRequest.getTestBuildState(), webHookConfig, contentBuilder, wh);
		webHookHistoryItem.getWebHookExecutionStats().setEnabled(true);

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
			WebHookConfig webHookConfig, WebHookContentBuilder contentBuilder, WebHook wh)
	{

		WebHookRunnerFactory myWebHookRunnerFactory = new WebHookRunnerFactory(
				contentBuilder,
				myWebHookHistoryRepository,
				myWebHookHistoryItemFactory);

		SBuild sRunningBuild = myServer.findBuildInstanceById(buildId);
		WebHookRunner webHookRunner;

		if (   testBuildState.equals(BuildStateEnum.BUILD_ADDED_TO_QUEUE)
			|| testBuildState.equals(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE))
		{
			webHookRunner = myWebHookRunnerFactory.getRunner(
												wh,
												webHookConfig,
												new TestingSQueuedBuild(sRunningBuild),
												testBuildState,
												A_TESTING_USER,
												A_TEST_EXECUTION_COMMENT,
												true
											);

	} else if (testBuildState.equals(BuildStateEnum.BUILD_PINNED)
		|| testBuildState.equals(BuildStateEnum.BUILD_UNPINNED)) {

			webHookRunner = myWebHookRunnerFactory.getRunner(
												wh,
												webHookConfig,
												sRunningBuild,
												testBuildState,
												A_TESTING_USER,
												A_TEST_EXECUTION_COMMENT,
												true
											);
	} else if (testBuildState.equals(BuildStateEnum.RESPONSIBILITY_CHANGED)) {
		WebHookResponsibilityHolder responsibilityHolder = WebHookResponsibilityHolder
																.builder()
																.sBuildType(sRunningBuild.getBuildType())
																.sProject(sRunningBuild.getBuildType().getProject())
																.responsibilityEntryOld(new TestingResponsibilityEntry(A_PREVIOUS_TESTING_USER, State.NONE))
																.responsibilityEntryNew(new TestingResponsibilityEntry(A_TESTING_USER, State.TAKEN))
																.build();
		webHookRunner = myWebHookRunnerFactory.getRunner(
				wh, 
				webHookConfig, 
				testBuildState,
				responsibilityHolder, 
				true
				);		
	} else {
			webHookRunner = myWebHookRunnerFactory.getRunner(
												wh,
												webHookConfig,
												sRunningBuild,
												testBuildState,
												null,
												null,
												true
											);
		}
		webHookRunner.run();
		return webHookRunner.getWebHookHistoryItem();
	}

	private static class NonDiscrimatoryTemplateResolver extends WebHookTemplateResolver {

		private WebHookTemplateConfig myWebHookTemplateConfig;

		public NonDiscrimatoryTemplateResolver(WebHookTemplateManager webHookTemplateManager, WebHookTemplateConfig webHookTemplateConfig, WebHookPayloadManager payloadManager) {
			super(webHookTemplateManager, payloadManager);
			myWebHookTemplateConfig = webHookTemplateConfig;
		}

		@Override
		public WebHookTemplateContent findWebHookBranchTemplate(BuildStateEnum state, SBuildType buildType,	String templateName) {
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
		public WebHookTemplateContent findWebHookTemplate(BuildStateEnum state, SBuildType buildType, String templateName) {
			return WebHookTemplateContent.create(
					state.getShortName(),
					myWebHookTemplateConfig.getDefaultTemplate().getTemplateContent(),
					true,
					myWebHookTemplateConfig.getPreferredDateTimeFormat()
				);
			}

		@Override
		public WebHookPayload getTemplatePayloadFormat(String templateId) {
			return this.payloadManager.getFormat(myWebHookTemplateConfig.getFormat());
		}

	}

}
