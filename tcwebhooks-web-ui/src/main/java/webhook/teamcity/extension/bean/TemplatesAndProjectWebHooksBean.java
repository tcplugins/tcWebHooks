package webhook.teamcity.extension.bean;

import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.extension.util.ProjectHistoryResolver.ProjectHistoryBean;

public class TemplatesAndProjectWebHooksBean {
	RegisteredWebHookTemplateBean registeredTemplates;
	ProjectWebHooksBean projectWebhookConfig;
	ProjectHistoryBean projectHistory;
	
	public static TemplatesAndProjectWebHooksBean build(RegisteredWebHookTemplateBean templates, ProjectWebHooksBean projects, ProjectHistoryBean history ){
		TemplatesAndProjectWebHooksBean bean = new TemplatesAndProjectWebHooksBean();
		bean.registeredTemplates = templates;
		bean.projectWebhookConfig = projects;
		bean.projectHistory = history;
		return bean;
	}
}
