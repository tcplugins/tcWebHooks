package webhook.teamcity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import webhook.WebHook;
import webhook.teamcity.executor.WebHookResponsibilityHolder;
import webhook.teamcity.payload.WebHookContentObjectSerialiser;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolver;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.project.WebHookParameter;
import webhook.teamcity.settings.project.WebHookParameterStore;
import webhook.teamcity.settings.project.WebHookParameterStoreFactory;

public class WebHookContentBuilder {
	private final SBuildServer server;
	private final WebHookTemplateResolver webHookTemplateResolver;
	private final WebHookVariableResolverManager webHookVariableResolverManager;
	private final WebHookParameterStore webHookParameterStore;
	

	public WebHookContentBuilder(SBuildServer server, WebHookTemplateResolver resolver, WebHookVariableResolverManager variableResolverManager, WebHookParameterStore webHookParameterStore) {
		this.server = server;
		this.webHookTemplateResolver = resolver;
		this.webHookVariableResolverManager = variableResolverManager;
		this.webHookParameterStore = webHookParameterStore;
	}
	
	public WebHookContentBuilder(SBuildServer server, WebHookTemplateResolver resolver, WebHookVariableResolverManager variableResolverManager, WebHookParameterStoreFactory webHookParameterStoreFactory) {
		this.server = server;
		this.webHookTemplateResolver = resolver;
		this.webHookVariableResolverManager = variableResolverManager;
		this.webHookParameterStore = webHookParameterStoreFactory.getWebHookParameterStore();
	}
	
	public WebHook buildWebHookContent(WebHook wh, WebHookConfig whc, SQueuedBuild sBuild, BuildStateEnum state,
			String user, String comment, boolean overrideIsEnabled) {
		WebHookPayload payloadFormat = webHookTemplateResolver.getTemplatePayloadFormat(whc.getPayloadTemplate());
		VariableResolverFactory variableResolverFactory = this.webHookVariableResolverManager.getVariableResolverFactory(payloadFormat.getTemplateEngineType());
		WebHookTemplateContent templateForThisBuild;
		wh.setContentType(payloadFormat.getContentType());
		wh.setCharset(payloadFormat.getCharset());
		wh.setVariableResolverFactory(variableResolverFactory);
		
		if (state.equals(BuildStateEnum.BUILD_ADDED_TO_QUEUE) ){
			wh.setEnabledForBuildState(state, overrideIsEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(state)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), null, getPreferredDateFormat(templateForThisBuild));
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, state, extraParameters, whc.getEnabledTemplates(), user, comment);
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.buildAddedToQueue(sBuild, extraParameters, whc.getEnabledTemplates(), templateForThisBuild));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		} else if (state.equals(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE) ){
			wh.setEnabledForBuildState(state, overrideIsEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(state)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), null, getPreferredDateFormat(templateForThisBuild));
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, state, extraParameters, whc.getEnabledTemplates(), user, comment);
				Map<String, VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.buildRemovedFromQueue(sBuild, extraParameters, whc.getEnabledTemplates(), templateForThisBuild, user, comment));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		}
		return wh;
	}

	private Map<String,VariableMessageBuilder> createVariableMessageBuilders(WebHookPayload payloadFormat, WebHookPayloadContent content) {
		Map<String, VariableMessageBuilder> buildersMap = new TreeMap<>();
		for(VariableResolverFactory variableResolverFactory : this.webHookVariableResolverManager.getAllVariableResolverFactories()) {
			buildersMap.put(
					variableResolverFactory.getPayloadTemplateType().toString(), 
					variableResolverFactory.createVariableMessageBuilder(
							variableResolverFactory.buildVariableResolver(
									content.getProject(), 
									payloadFormat, 
									content, 
									content.getAllParameters()
									)));
		}
		return buildersMap;
	}
	
	public WebHook buildWebHookContent(WebHook wh, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, String username, String comment, boolean isOverrideEnabled) {
		WebHookPayload payloadFormat = webHookTemplateResolver.getTemplatePayloadFormat(whc.getPayloadTemplate());
		VariableResolverFactory variableResolverFactory = this.webHookVariableResolverManager.getVariableResolverFactory(payloadFormat.getTemplateEngineType());
		WebHookTemplateContent templateForThisBuild;
		wh.setContentType(payloadFormat.getContentType());
		wh.setCharset(payloadFormat.getCharset());
		wh.setVariableResolverFactory(variableResolverFactory);
		
		
		if (state.equals(BuildStateEnum.BUILD_STARTED)){
			wh.setEnabledForBuildState(BuildStateEnum.BUILD_STARTED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_STARTED)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), sBuild, getPreferredDateFormat(templateForThisBuild));
				extraParameters.forceResolveVariables(sBuild.getValueResolver());
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, getPreviousNonPersonalBuild(wh, sBuild), state, extraParameters, whc.getEnabledTemplates());
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.buildStarted(sBuild, getPreviousNonPersonalBuild(wh, sBuild), extraParameters, whc.getEnabledTemplates(), templateForThisBuild));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		} else if (state.equals(BuildStateEnum.CHANGES_LOADED)){
			wh.setEnabledForBuildState(BuildStateEnum.CHANGES_LOADED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.CHANGES_LOADED)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), sBuild, getPreferredDateFormat(templateForThisBuild));
				extraParameters.forceResolveVariables(sBuild.getValueResolver());
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, getPreviousNonPersonalBuild(wh, sBuild), state, extraParameters, whc.getEnabledTemplates());
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.changesLoaded(sBuild, getPreviousNonPersonalBuild(wh, sBuild), extraParameters, whc.getEnabledTemplates(), templateForThisBuild));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		} else if (state.equals(BuildStateEnum.SERVICE_MESSAGE_RECEIVED)){
			wh.setEnabledForBuildState(BuildStateEnum.SERVICE_MESSAGE_RECEIVED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.SERVICE_MESSAGE_RECEIVED)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), sBuild, getPreferredDateFormat(templateForThisBuild));
				extraParameters.forceResolveVariables(sBuild.getValueResolver());
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, getPreviousNonPersonalBuild(wh, sBuild), state, extraParameters, whc.getEnabledTemplates());
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.serviceMessageReceived(sBuild, getPreviousNonPersonalBuild(wh, sBuild), extraParameters, whc.getEnabledTemplates(), templateForThisBuild));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		} else if (state.equals(BuildStateEnum.BUILD_INTERRUPTED)){
			wh.setEnabledForBuildState(BuildStateEnum.BUILD_INTERRUPTED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_INTERRUPTED)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), sBuild, getPreferredDateFormat(templateForThisBuild));
				extraParameters.forceResolveVariables(sBuild.getValueResolver());
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, getPreviousNonPersonalBuild(wh, sBuild), state, extraParameters, whc.getEnabledTemplates());
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.buildInterrupted(sBuild, getPreviousNonPersonalBuild(wh, sBuild), extraParameters, whc.getEnabledTemplates(), templateForThisBuild));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		} else if (state.equals(BuildStateEnum.BEFORE_BUILD_FINISHED)){
			wh.setEnabledForBuildState(BuildStateEnum.BEFORE_BUILD_FINISHED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BEFORE_BUILD_FINISHED)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), sBuild, getPreferredDateFormat(templateForThisBuild));
				extraParameters.forceResolveVariables(sBuild.getValueResolver());
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, getPreviousNonPersonalBuild(wh, sBuild), state, extraParameters, whc.getEnabledTemplates());
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.beforeBuildFinish(sBuild, getPreviousNonPersonalBuild(wh, sBuild), extraParameters, whc.getEnabledTemplates(), templateForThisBuild));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		} else if (state.equals(BuildStateEnum.BUILD_FINISHED) || state.equals(BuildStateEnum.BUILD_SUCCESSFUL) || state.equals(BuildStateEnum.BUILD_FAILED) || state.equals(BuildStateEnum.BUILD_FIXED) || state.equals(BuildStateEnum.BUILD_BROKEN)){
			wh.setEnabledForBuildState(BuildStateEnum.BUILD_FINISHED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(
					BuildStateEnum.BUILD_FINISHED, 
					sBuild.getStatusDescriptor().isSuccessful(),
					hasBuildChangedHistoricalState(sBuild, getPreviousNonPersonalBuild(wh, sBuild)))));
			
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, BuildState.getEffectiveState(state, sBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sBuild,getPreviousNonPersonalBuild(wh, sBuild))), whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), sBuild, getPreferredDateFormat(templateForThisBuild));
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, getPreviousNonPersonalBuild(wh, sBuild), BuildState.getEffectiveState(state, sBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sBuild, getPreviousNonPersonalBuild(wh, sBuild))), extraParameters, whc.getEnabledTemplates());
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				extraParameters.forceResolveVariables(sBuild.getValueResolver());
				wh.setPayload(payloadFormat.buildFinished(sBuild, getPreviousNonPersonalBuild(wh, sBuild), extraParameters, whc.getEnabledTemplates(), templateForThisBuild));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		} else if (state.equals(BuildStateEnum.BUILD_PINNED)) {
			wh.setEnabledForBuildState(state, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(state)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), sBuild, getPreferredDateFormat(templateForThisBuild));
				extraParameters.forceResolveVariables(sBuild.getValueResolver());
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, state, extraParameters, whc.getEnabledTemplates(), username, comment);
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.buildPinned(sBuild, extraParameters, whc.getEnabledTemplates(), templateForThisBuild, username, comment));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		} else if (state.equals(BuildStateEnum.BUILD_UNPINNED)) {
			wh.setEnabledForBuildState(state, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(state)));
			if (Boolean.TRUE.equals(wh.isEnabled())){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				ExtraParameters extraParameters = mergeParameters(whc.getParams(), sBuild.getBuildType().getProject(), sBuild, getPreferredDateFormat(templateForThisBuild));
				extraParameters.forceResolveVariables(sBuild.getValueResolver());
				WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, state, extraParameters, whc.getEnabledTemplates(), username, comment);
				Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
				VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
				extraParameters.resolveParameters(builders);
				wh.setPayload(payloadFormat.buildUnpinned(sBuild, extraParameters, whc.getEnabledTemplates(), templateForThisBuild, username, comment));
				builder.addWebHookPayload(wh.getPayload());
				wh.resolveAuthenticationParameters(builder);
				wh.setUrl(builder.build(whc.getUrl()));
				wh.checkFilters(builder);
				wh.resolveHeaders(builder);
				wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
			}
		}
		return wh;
	}
	
	public WebHook buildWebHookContent(WebHook wh, WebHookConfig whc,
			WebHookResponsibilityHolder responsibilityHolder, BuildStateEnum state, boolean isOverrideEnabled) {
		WebHookPayload payloadFormat = webHookTemplateResolver.getTemplatePayloadFormat(whc.getPayloadTemplate());
		VariableResolverFactory variableResolverFactory = this.webHookVariableResolverManager.getVariableResolverFactory(payloadFormat.getTemplateEngineType());
		WebHookTemplateContent templateForThisBuild;
		wh.setContentType(payloadFormat.getContentType());
		wh.setCharset(payloadFormat.getCharset());
		wh.setVariableResolverFactory(variableResolverFactory);
		
		wh.setEnabledForBuildState(BuildStateEnum.RESPONSIBILITY_CHANGED, isOverrideEnabled || wh.getBuildStates().enabled(BuildStateEnum.RESPONSIBILITY_CHANGED));
		if (Boolean.TRUE.equals(wh.isEnabled())){
			templateForThisBuild = findTemplateForState(responsibilityHolder.getSProject(), state, whc.getPayloadTemplate());
			ExtraParameters extraParameters = mergeParameters(whc.getParams(), responsibilityHolder.getSProject(), null, getPreferredDateFormat(templateForThisBuild));
			WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, responsibilityHolder, state, extraParameters, whc.getEnabledTemplates());
			Map<String,VariableMessageBuilder> builders = createVariableMessageBuilders(payloadFormat, content);
			VariableMessageBuilder builder = builders.get(payloadFormat.getTemplateEngineType().toString());
			extraParameters.resolveParameters(builders);
			wh.setPayload(payloadFormat.responsibilityChanged(responsibilityHolder, extraParameters, whc.getEnabledTemplates(), templateForThisBuild));
			builder.addWebHookPayload(wh.getPayload());
			wh.resolveAuthenticationParameters(builder);
			wh.setUrl(builder.build(whc.getUrl()));
			wh.checkFilters(builder);
			wh.resolveHeaders(builder);
			wh.getExecutionStats().setSecureValueAccessed(extraParameters.wasSecureValueAccessed());
		}
		return wh;
	}
	
	public static String getPreferredDateFormat(WebHookTemplateContent templateContent){
		if (templateContent != null){
			return templateContent.getPreferredDateTimeFormat();
		}
		return "";
	}

	/** SBuild version */
	public String resolveTemplatedUrl(VariableResolverFactory variableResolverFactory, WebHook wh, String url, BuildStateEnum buildState, SBuild sBuild, WebHookContentObjectSerialiser serialiser, ExtraParameters extraParameters, Map<String,String> templates){
		if (url.contains(variableResolverFactory.getPayloadTemplateType().getVariablePrefix()) && url.contains(variableResolverFactory.getPayloadTemplateType().getVariableSuffix())){
			WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, getPreviousNonPersonalBuild(wh, sBuild), buildState, extraParameters, templates);
			VariableResolver variableResolver = variableResolverFactory.buildVariableResolver(content.getProject(), serialiser,content, content.getAllParameters());
			VariableMessageBuilder builder = variableResolverFactory.createVariableMessageBuilder(variableResolver);
			return builder.build(url);
		} else {
			return url;
		}
	}
	
	/** SBuild version with user and comment*/
	public String resolveTemplatedUrl(VariableResolverFactory variableResolverFactory, String url, BuildStateEnum buildState, SBuild sBuild, WebHookContentObjectSerialiser serialiser, ExtraParameters extraParameters, Map<String,String> templates, String user, String comment){
		if (url.contains(variableResolverFactory.getPayloadTemplateType().getVariablePrefix()) && url.contains(variableResolverFactory.getPayloadTemplateType().getVariableSuffix())){
			WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, buildState, extraParameters, templates, user, comment);
			VariableResolver variableResolver = variableResolverFactory.buildVariableResolver(content.getProject(), serialiser,content, content.getAllParameters());
			VariableMessageBuilder builder = variableResolverFactory.createVariableMessageBuilder(variableResolver);
			return builder.build(url);
		} else {
			return url;
		}
	}
	
	/** SQueuedBuild version */
	public String resolveTemplatedUrl(VariableResolverFactory variableResolverFactory, String url, VariableMessageBuilder builder){
		if (url.contains(variableResolverFactory.getPayloadTemplateType().getVariablePrefix()) && url.contains(variableResolverFactory.getPayloadTemplateType().getVariableSuffix())){
			return builder.build(url);
		} else {
			return url;
		}
	}
	/** ResponsibleChanged version */
	public String resolveTemplatedUrl(VariableResolverFactory variableResolverFactory, String url, BuildStateEnum buildState, WebHookResponsibilityHolder responsibilityHolder, WebHookContentObjectSerialiser serialiser, ExtraParameters extraParameters, Map<String,String> templates){
		if (url.contains(variableResolverFactory.getPayloadTemplateType().getVariablePrefix()) && url.contains(variableResolverFactory.getPayloadTemplateType().getVariableSuffix())){
			WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, responsibilityHolder, buildState, extraParameters, templates);
			VariableResolver variableResolver = variableResolverFactory.buildVariableResolver(content.getProject(), serialiser,content, content.getAllParameters());
			VariableMessageBuilder builder = variableResolverFactory.createVariableMessageBuilder(variableResolver);
			return builder.build(url);
		} else {
			return url;
		}
	}
	
	public VariableResolver getVariableResolver(VariableResolverFactory variableResolverFactory, WebHook wh, BuildStateEnum buildState, SBuild runningBuild, WebHookContentObjectSerialiser serialiser, ExtraParameters extraParameters, Map<String,String> templates){
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, runningBuild, getPreviousNonPersonalBuild(wh, runningBuild), buildState, extraParameters, templates);
		return variableResolverFactory.buildVariableResolver(content.getProject(), serialiser, content, content.getAllParameters());
	}
	
	public VariableResolver getVariableResolver(VariableResolverFactory variableResolverFactory, BuildStateEnum buildState, SBuild runningBuild, WebHookContentObjectSerialiser serialiser, ExtraParameters extraParameters, Map<String,String> templates, String user, String comment){
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, runningBuild, buildState, extraParameters, templates, user, comment);
		return variableResolverFactory.buildVariableResolver(content.getProject(), serialiser, content, content.getAllParameters());
	}
	
	public VariableResolver getVariableResolver(VariableResolverFactory variableResolverFactory, BuildStateEnum buildState, SQueuedBuild queuedBuild, WebHookContentObjectSerialiser serialiser, ExtraParameters extraParameters, Map<String,String> templates, String user, String comment){
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory,  server,  queuedBuild,  buildState, extraParameters, templates, user, comment);
		return variableResolverFactory.buildVariableResolver(content.getProject(), serialiser, content, content.getAllParameters());
	}
	
	public VariableResolver getVariableResolver(VariableResolverFactory variableResolverFactory, BuildStateEnum buildState, WebHookResponsibilityHolder responsibilityHolder, WebHookContentObjectSerialiser serialiser, ExtraParameters extraParameters, Map<String,String> templates){
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory,  server, responsibilityHolder,  buildState, extraParameters,  templates);
		return variableResolverFactory.buildVariableResolver(content.getProject(), serialiser, content, content.getAllParameters());
	}
	
	public WebHookTemplateContent findTemplateForState(
			SBuild sRunningBuild, BuildStateEnum state,
			String payloadtemplateName) {
		WebHookTemplateContent templateForThisBuild;
		if (sRunningBuild.getBranch() != null){ 
			// We have a branch aware sBuild. Get the branch template.
			templateForThisBuild = webHookTemplateResolver.findWebHookBranchTemplate(state, sRunningBuild.getBuildType(), payloadtemplateName);
		} else {
			// Branch is null. TeamCity is not aware of branch support for this sBuild, so get the non-branch template.
			templateForThisBuild = webHookTemplateResolver.findWebHookTemplate(state, sRunningBuild.getBuildType(), payloadtemplateName);
		}
		return templateForThisBuild;
	}
	
	public WebHookTemplateContent findTemplateForState(
			SQueuedBuild sQueuedBuild, BuildStateEnum state,
			String payloadtemplateName) {

		// Branch is null for queued builds, so get the non-branch template.
		return webHookTemplateResolver.findWebHookTemplate(state, sQueuedBuild.getBuildType(), payloadtemplateName);
	}
	
	public WebHookTemplateContent findTemplateForState(
			SProject sProject, BuildStateEnum state,
			String payloadtemplateName) {
		
		// Branch is null for responsible changed, so get the non-branch template.
		return webHookTemplateResolver.findWebHookTemplate(state, sProject, payloadtemplateName);
	}
	
	public ExtraParameters mergeParameters(ExtraParameters parametersFromConfig, SProject project, SBuild build, String preferredDateFormat){
		ExtraParameters extraParameters = new ExtraParameters();
		
		// First add the preferredDateFormat from the template. This can then be overriden 
		// by the webhook config (plugin-settings.xml) 
		// which can in turn be overriden by a user defined build property (webhook.*)
		
		extraParameters.put(ExtraParameters.WEBHOOK, "preferredDateFormat", preferredDateFormat);
		
		// Now add any parameters passed in in the webhook configuration in plugin-settings.xml
		extraParameters.putAll(ExtraParameters.WEBHOOK, parametersFromConfig.getAll());

		if (project != null ) {
			List<WebHookParameter> projectParameters = this.webHookParameterStore.getAllWebHookParameters(project);
			Collections.reverse(projectParameters);
			extraParameters.putAll(
					ExtraParameters.PROJECT, 
					projectParameters);
		}
		
		// Then override any from ones declared in the actual build via webhook.*
		if (build != null) {
			Map<String,String> teamCityProperties = build.getParametersProvider().getAll(); 
			extraParameters.putAll(ExtraParameters.TEAMCITY, teamCityProperties);
			for (Entry<String,String> tcProperty : teamCityProperties.entrySet()){
				if (tcProperty.getKey().startsWith("webhook.")){
					extraParameters.put(ExtraParameters.WEBHOOK, tcProperty.getKey().substring("webhook.".length()), tcProperty.getValue());
				}
			}
		}
		return extraParameters;
	}
	
	@Nullable
	protected SFinishedBuild getPreviousNonPersonalBuild(WebHook wh, SBuild paramSBuild)
	  {
		if (wh.getPreviousNonPersonalBuild() != null) {
			return wh.getPreviousNonPersonalBuild();
		}
		wh.setPreviousNonPersonalBuild(getRecursivePreviousNonPersonalBuild(paramSBuild));
		return wh.getPreviousNonPersonalBuild();
	}
	
	@Nullable 
	private SFinishedBuild getRecursivePreviousNonPersonalBuild(SBuild paramSBuild) {
		SFinishedBuild localSFinishedBuild = paramSBuild.getPreviousFinished();
		if (localSFinishedBuild == null) { // There was not a previous build.
			return localSFinishedBuild;
		} else if (localSFinishedBuild.isPersonal()){
			localSFinishedBuild = getRecursivePreviousNonPersonalBuild(localSFinishedBuild);
		}
		return localSFinishedBuild;
	}
	
	private boolean hasBuildChangedHistoricalState(SBuild sRunningBuild, SFinishedBuild previous){
		if (previous != null){
			if (sRunningBuild.getBuildStatus().isSuccessful()){
				return previous.getBuildStatus().isFailed();
			} else if (sRunningBuild.getBuildStatus().isFailed()) {
				return previous.getBuildStatus().isSuccessful();
			}
		}
		return true; 
	}

}
