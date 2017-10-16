package webhook.teamcity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.ParametersSupport;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import webhook.WebHook;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.util.TemplateMatcher.VariableResolver;
import webhook.teamcity.payload.util.VariableMessageBuilder;
import webhook.teamcity.payload.util.WebHooksBeanUtilsVariableResolver;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookContentBuilder {
	private final WebHookPayloadManager payloadManager;
	private final WebHookTemplateResolver webHookTemplateResolver;
	

	public WebHookContentBuilder(WebHookPayloadManager manager, WebHookTemplateResolver resolver) {
		this.payloadManager = manager;
		this.webHookTemplateResolver = resolver;
	}
	
	public WebHook buildWebHookContent(WebHook wh, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, boolean isOverrideEnabled) throws WebHookPayloadContentAssemblyException{
		WebHookPayload payloadFormat = payloadManager.getFormat(whc.getPayloadFormat());
		WebHookTemplateContent templateForThisBuild;
		wh.setContentType(payloadFormat.getContentType());
		
		if (state.equals(BuildStateEnum.BUILD_STARTED)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_STARTED));
			if (wh.isEnabled()){
				Loggers.SERVER.debug("WebHookContentBuilder::buildWebHookContent BUILD_STARTED ** 01 ** ID: " + sBuild.getBuildId());
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate(), payloadFormat);
				Loggers.SERVER.debug("WebHookContentBuilder::buildWebHookContent BUILD_STARTED ** 02 ** ID: " + sBuild.getBuildId());
				wh.setPayload(payloadFormat.buildStarted(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				Loggers.SERVER.debug("WebHookContentBuilder::buildWebHookContent BUILD_STARTED ** 03 ** ID: " + sBuild.getBuildId());
				wh.setUrl(resolveTemplatedUrl(wh, whc.getUrl(), state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				Loggers.SERVER.debug("WebHookContentBuilder::buildWebHookContent BUILD_STARTED ** 04 ** ID: " + sBuild.getBuildId());
				wh.checkFilters(getVariableResolver(wh, state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				Loggers.SERVER.debug("WebHookContentBuilder::buildWebHookContent BUILD_STARTED ** 05 ** ID: " + sBuild.getBuildId());
			}
		} else if (state.equals(BuildStateEnum.CHANGES_LOADED)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.CHANGES_LOADED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.changesLoaded(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(wh, whc.getUrl(), state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(wh, state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BUILD_INTERRUPTED)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_INTERRUPTED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.buildInterrupted(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(wh, whc.getUrl(), state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(wh, state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BEFORE_BUILD_FINISHED)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BEFORE_BUILD_FINISHED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.beforeBuildFinish(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(wh, whc.getUrl(), state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(wh, state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BUILD_FINISHED) || state.equals(BuildStateEnum.BUILD_SUCCESSFUL) || state.equals(BuildStateEnum.BUILD_FAILED) || state.equals(BuildStateEnum.BUILD_FIXED) || state.equals(BuildStateEnum.BUILD_BROKEN)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(
					BuildStateEnum.BUILD_FINISHED, 
					sBuild.getStatusDescriptor().isSuccessful(),
					hasBuildChangedHistoricalState(sBuild, getPreviousNonPersonalBuild(wh, sBuild))));
			
			if (wh.isEnabled() || isOverrideEnabled){
				templateForThisBuild = findTemplateForState(sBuild, BuildState.getEffectiveState(state, sBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sBuild,getPreviousNonPersonalBuild(wh, sBuild))), whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.buildFinished(sBuild, getPreviousNonPersonalBuild(wh, sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));;
				wh.setUrl(resolveTemplatedUrl(wh, whc.getUrl(), BuildState.getEffectiveState(state, sBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sBuild, getPreviousNonPersonalBuild(wh, sBuild))), sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
				wh.checkFilters(getVariableResolver(wh, state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		}
		return wh;
	}
	
	public static String getPreferredDateFormat(WebHookTemplateContent templateContent){
		if (templateContent != null){
			return templateContent.getPreferredDateTimeFormat();
		}
		return "";
	}

	public String resolveTemplatedUrl(WebHook wh, String url, BuildStateEnum buildState, SBuild runningBuild, SortedMap<String,String> extraParameters, Map<String,String> templates){
		if (url.contains("${") && url.contains("}")){
			WebHookPayloadContent content = new WebHookPayloadContent(payloadManager.getServer(), runningBuild, getPreviousNonPersonalBuild(wh, runningBuild), buildState, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
			VariableMessageBuilder builder = VariableMessageBuilder.create(url, new WebHooksBeanUtilsVariableResolver(content, content.getAllParameters()));
			return builder.build();
		} else {
			return url;
		}
	}
	
	public VariableResolver getVariableResolver(WebHook wh, BuildStateEnum buildState, SBuild runningBuild, SortedMap<String,String> extraParameters, Map<String,String> templates){
		WebHookPayloadContent content = new WebHookPayloadContent(payloadManager.getServer(), runningBuild, getPreviousNonPersonalBuild(wh, runningBuild), buildState, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
		return new WebHooksBeanUtilsVariableResolver(content, content.getAllParameters());
	}
	
	public WebHookTemplateContent findTemplateForState(
			SBuild sRunningBuild, BuildStateEnum state,
			String payloadtemplateName, WebHookPayload payloadFormat) {
		WebHookTemplateContent templateForThisBuild;
		if (sRunningBuild.getBranch() != null){ 
			// We have a branch aware sBuild. Get the branch template.
			templateForThisBuild = webHookTemplateResolver.findWebHookBranchTemplate(state, sRunningBuild.getBuildType(), payloadFormat.getFormatShortName(), payloadtemplateName);
		} else {
			// Branch is null. TeamCity is not aware of branch support for this sBuild, so get the non-branch template.
			templateForThisBuild = webHookTemplateResolver.findWebHookTemplate(state, sRunningBuild.getBuildType(), payloadFormat.getFormatShortName(), payloadtemplateName);
		}
		return templateForThisBuild;
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
		Map<String,String> teamCityProperties = build.getParametersProvider().getAll(); 
		for (Entry<String,String> tcProperty : teamCityProperties.entrySet()){
			if (tcProperty.getKey().startsWith("webhook.")){
				newMap.put(tcProperty.getKey().substring("webhook.".length()), tcProperty.getValue());
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
