package webhook.teamcity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.ParametersSupport;
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
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.variableresolver.VariableMessageBuilder;
import webhook.teamcity.payload.variableresolver.VariableResolver;
import webhook.teamcity.payload.variableresolver.VariableResolverFactory;
import webhook.teamcity.payload.variableresolver.WebHookVariableResolverManager;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookContentBuilder {
	private final WebHookTemplateResolver webHookTemplateResolver;
	private final WebHookVariableResolverManager webHookVariableResolverManager;
	private final SBuildServer server;
	

	public WebHookContentBuilder(SBuildServer server, WebHookTemplateResolver resolver, WebHookVariableResolverManager variableResolverManager) {
		this.server = server;
		this.webHookTemplateResolver = resolver;
		this.webHookVariableResolverManager = variableResolverManager; 
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
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.buildAddedToQueue(sBuild, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, whc.getUrl(), state, sBuild, payloadFormat, mergeParameters(whc.getParams(),null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), user, comment));
				wh.checkFilters(getVariableResolver(variableResolverFactory, state, sBuild, payloadFormat, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), user, comment));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, state, sBuild, payloadFormat, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), user, comment));
			}
		} else if (state.equals(BuildStateEnum.BUILD_REMOVED_FROM_QUEUE) ){
			wh.setEnabledForBuildState(state, overrideIsEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(state)));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.buildRemovedFromQueue(sBuild, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild, user, comment));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, whc.getUrl(), state, sBuild, payloadFormat, mergeParameters(whc.getParams(),null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), user, comment));
				wh.checkFilters(getVariableResolver(variableResolverFactory, state, sBuild, payloadFormat, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), user, comment));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, state, sBuild, payloadFormat, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), user, comment));
			}
		}
		return wh;
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
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.buildStarted(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, wh, whc.getUrl(), state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.CHANGES_LOADED)){
			wh.setEnabledForBuildState(BuildStateEnum.CHANGES_LOADED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.CHANGES_LOADED)));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.changesLoaded(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, wh, whc.getUrl(), state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BUILD_INTERRUPTED)){
			wh.setEnabledForBuildState(BuildStateEnum.BUILD_INTERRUPTED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_INTERRUPTED)));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.buildInterrupted(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, wh, whc.getUrl(), state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BEFORE_BUILD_FINISHED)){
			wh.setEnabledForBuildState(BuildStateEnum.BEFORE_BUILD_FINISHED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BEFORE_BUILD_FINISHED)));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.beforeBuildFinish(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, wh, whc.getUrl(), state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BUILD_FINISHED) || state.equals(BuildStateEnum.BUILD_SUCCESSFUL) || state.equals(BuildStateEnum.BUILD_FAILED) || state.equals(BuildStateEnum.BUILD_FIXED) || state.equals(BuildStateEnum.BUILD_BROKEN)){
			wh.setEnabledForBuildState(BuildStateEnum.BUILD_FINISHED, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(
					BuildStateEnum.BUILD_FINISHED, 
					sBuild.getStatusDescriptor().isSuccessful(),
					hasBuildChangedHistoricalState(sBuild, getPreviousNonPersonalBuild(wh, sBuild)))));
			
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, BuildState.getEffectiveState(state, sBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sBuild,getPreviousNonPersonalBuild(wh, sBuild))), whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.buildFinished(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, wh, whc.getUrl(), BuildState.getEffectiveState(state, sBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sBuild, getPreviousNonPersonalBuild(wh, sBuild))), sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, wh, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BUILD_PINNED)) {
			wh.setEnabledForBuildState(state, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(state)));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.buildPinned(sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild, username, comment));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, whc.getUrl(), state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), username, comment));
				wh.checkFilters(getVariableResolver(variableResolverFactory, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), username, comment));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), username, comment));
			}
		} else if (state.equals(BuildStateEnum.BUILD_UNPINNED)) {
			wh.setEnabledForBuildState(state, isOverrideEnabled || (whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(state)));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate());
				wh.setPayload(payloadFormat.buildUnpinned(sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild, username, comment));
				wh.setUrl(resolveTemplatedUrl(variableResolverFactory, whc.getUrl(), state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), username, comment));
				wh.checkFilters(getVariableResolver(variableResolverFactory, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), username, comment));
				wh.resolveHeaders(getVariableResolver(variableResolverFactory, state, sBuild, payloadFormat, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), username, comment));
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
		if (wh.isEnabled()){
			templateForThisBuild = findTemplateForState(responsibilityHolder.getSProject(), state, whc.getPayloadTemplate());
			wh.setPayload(payloadFormat.responsibilityChanged(responsibilityHolder, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
			wh.setUrl(resolveTemplatedUrl(variableResolverFactory, whc.getUrl(), state, responsibilityHolder, payloadFormat, mergeParameters(whc.getParams(),null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			wh.checkFilters(getVariableResolver(variableResolverFactory, state, responsibilityHolder, payloadFormat, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			wh.resolveHeaders(getVariableResolver(variableResolverFactory, state, responsibilityHolder, payloadFormat, mergeParameters(whc.getParams(), null, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
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
	public String resolveTemplatedUrl(VariableResolverFactory variableResolverFactory, WebHook wh, String url, BuildStateEnum buildState, SBuild sBuild, WebHookContentObjectSerialiser serialiser, SortedMap<String,String> extraParameters, Map<String,String> templates){
		if (url.contains(variableResolverFactory.getPayloadTemplateType().getVariablePrefix()) && url.contains(variableResolverFactory.getPayloadTemplateType().getVariableSuffix())){
			WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, getPreviousNonPersonalBuild(wh, sBuild), buildState, extraParameters, sBuild.getParametersProvider().getAll(), templates);
			VariableResolver variableResolver = variableResolverFactory.buildVariableResolver(serialiser,content, content.getAllParameters());
			VariableMessageBuilder builder = variableResolverFactory.createVariableMessageBuilder(url, variableResolver);
			return builder.build();
		} else {
			return url;
		}
	}
	
	/** SBuild version with user and comment*/
	public String resolveTemplatedUrl(VariableResolverFactory variableResolverFactory, String url, BuildStateEnum buildState, SBuild sBuild, WebHookContentObjectSerialiser serialiser, SortedMap<String,String> extraParameters, Map<String,String> templates, String user, String comment){
		if (url.contains(variableResolverFactory.getPayloadTemplateType().getVariablePrefix()) && url.contains(variableResolverFactory.getPayloadTemplateType().getVariableSuffix())){
			WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sBuild, buildState, extraParameters, sBuild.getParametersProvider().getAll(), templates, user, comment);
			VariableResolver variableResolver = variableResolverFactory.buildVariableResolver(serialiser,content, content.getAllParameters());
			VariableMessageBuilder builder = variableResolverFactory.createVariableMessageBuilder(url, variableResolver);
			return builder.build();
		} else {
			return url;
		}
	}
	
	/** SQueuedBuild version */
	public String resolveTemplatedUrl(VariableResolverFactory variableResolverFactory, String url, BuildStateEnum buildState, SQueuedBuild sQueuedBuild, WebHookContentObjectSerialiser serialiser, SortedMap<String,String> extraParameters, Map<String,String> templates, String user, String comment){
		if (url.contains(variableResolverFactory.getPayloadTemplateType().getVariablePrefix()) && url.contains(variableResolverFactory.getPayloadTemplateType().getVariableSuffix())){
			WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, sQueuedBuild, buildState, extraParameters, templates, user, comment);
			VariableResolver variableResolver = variableResolverFactory.buildVariableResolver(serialiser,content, content.getAllParameters());
			VariableMessageBuilder builder = variableResolverFactory.createVariableMessageBuilder(url, variableResolver);
			return builder.build();
		} else {
			return url;
		}
	}
	/** ResponsibleChanged version */
	public String resolveTemplatedUrl(VariableResolverFactory variableResolverFactory, String url, BuildStateEnum buildState, WebHookResponsibilityHolder responsibilityHolder, WebHookContentObjectSerialiser serialiser, SortedMap<String,String> extraParameters, Map<String,String> templates){
		if (url.contains(variableResolverFactory.getPayloadTemplateType().getVariablePrefix()) && url.contains(variableResolverFactory.getPayloadTemplateType().getVariableSuffix())){
			WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, responsibilityHolder, buildState, extraParameters, templates);
			VariableResolver variableResolver = variableResolverFactory.buildVariableResolver(serialiser,content, content.getAllParameters());
			VariableMessageBuilder builder = variableResolverFactory.createVariableMessageBuilder(url, variableResolver);
			return builder.build();
		} else {
			return url;
		}
	}
	
	public VariableResolver getVariableResolver(VariableResolverFactory variableResolverFactory, WebHook wh, BuildStateEnum buildState, SBuild runningBuild, WebHookContentObjectSerialiser serialiser, SortedMap<String,String> extraParameters, Map<String,String> templates){
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, runningBuild, getPreviousNonPersonalBuild(wh, runningBuild), buildState, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return variableResolverFactory.buildVariableResolver(serialiser, content, content.getAllParameters());
	}
	
	public VariableResolver getVariableResolver(VariableResolverFactory variableResolverFactory, BuildStateEnum buildState, SBuild runningBuild, WebHookContentObjectSerialiser serialiser, SortedMap<String,String> extraParameters, Map<String,String> templates, String user, String comment){
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory, server, runningBuild, buildState, extraParameters, runningBuild.getParametersProvider().getAll(), templates, user, comment);
		return variableResolverFactory.buildVariableResolver(serialiser, content, content.getAllParameters());
	}
	
	public VariableResolver getVariableResolver(VariableResolverFactory variableResolverFactory, BuildStateEnum buildState, SQueuedBuild queuedBuild, WebHookContentObjectSerialiser serialiser, SortedMap<String,String> extraParameters, Map<String,String> templates, String user, String comment){
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory,  server,  queuedBuild,  buildState, extraParameters, templates, user, comment);
		return variableResolverFactory.buildVariableResolver(serialiser, content, content.getAllParameters());
	}
	
	public VariableResolver getVariableResolver(VariableResolverFactory variableResolverFactory, BuildStateEnum buildState, WebHookResponsibilityHolder responsibilityHolder, WebHookContentObjectSerialiser serialiser, SortedMap<String,String> extraParameters, Map<String,String> templates){
		WebHookPayloadContent content = new WebHookPayloadContent(variableResolverFactory,  server, responsibilityHolder,  buildState, extraParameters,  templates);
		return variableResolverFactory.buildVariableResolver(serialiser, content, content.getAllParameters());
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
	
	public static SortedMap<String,String> mergeParameters(SortedMap<String,String> parametersFromConfig, ParametersSupport build, String preferredDateFormat){
		SortedMap<String, String> newMap = new TreeMap<>();
		
		// First add the preferredDateFormat from the template. This can then be overriden 
		// by the webhook config (plugin-settings.xml) 
		// which can in turn be overriden by a user defined build property (webhook.*)
		
		newMap.put("preferredDateFormat", preferredDateFormat);
		
		// Now add any parameters passed in in the webhook configuration in plugin-settings.xml
		newMap.putAll(parametersFromConfig);

		// Then override any from ones declared in the actual build via webhook.*
		if (build != null) {
			Map<String,String> teamCityProperties = build.getParametersProvider().getAll(); 
			for (Entry<String,String> tcProperty : teamCityProperties.entrySet()){
				if (tcProperty.getKey().startsWith("webhook.")){
					newMap.put(tcProperty.getKey().substring("webhook.".length()), tcProperty.getValue());
				}
			}
		}
		return newMap;
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
