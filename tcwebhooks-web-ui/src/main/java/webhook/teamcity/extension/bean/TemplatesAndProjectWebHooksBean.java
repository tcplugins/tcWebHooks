package webhook.teamcity.extension.bean;

import java.util.Map;

import webhook.teamcity.extension.bean.RegisteredWebhookAuthenticationTypesBean.SimpleAuthType;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;
import webhook.teamcity.extension.util.ProjectHistoryResolver.ProjectHistoryBean;

public class TemplatesAndProjectWebHooksBean {
	RegisteredWebHookTemplateBean registeredTemplates;
	Map<String,SimpleAuthType> registeredAuthTypes;
	ProjectWebHooksBean projectWebhookConfig;
	//ProjectHistoryBean projectHistory;
	
	public static TemplatesAndProjectWebHooksBeanResponseWrapper build(
													RegisteredWebHookTemplateBean templates, 
													ProjectWebHooksBean projects, 
													//ProjectHistoryBean history, 
													RegisteredWebhookAuthenticationTypesBean authTypes) {
		
		TemplatesAndProjectWebHooksBean bean = new TemplatesAndProjectWebHooksBean();
		bean.registeredTemplates = templates;
		bean.registeredAuthTypes = authTypes.getAuthenticators();
		bean.projectWebhookConfig = projects;
		//bean.projectHistory = history;
		return new TemplatesAndProjectWebHooksBeanResponseWrapper(bean);
		
	}
	
	public static class TemplatesAndProjectWebHooksBeanResponseWrapper{
		TemplatesAndProjectWebHooksBean templatesAndWebhooks;
		
		public TemplatesAndProjectWebHooksBeanResponseWrapper(TemplatesAndProjectWebHooksBean bean) {
			templatesAndWebhooks = bean;
		}
		
	}
}
