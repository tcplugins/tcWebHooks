package webhook.teamcity.extension.bean.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplate;

public class RegisteredWebHookTemplateBean {
	
	List<SimpleTemplate> templateList = new ArrayList<SimpleTemplate>();

	public static RegisteredWebHookTemplateBean build(List<WebHookTemplate> registeredTemplates, Set<String> webhookFormats) {
		RegisteredWebHookTemplateBean bean = new RegisteredWebHookTemplateBean();
		for (WebHookTemplate t : registeredTemplates){
			for (String f :webhookFormats){
				if (t.supportsPayloadFormat(f)){
					bean.templateList.add(SimpleTemplate.build(t, f));
				}
			}
		}
		return bean;
	}
	
	public static class SimpleTemplate{
		private String description;
		private String shortName;
		private String formatName;
		private List<String> supportedStates = new ArrayList<String>();
		private List<String> supportedBranchStates = new ArrayList<String>();

		public static SimpleTemplate build(WebHookTemplate webHookTemplate, String format) {
			SimpleTemplate temp = new SimpleTemplate();
			temp.description = webHookTemplate.getTemplateDescription();
			temp.shortName = webHookTemplate.getTemplateShortName();
			temp.formatName = format;
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
	}		
}
