package webhook.teamcity.extension.bean;

import java.util.ArrayList;
import java.util.List;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.settings.WebHookConfig;

public class WebhookConfigAndBuildTypeListHolder {
	public String url;
	public String uniqueKey; 
	public boolean enabled;
	public String payloadFormat;
	public List<StateBean> states = new ArrayList<StateBean>();
	public boolean allBuildTypesEnabled;
	private List<WebhookBuildTypeEnabledStatusBean> builds = new ArrayList<WebhookBuildTypeEnabledStatusBean>();
	
	public WebhookConfigAndBuildTypeListHolder(WebHookConfig config) {
		url = config.getUrl();
		uniqueKey = config.getUniqueKey();
		enabled = config.getEnabled();
		payloadFormat = config.getPayloadFormat();
		allBuildTypesEnabled = config.isEnabledForAllBuildsInProject();
		for (BuildStateEnum state : config.getBuildStates().getStateSet()){
			states.add(new StateBean(state.name(), config.getBuildStates().enabled(state)));
		}
	}

	public List<WebhookBuildTypeEnabledStatusBean> getBuilds() {
		return builds;
	}
	
	public String getEnabledBuildTypes(){
		StringBuilder types = new StringBuilder();
		for (WebhookBuildTypeEnabledStatusBean build : getBuilds()){
			if (build.enabled){
				types.append(build.buildTypeId).append(",");
			}
		}
		return types.toString();
		
	}

	public void setBuilds(List<WebhookBuildTypeEnabledStatusBean> builds) {
		this.builds = builds;
	}
	
	
	public void addWebHookBuildType(WebhookBuildTypeEnabledStatusBean status){
		this.builds.add(status);
	}
	
}
