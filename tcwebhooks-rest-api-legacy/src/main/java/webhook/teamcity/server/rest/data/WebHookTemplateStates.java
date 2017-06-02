package webhook.teamcity.server.rest.data;

import java.util.HashMap;
import java.util.Map;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateState;

public class WebHookTemplateStates {
	
	Map<BuildStateEnum, Boolean> buildStatesWithTemplate = new HashMap<>();
	
	public void addBuildState(BuildStateEnum state){
		buildStatesWithTemplate.put(state, true);
	}
	
	public void removeBuildState(BuildStateEnum state){
		buildStatesWithTemplate.put(state, false);
	}
	
	public boolean hasBuildEventTemplate(BuildStateEnum state) {
		return buildStatesWithTemplate.containsKey(state) && buildStatesWithTemplate.get(state);
	}
	
	public static WebHookTemplateStates build(WebHookTemplateConfig webHookTemplateConfig) {
		WebHookTemplateStates webHookTemplateStates = new WebHookTemplateStates();
		
		for (WebHookTemplateItem webHookTemplateItem : webHookTemplateConfig.getTemplates().getTemplates()) {
			for (WebHookTemplateState state : webHookTemplateItem.getStates()) {
				if (state.isEnabled() && BuildStateEnum.findBuildState(state.getType()) != null && state.getType().equals(BuildStateEnum.BUILD_FINISHED.getShortName())) {
					webHookTemplateStates.addBuildState(BuildStateEnum.BUILD_FAILED);
					webHookTemplateStates.addBuildState(BuildStateEnum.BUILD_SUCCESSFUL);
					webHookTemplateStates.addBuildState(BuildStateEnum.BUILD_BROKEN);
					webHookTemplateStates.addBuildState(BuildStateEnum.BUILD_FIXED);
				} else if (state.isEnabled() && BuildStateEnum.findBuildState(state.getType()) != null) {
					webHookTemplateStates.addBuildState(BuildStateEnum.findBuildState(state.getType()));
				}
			}
		}
		return webHookTemplateStates;
	}

	public boolean isAvailable(String type) {
		BuildStateEnum state = BuildStateEnum.findBuildState(type);
		if (state == null) {
			return false;
		}
		return isAvailable(state);
	}
	
	public boolean isAvailable(BuildStateEnum state) {
		return ! hasBuildEventTemplate(state);
	}

}
