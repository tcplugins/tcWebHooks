package webhook.teamcity.extension.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.settings.WebHookConfig;

@Getter
public class WebhookConfigAndBuildTypeListHolder {
	private String url;
	private String uniqueKey; 
	private boolean enabled;
	private String payloadTemplate;
	private String payloadFormatForWeb = "Unknown";
	private List<StateBean> states = new ArrayList<>();
	private boolean allBuildTypesEnabled;
	private boolean subProjectsEnabled;
	
	@Setter
	private List<WebhookBuildTypeEnabledStatusBean> builds = new ArrayList<>();
	
	@Setter
	private String enabledEventsListForWeb;
	@Setter
	private String enabledBuildsListForWeb;
	
	@Setter
	private WebhookAuthenticationConfigBean authConfig = null;
	
	public WebhookConfigAndBuildTypeListHolder(WebHookConfig config, Collection<WebHookPayload> registeredPayloads, List<WebHookPayloadTemplate> templateList) {
		url = config.getUrl();
		uniqueKey = config.getUniqueKey();
		enabled = config.getEnabled();
		payloadTemplate = config.getPayloadTemplate();
		setEnabledEventsListForWeb(config.getEnabledListAsString());
		setEnabledBuildsListForWeb(config.getBuildTypeCountAsFriendlyString());
		allBuildTypesEnabled = config.isEnabledForAllBuildsInProject();
		subProjectsEnabled = config.isEnabledForSubProjects();
		addBuildStatesFromConfig(config);
		
		if (config.getAuthenticationConfig() != null){
			this.authConfig = WebhookAuthenticationConfigBean.build(config.getAuthenticationConfig());
		}
		WebHookPayloadTemplate t = null;
		
		for (WebHookPayloadTemplate template : templateList){
			if (template.getTemplateId().equals(payloadTemplate)){
				t = template;
			}
		}
			
		if (t != null){
			for (WebHookPayload payload : registeredPayloads){
				if (t.supportsPayloadFormat(payload.getFormatShortName())){
					this.payloadFormatForWeb = t.getTemplateDescription() + " (" + payload.getFormatDescription() + ")";
					break;
				}
			}
		}
		if (this.payloadFormatForWeb.equalsIgnoreCase("Unknown")) {
			this.payloadFormatForWeb = "Unknown Template: '" + payloadTemplate + "'";
		}
	}

	private void addBuildStatesFromConfig(WebHookConfig config) {
		for (BuildStateEnum state : config.getBuildStates().getStateSet()){
			states.add(new StateBean(state.getShortName(), config.getBuildStates().enabled(state)));
		}
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
	
	public void addWebHookBuildType(WebhookBuildTypeEnabledStatusBean status){
		this.builds.add(status);
	}
	
}
