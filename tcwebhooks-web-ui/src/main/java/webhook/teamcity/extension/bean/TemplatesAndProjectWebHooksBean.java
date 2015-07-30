package webhook.teamcity.extension.bean;

import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;

public class TemplatesAndProjectWebHooksBean {
	RegisteredWebHookTemplateBean registeredTemplates;
	ProjectWebHooksBean projectWebhookConfig;
	
	public static TemplatesAndProjectWebHooksBean build(RegisteredWebHookTemplateBean templates, ProjectWebHooksBean projects){
		TemplatesAndProjectWebHooksBean bean = new TemplatesAndProjectWebHooksBean();
		bean.registeredTemplates = templates;
		bean.projectWebhookConfig = projects;
		return bean;
	}
}
