package webhook.teamcity.extension.bean.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookTemplate;

public class RegisteredWebHookTemplateBean {
	
	Map<String,SimpleTemplate> templateList = new HashMap<String,SimpleTemplate>();

	public static RegisteredWebHookTemplateBean build(List<WebHookTemplate> registeredTemplates, List<WebHookPayload> webhookFormats) {
		RegisteredWebHookTemplateBean bean = new RegisteredWebHookTemplateBean();
		for (WebHookTemplate t : registeredTemplates){
			for (WebHookPayload f :webhookFormats){
				if (t.supportsPayloadFormat(f.getFormatShortName())){
					SimpleTemplate template = SimpleTemplate.build(t, f);
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
		private String shortName;
		private String formatName;
		private String templateFormatCombinationKey;
		private List<String> supportedStates = new ArrayList<String>();
		private List<String> supportedBranchStates = new ArrayList<String>();

		public static SimpleTemplate build(WebHookTemplate webHookTemplate, WebHookPayload format) {
			SimpleTemplate temp = new SimpleTemplate();
			
			temp.description = webHookTemplate.getTemplateDescription() + " (" + format.getFormatDescription() + ")";
			temp.shortName = webHookTemplate.getTemplateShortName();
			temp.formatName = format.getFormatShortName();
			temp.templateFormatCombinationKey = webHookTemplate.getTemplateShortName() + "_" + format.getFormatShortName();
			for (BuildStateEnum s : webHookTemplate.getSupportedBuildStates()){
				temp.supportedStates.add(s.getShortName());
			}
			for (BuildStateEnum s : webHookTemplate.getSupportedBranchBuildStates()){
				temp.supportedBranchStates.add(s.getShortName());
			}
			return temp;
		}
		
		public String getShortName() {
			return shortName;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getFormatName() {
			return formatName;
		}
		
		public List<String> getSupportedStates() {
			return supportedStates;
		}
		
		public List<String> getSupportedBranchStates() {
			return supportedBranchStates;
		}
		
		public String getTemplateFormatCombinationKey() {
			return templateFormatCombinationKey;
		}
	}		
}
