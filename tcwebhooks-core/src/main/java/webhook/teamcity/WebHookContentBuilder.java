package webhook.teamcity;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jetbrains.annotations.Nullable;

import jetbrains.buildServer.serverSide.ParametersSupport;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.WebHook;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateContent;
import webhook.teamcity.payload.WebHookTemplateResolver;
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
	
	public WebHook buildWebHookContent(WebHook wh, WebHookConfig whc, SBuild sRunningBuild, BuildStateEnum state, boolean isOverrideEnabled){
		WebHookPayload payloadFormat = payloadManager.getFormat(whc.getPayloadFormat());
		WebHookTemplateContent templateForThisBuild;
		wh.setContentType(payloadFormat.getContentType());
		
		if (state.equals(BuildStateEnum.BUILD_STARTED)){
			wh.setEnabled(whc.isEnabledForBuildType(sRunningBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_STARTED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sRunningBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.buildStarted(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), mergeParameters(whc.getParams(),sRunningBuild), whc.getEnabledTemplates(), templateForThisBuild));
			}
		} else if (state.equals(BuildStateEnum.BUILD_INTERRUPTED)){
			wh.setEnabled(whc.isEnabledForBuildType(sRunningBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BUILD_INTERRUPTED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sRunningBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.buildInterrupted(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), mergeParameters(whc.getParams(),sRunningBuild), whc.getEnabledTemplates(), templateForThisBuild));
			}
		} else if (state.equals(BuildStateEnum.BEFORE_BUILD_FINISHED)){
			wh.setEnabled(whc.isEnabledForBuildType(sRunningBuild.getBuildType()) && wh.getBuildStates().enabled(BuildStateEnum.BEFORE_BUILD_FINISHED));
			if (wh.isEnabled()){
				templateForThisBuild = findTemplateForState(sRunningBuild, state, whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.beforeBuildFinish(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), mergeParameters(whc.getParams(),sRunningBuild), whc.getEnabledTemplates(), templateForThisBuild));
			}
		} else if (state.equals(BuildStateEnum.BUILD_FINISHED) || state.equals(BuildStateEnum.BUILD_SUCCESSFUL) || state.equals(BuildStateEnum.BUILD_FAILED) || state.equals(BuildStateEnum.BUILD_FIXED) || state.equals(BuildStateEnum.BUILD_BROKEN)){
			wh.setEnabled(whc.isEnabledForBuildType(sRunningBuild.getBuildType()) && wh.getBuildStates().enabled(
					BuildStateEnum.BUILD_FINISHED, 
					sRunningBuild.getStatusDescriptor().isSuccessful(),
					hasBuildChangedHistoricalState(sRunningBuild)));
			
			if (wh.isEnabled() || isOverrideEnabled){
				templateForThisBuild = findTemplateForState(sRunningBuild, BuildState.getEffectiveState(state, sRunningBuild.getStatusDescriptor().isSuccessful(), this.hasBuildChangedHistoricalState(sRunningBuild)), whc.getPayloadTemplate(), payloadFormat);
				wh.setPayload(payloadFormat.buildFinished(sRunningBuild, getPreviousNonPersonalBuild(sRunningBuild), mergeParameters(whc.getParams(),sRunningBuild), whc.getEnabledTemplates(), templateForThisBuild));;
			}
		}
		return wh;
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
	
	public static SortedMap<String,String> mergeParameters(SortedMap<String,String> parametersFromConfig, ParametersSupport build){
		SortedMap<String, String> newMap = new TreeMap<String,String>();
		
		Map<String,String> teamCityProperties = build.getParametersProvider().getAll(); 
		for (String key : teamCityProperties.keySet()){
			if (key.startsWith("webhook.")){
				newMap.put(key.substring("webhook.".length()), teamCityProperties.get(key));
			}
		}
		newMap.putAll(parametersFromConfig);
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
