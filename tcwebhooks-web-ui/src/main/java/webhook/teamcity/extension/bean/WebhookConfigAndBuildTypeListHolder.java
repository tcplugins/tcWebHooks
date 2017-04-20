package webhook.teamcity.extension.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.settings.WebHookConfig;

public class WebhookConfigAndBuildTypeListHolder {
	public String url;
	public String uniqueKey; 
	public boolean enabled;
	public String payloadFormat;
	public String payloadTemplate;
	public String payloadFormatForWeb = "Unknown";
	public List<StateBean> states = new ArrayList<StateBean>();
	public boolean allBuildTypesEnabled;
	public boolean subProjectsEnabled;
	private List<WebhookBuildTypeEnabledStatusBean> builds = new ArrayList<WebhookBuildTypeEnabledStatusBean>();
	private String enabledEventsListForWeb;
	private String enabledBuildsListForWeb;
	private WebhookAuthenticationConfigBean authConfig = null;
	
	public WebhookConfigAndBuildTypeListHolder(WebHookConfig config, Collection<WebHookPayload> registeredPayloads, List<WebHookPayloadTemplate> templateList) {
		url = config.getUrl();
		uniqueKey = config.getUniqueKey();
		enabled = config.getEnabled();
		payloadFormat = config.getPayloadFormat();
		payloadTemplate = config.getPayloadTemplate();
		setEnabledEventsListForWeb(config.getEnabledListAsString());
		setEnabledBuildsListForWeb(config.getBuildTypeCountAsFriendlyString());
		allBuildTypesEnabled = config.isEnabledForAllBuildsInProject();
		subProjectsEnabled = config.isEnabledForSubProjects();
		for (BuildStateEnum state : config.getBuildStates().getStateSet()){
			states.add(new StateBean(state.getShortName(), config.getBuildStates().enabled(state)));
		}
		if (config.getAuthenticationConfig() != null){
			this.authConfig = WebhookAuthenticationConfigBean.build(config.getAuthenticationConfig());
		}
		WebHookPayloadTemplate t = null;
		
		if (payloadFormat != null){
			for (WebHookPayloadTemplate template : templateList){
				if (template.supportsPayloadFormat(payloadFormat) && template.getTemplateShortName().equals(payloadTemplate)){
					t = template;
				}
			}
			
			for (WebHookPayload payload : registeredPayloads){
				if (payload.getFormatShortName().equalsIgnoreCase(payloadFormat)){
					if (t != null){
						this.payloadFormatForWeb = t.getTemplateDescription() + " (" + payload.getFormatDescription() + ")";
					} else {
						this.payloadFormatForWeb = payload.getFormatDescription();
					}
				}
			}
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

	public String getEnabledEventsListForWeb() {
		return enabledEventsListForWeb;
	}

	public void setEnabledEventsListForWeb(String enabledEventsListForWeb) {
		this.enabledEventsListForWeb = enabledEventsListForWeb;
	}

	public String getEnabledBuildsListForWeb() {
		return enabledBuildsListForWeb;
	}

	public void setEnabledBuildsListForWeb(String enabledBuildsListForWeb) {
		this.enabledBuildsListForWeb = enabledBuildsListForWeb;
	}
	
	public String getUniqueKey() {
		return uniqueKey;
	}
	
	public String getPayloadFormat() {
		return payloadFormat;
	}
	
	public String getPayloadFormatForWeb() {
		return payloadFormatForWeb;
	}
	
	public String getPayloadTemplate() {
		return payloadTemplate;
	}
	
	public String getUrl() {
		return url;
	}
	
	public List<StateBean> getStates() {
		return states;
	}
	
	public WebhookAuthenticationConfigBean getAuthConfig() {
		return authConfig;
	}
	
	public void setAuthConfig(WebhookAuthenticationConfigBean authConfig) {
		this.authConfig = authConfig;
	}
	
}
