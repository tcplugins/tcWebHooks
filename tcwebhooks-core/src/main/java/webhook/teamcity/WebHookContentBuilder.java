package webhook.teamcity;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jetbrains.buildServer.serverSide.ParametersSupport;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import org.jetbrains.annotations.Nullable;

import webhook.WebHook;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.WebHookPayloadContent;
import webhook.teamcity.payload.util.VariableMessageBuilder;
import webhook.teamcity.payload.util.WebHooksBeanUtilsVariableResolver;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookContentBuilder {
	private final SBuildServer buildServer;
	private final WebHookPayloadManager payloadManager;
	private final WebHookTemplateResolver webHookTemplateResolver;
	

	public WebHookContentBuilder(SBuildServer server, WebHookPayloadManager manager, WebHookTemplateResolver resolver) {
		this.payloadManager = manager;
		this.webHookTemplateResolver = resolver;
		this.buildServer = server;
	}
	
	public WebHook buildWebHookContent(WebHook wh, WebHookConfig whc, SBuild sBuild, BuildStateEnum state, boolean isOverrideEnabled){
		WebHookPayload payloadFormat = payloadManager.getFormat(whc.getPayloadFormat());
		WebHookTemplateContent templateForThisBuild;
		wh.setContentType(payloadFormat.getContentType());
		
		if (state.equals(BuildStateEnum.BUILD_STARTED)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_STARTED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.buildStarted(sBuild, getPreviousNonPersonalBuild(sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(whc.getUrl(), state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.CHANGES_LOADED)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.CHANGES_LOADED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.changesLoaded(sBuild, getPreviousNonPersonalBuild(sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(whc.getUrl(), state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BUILD_INTERRUPTED)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_INTERRUPTED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.buildInterrupted(sBuild, getPreviousNonPersonalBuild(sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(whc.getUrl(), state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BEFORE_BUILD_FINISHED)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BEFORE_BUILD_FINISHED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.beforeBuildFinish(sBuild, getPreviousNonPersonalBuild(sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));
				wh.setUrl(resolveTemplatedUrl(whc.getUrl(), state, sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
			}
		} else if (state.equals(BuildStateEnum.BUILD_FINISHED) || state.equals(BuildStateEnum.BUILD_SUCCESSFUL) || state.equals(BuildStateEnum.BUILD_FAILED) || state.equals(BuildStateEnum.BUILD_FIXED) || state.equals(BuildStateEnum.BUILD_BROKEN)){
			wh.setEnabled(whc.isEnabledForBuildType(sBuild.getBuildType()) && wh.getBuildStates().enabled(
					BuildStateEnum.BUILD_FINISHED, 
					sBuild.getStatusDescriptor().isSuccessful(),
					hasBuildChangedHistoricalState(sBuild)));
			
			if (wh.isEnabled() || isOverrideEnabled){
				templateForThisBuild = findTemplateForState(sBuild, BuildState.getEffectiveState(state, sBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sBuild)), whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.buildFinished(sBuild, getPreviousNonPersonalBuild(sBuild), mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates(), templateForThisBuild));;
				wh.setUrl(resolveTemplatedUrl(whc.getUrl(), BuildState.getEffectiveState(state, sBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sBuild)), sBuild, mergeParameters(whc.getParams(),sBuild, getPreferredDateFormat(templateForThisBuild)), whc.getEnabledTemplates()));
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

	public String resolveTemplatedUrl(String url, BuildStateEnum buildState, SBuild runningBuild, SortedMap<String,String> extraParameters, Map<String,String> templates){
		if (url.contains("${") && url.contains("}")){
			WebHookPayloadContent content = new WebHookPayloadContent(payloadManager.getServer(), runningBuild, getPreviousNonPersonalBuild(runningBuild), buildState, extraParameters, runningBuild.getParametersProvider().getAll(), templates);
			VariableMessageBuilder builder = VariableMessageBuilder.create(url, new WebHooksBeanUtilsVariableResolver(content, content.getAllParameters()));
			return builder.build();
		} else {
			return url;
		}
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
		for (String key : teamCityProperties.keySet()){
			if (key.startsWith("webhook.")){
				newMap.put(key.substring("webhook.".length()), teamCityProperties.get(key));
			}
		}
		return newMap;
	}
	
	@Nullable
	private SFinishedBuild getPreviousNonPersonalBuild(SBuild paramSRunningBuild)
	  {
	    List<SFinishedBuild> localList = this.buildServer.getHistory().getEntriesBefore(paramSRunningBuild, false);

	    for (SFinishedBuild localSFinishedBuild : localList)
	      if (!(localSFinishedBuild.isPersonal())) return localSFinishedBuild;
	    return null;
	}
	
	private boolean hasBuildChangedHistoricalState(SBuild sRunningBuild){
		SFinishedBuild previous = getPreviousNonPersonalBuild(sRunningBuild);
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
