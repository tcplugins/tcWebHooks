package webhook.teamcity.extension.bean.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.settings.WebHookSettingsManager;

public class RegisteredWebHookTemplateBean {
	
	Map<String,SimpleTemplate> templateList = new LinkedHashMap<>();

	public static RegisteredWebHookTemplateBean build(
			List<WebHookPayloadTemplate> registeredTemplates, 
			List<WebHookPayload> webhookFormats,
			WebHookSettingsManager myWebHookSettingsManager) 
	{
		RegisteredWebHookTemplateBean bean = new RegisteredWebHookTemplateBean();
		for (WebHookPayloadTemplate t : registeredTemplates){
			for (WebHookPayload f :webhookFormats){
				if (t.supportsPayloadFormat(f.getFormatShortName())){
					SimpleTemplate template = SimpleTemplate.build(t, f, myWebHookSettingsManager.getTemplateUsageCount(t.getTemplateId()));
					bean.templateList.put(template.getTemplateId(), template);
				}
			}
		}
		return bean;
	}
	
	public static RegisteredWebHookTemplateBean build(
			WebHookTemplateManager templateManager, 
			List<WebHookPayloadTemplate> registeredTemplates, 
			List<WebHookPayload> webhookFormats, 
			WebHookSettingsManager myWebHookSettingsManager) 
	{
		RegisteredWebHookTemplateBean bean = new RegisteredWebHookTemplateBean();
		for (WebHookPayloadTemplate t : registeredTemplates){
			for (WebHookPayload f :webhookFormats){
				if (t.supportsPayloadFormat(f.getFormatShortName())){
					SimpleTemplate template = SimpleTemplate.build(t, f, 
												templateManager.getTemplateState(t.getTemplateId(), TemplateState.BEST), 
												myWebHookSettingsManager.getTemplateUsageCount(t.getTemplateId()));
					bean.templateList.put(template.getTemplateId(), template);
				}
			}
		}
		return bean;
	}
	
	public Collection<SimpleTemplate> getTemplateList() {
		return templateList.values();
	}
	
	public Map<String, SimpleTemplate> getTemplateMap() {
		return templateList;
	}
	
	public static class SimpleTemplate{
		private String description;
		private String templateId;
		private String templateDescription;
		private String templateToolTip;
		private String formatShortName;
		private String formatDescription;
		private List<String> supportedStates = new ArrayList<>();
		private List<BuildStateEnum> supportedBuildEnumStates = new ArrayList<>();
		private List<String> supportedBranchStates = new ArrayList<>();
		private TemplateState templateState;
		private int webhookUsageCount = 0;

		public static SimpleTemplate build(WebHookPayloadTemplate webHookTemplate, WebHookPayload format, int usageCount) {
			SimpleTemplate temp = new SimpleTemplate();
			
			temp.description = webHookTemplate.getTemplateDescription() + " (" + format.getFormatDescription() + ")";
			temp.templateDescription = webHookTemplate.getTemplateDescription();
			temp.templateToolTip = webHookTemplate.getTemplateToolTip();
			temp.formatDescription = format.getFormatDescription();
			temp.templateId = webHookTemplate.getTemplateId();
			temp.formatShortName = format.getFormatShortName().toLowerCase();
			for (BuildStateEnum s : webHookTemplate.getSupportedBuildStates()){
				temp.supportedStates.add(s.getShortName());
				temp.supportedBuildEnumStates.add(s);
			}
			for (BuildStateEnum s : webHookTemplate.getSupportedBranchBuildStates()){
				temp.supportedBranchStates.add(s.getShortName());
			}
			temp.webhookUsageCount = usageCount;
			return temp;
		}
		
		public static SimpleTemplate build(WebHookPayloadTemplate webHookTemplate, WebHookPayload format, TemplateState templateState, int usageCount ) {
			SimpleTemplate temp = build(webHookTemplate, format, usageCount);
			temp.templateState = templateState;
			return temp;
		}
		
		public String getTemplateId() {
			return templateId;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getTemplateDescription() {
			return templateDescription;
		}
		
		public String getTemplateToolTip() {
			return templateToolTip;
		}
		
		public String getFormatShortName() {
			return formatShortName;
		}
		
		public String getFormatDescription() {
			return formatDescription;
		}
		
		public List<String> getSupportedStates() {
			return supportedStates;
		}
		
		public List<String> getSupportedBranchStates() {
			return supportedBranchStates;
		}
		
		public List<BuildStateEnum> getSupportedBuildEnumStates() {
			return supportedBuildEnumStates;
		}

		public TemplateState getTemplateState() {
			return templateState;
		}
		
		public int getWebhookUsageCount() {
			return webhookUsageCount;
		}
	}		
}
