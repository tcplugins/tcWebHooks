package webhook.teamcity.extension.bean;

import java.util.Map;

import webhook.teamcity.auth.WebHookAuthConfig;

public class WebhookAuthenticationConfigBean extends WebHookAuthConfig {
	
	public static WebhookAuthenticationConfigBean build(WebHookAuthConfig config){
		WebhookAuthenticationConfigBean bean = new WebhookAuthenticationConfigBean();
		bean.setType(config.getType());
		bean.setPreemptive(config.getPreemptive());
		bean.getParameters().putAll(config.getParameters());
		return bean;
	}

	public String getType(){
		return type;
	}

	public boolean isPreemptive() {
		return preemptive.booleanValue();
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

}
