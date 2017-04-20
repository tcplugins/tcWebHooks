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

public class RegisteredWebHookTemplateBean {
	
	Map<String,SimpleTemplate> templateList = new LinkedHashMap<String,SimpleTemplate>();

	public static RegisteredWebHookTemplateBean build(List<WebHookPayloadTemplate> registeredTemplates, List<WebHookPayload> webhookFormats) {
		RegisteredWebHookTemplateBean bean = new RegisteredWebHookTemplateBean();
		for (WebHookPayloadTemplate t : registeredTemplates){
			for (WebHookPayload f :webhookFormats){
				if (t.supportsPayloadFormat(f.getFormatShortName())){
					SimpleTemplate template = SimpleTemplate.build(t, f);
					bean.templateList.put(template.getTemplateFormatCombinationKey(), template);
				}
			}
		}
		return bean;
	}
	
	public static RegisteredWebHookTemplateBean build(WebHookTemplateManager templateManager, List<WebHookPayloadTemplate> registeredTemplates, List<WebHookPayload> webhookFormats) {
		RegisteredWebHookTemplateBean bean = new RegisteredWebHookTemplateBean();
		for (WebHookPayloadTemplate t : registeredTemplates){
			for (WebHookPayload f :webhookFormats){
				if (t.supportsPayloadFormat(f.getFormatShortName())){
					SimpleTemplate template = SimpleTemplate.build(t, f, templateManager.getTemplateState(t.getTemplateShortName()));
					bean.templateList.put(template.getTemplateFormatCombinationKey(), template);
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
		private String templateShortName;
		private String templateDescription;
		private String formatShortName;
		private String formatDescription;
		private String templateFormatCombinationKey;
		private List<String> supportedStates = new ArrayList<String>();
		private List<BuildStateEnum> supportedBuildEnumStates = new ArrayList<BuildStateEnum>();
		private List<String> supportedBranchStates = new ArrayList<String>();
		private TemplateState templateState;

		public static SimpleTemplate build(WebHookPayloadTemplate webHookTemplate, WebHookPayload format) {
			SimpleTemplate temp = new SimpleTemplate();
			
			temp.description = webHookTemplate.getTemplateDescription() + " (" + format.getFormatDescription() + ")";
			temp.templateDescription = webHookTemplate.getTemplateDescription();
			temp.formatDescription = format.getFormatDescription();
			temp.templateShortName = webHookTemplate.getTemplateShortName();
			temp.formatShortName = format.getFormatShortName().toLowerCase();
			temp.templateFormatCombinationKey = webHookTemplate.getTemplateShortName() + "_" + format.getFormatShortName().toLowerCase();
			for (BuildStateEnum s : webHookTemplate.getSupportedBuildStates()){
				temp.supportedStates.add(s.getShortName());
				temp.supportedBuildEnumStates.add(s);
			}
			for (BuildStateEnum s : webHookTemplate.getSupportedBranchBuildStates()){
				temp.supportedBranchStates.add(s.getShortName());
			}
			return temp;
		}
		
		public static SimpleTemplate build(WebHookPayloadTemplate webHookTemplate, WebHookPayload format, TemplateState templateState ) {
			SimpleTemplate temp = build(webHookTemplate, format);
			temp.templateState = templateState;
			return temp;
		}
		
		public String getTemplateShortName() {
			return templateShortName;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getTemplateDescription() {
			return templateDescription;
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
		
		public String getTemplateFormatCombinationKey() {
			return templateFormatCombinationKey;
		}
		
		public TemplateState getTemplateState() {
			return templateState;
		}
	}		
}
