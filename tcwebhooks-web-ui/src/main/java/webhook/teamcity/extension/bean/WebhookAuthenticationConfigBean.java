package webhook.teamcity.extension.bean;

import webhook.teamcity.auth.WebHookAuthConfig;

public class WebhookAuthenticationConfigBean extends WebHookAuthConfig {
	
	public static WebhookAuthenticationConfigBean build(WebHookAuthConfig config){
		WebhookAuthenticationConfigBean bean = new WebhookAuthenticationConfigBean();
		bean.setType(config.getType());
		bean.setPreemptive(config.getPreemptive());
		bean.getParameters().putAll(config.getParameters());
		return bean;
	}

}
