package webhook.teamcity.extension.bean;

import java.util.Map;

import webhook.teamcity.extension.bean.RegisteredWebhookAuthenticationTypesBean.SimpleAuthType;
import webhook.teamcity.extension.bean.template.RegisteredWebHookTemplateBean;

public class TemplatesAndProjectWebHooksBean {
	RegisteredWebHookTemplateBean registeredTemplates;
	Map<String,SimpleAuthType> registeredAuthTypes;
	ProjectWebHooksBean projectWebhookConfig;
	
	public static TemplatesAndProjectWebHooksBeanResponseWrapper build(
													RegisteredWebHookTemplateBean templates, 
													ProjectWebHooksBean projects, 
													RegisteredWebhookAuthenticationTypesBean authTypes) {
		
		TemplatesAndProjectWebHooksBean bean = new TemplatesAndProjectWebHooksBean();
		bean.registeredTemplates = templates;
		bean.registeredAuthTypes = authTypes.getAuthenticators();
		bean.projectWebhookConfig = projects;
		return new TemplatesAndProjectWebHooksBeanResponseWrapper(bean);
		
	}
	
	public static class TemplatesAndProjectWebHooksBeanResponseWrapper{
		TemplatesAndProjectWebHooksBean templatesAndWebhooks;
		
		public TemplatesAndProjectWebHooksBeanResponseWrapper(TemplatesAndProjectWebHooksBean bean) {
			templatesAndWebhooks = bean;
		}
		
	}
}
