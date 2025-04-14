package webhook.teamcity.extension.bean.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import webhook.Constants;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.settings.WebHookSettingsManager;

public class RegisteredWebHookTemplateBean {
	private static final Logger LOG = Logger.getInstance(RegisteredWebHookTemplateBean.class.getName());

	Map<String,SimpleTemplate> templateList = new LinkedHashMap<>();

	public static RegisteredWebHookTemplateBean build(
			List<WebHookPayloadTemplate> registeredTemplates, 
			List<WebHookPayload> webhookFormats,
			WebHookSettingsManager myWebHookSettingsManager,
			ProjectManager projectManager) 
	{
		RegisteredWebHookTemplateBean bean = new RegisteredWebHookTemplateBean();
		for (WebHookPayloadTemplate t : registeredTemplates){
			for (WebHookPayload f :webhookFormats){
				if (t.supportsPayloadFormat(f.getFormatShortName())){
					SimpleTemplate template = SimpleTemplate.build(t, f, myWebHookSettingsManager.getTemplateUsageCount(t.getTemplateId()), projectManager.findProjectById(t.getProjectId()));
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
			WebHookSettingsManager myWebHookSettingsManager,
			ProjectManager projectManager) 
	{
		RegisteredWebHookTemplateBean bean = new RegisteredWebHookTemplateBean();
		for (WebHookPayloadTemplate t : registeredTemplates){
			boolean validTemplate = false;
			for (WebHookPayload f :webhookFormats){
				if (t.supportsPayloadFormat(f.getFormatShortName())){
					SimpleTemplate template = SimpleTemplate.build(t, f, 
												templateManager.getTemplateState(t.getTemplateId(), TemplateState.BEST), 
												myWebHookSettingsManager.getTemplateUsageCount(t.getTemplateId()),
												projectManager.findProjectById(t.getProjectId()));
					bean.templateList.put(template.getTemplateId(), template);
					validTemplate = true;
				}
			}
			if (! validTemplate) {
				LOG.warn("RegisteredWebHookTemplateBean :: template does not appear to be valid: " + t.getTemplateDescription() + " (" + t.getTemplateId() + ")");
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
		private String projectId;
		private String projectExternalId;
		private String projectName;
		private String templateDescription;
		private String templateToolTip;
		private String formatShortName;
		private String formatDescription;
		private List<String> supportedStates = new ArrayList<>();
		private List<BuildStateEnum> supportedBuildEnumStates = new ArrayList<>();
		private List<String> supportedBranchStates = new ArrayList<>();
		private TemplateState templateState;
		private int webhookUsageCount = 0;

		public static SimpleTemplate build(WebHookPayloadTemplate webHookTemplate, WebHookPayload format, int usageCount, SProject sProject) {
			SimpleTemplate temp = new SimpleTemplate();
			
			temp.description = webHookTemplate.getTemplateDescription() + " (" + format.getFormatDescription() + ")";
			temp.templateDescription = webHookTemplate.getTemplateDescription();
			temp.templateToolTip = webHookTemplate.getTemplateToolTip();
			temp.formatDescription = format.getFormatDescription();
			temp.templateId = webHookTemplate.getTemplateId();
			temp.projectId = webHookTemplate.getProjectId();
			if (sProject != null) {
				temp.projectExternalId = sProject.getExternalId();
				if (sProject.getProjectId().equals(Constants.ROOT_PROJECT_ID)) {
					temp.projectName = sProject.getExternalId();
				} else {
					temp.projectName = sProject.getName();
				}
			}
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
		
		public static SimpleTemplate build(WebHookPayloadTemplate webHookTemplate, WebHookPayload format, TemplateState templateState, int usageCount, SProject sProject ) {
			SimpleTemplate temp = build(webHookTemplate, format, usageCount, sProject);
			temp.templateState = templateState;
			return temp;
		}
		
		public String getTemplateId() {
			return templateId;
		}
		
		public String getProjectId() {
			return projectId;
		}
		
		public String getProjectExternalId() {
			return projectExternalId;
		}
		
		public String getProjectName() {
			return projectName;
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
