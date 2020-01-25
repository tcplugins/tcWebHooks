package webhook.teamcity.extension.bean.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lombok.Builder;
import lombok.Data;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateBranchText;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateItem;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateState;
import webhook.teamcity.settings.config.WebHookTemplateConfig.WebHookTemplateText;

@Data
@Builder
public class EditTemplateRenderingBean {
	
	String templateId;
	String projectExternalId;
	int rank;
	String dateFormat;
	String templateDescription;
	String payloadFormat;
	String toolTipText;
	@Builder.Default List<BuildStateEnum> defaultTemplateStates = new ArrayList<>();
	Boolean defaultIsBranchTemplate; 
	WebHookTemplateText defaultTemplateItem;
	WebHookTemplateBranchText defaultTemplateBranchItem;
	@Builder.Default List<EditTemplateRenderingEventTemplateBean> buildEventTemplates = new ArrayList<>();
	TemplateState templateState;
	
	protected void removeBuildStateFromDefaultTemplate(BuildStateEnum state) {
		if (state != null) {
			this.defaultTemplateStates.remove(state);
		}
	}

	public static EditTemplateRenderingBean build(WebHookTemplateConfig config, TemplateState templateState, String projectExternalId) {
		EditTemplateRenderingBean bean = builder()
										 .templateId(config.getId())
										 .projectExternalId(projectExternalId)
										 .rank(config.getRank())
										 .dateFormat(config.getPreferredDateTimeFormat())
										 .templateDescription(config.getTemplateDescription())
										 .payloadFormat(config.getFormat())
										 .toolTipText(config.getTemplateToolTip())
										 .defaultTemplateStates(new ArrayList<BuildStateEnum>())
										 .buildEventTemplates(new ArrayList<EditTemplateRenderingEventTemplateBean>())
										 .templateState(templateState)
										 .build();
		for (WebHookTemplateItem eventStateItem : config.getTemplates().getTemplates()) {
			bean.buildEventTemplates.add(new EditTemplateRenderingEventTemplateBean(eventStateItem));
		}
		if (config.getDefaultTemplate() != null) {
			bean.setDefaultTemplateItem(config.getDefaultTemplate());
			bean.setDefaultIsBranchTemplate(config.getDefaultTemplate().isUseTemplateTextForBranch());
			bean.getDefaultTemplateStates().addAll(Arrays.asList(BuildStateEnum.getNotifyStates()));
			
			for (EditTemplateRenderingEventTemplateBean buildEventTemplate : bean.getBuildEventTemplates()) {
				bean.defaultTemplateStates.removeAll(buildEventTemplate.getBuildStates());
			}
		}
		if (config.getDefaultBranchTemplate() != null) {
			bean.setDefaultTemplateBranchItem(config.getDefaultBranchTemplate());
		}
		return bean;
	}
	
	@Data
	public static class EditTemplateRenderingEventTemplateBean {
		Set<BuildStateEnum> buildStates = new TreeSet<>();
		WebHookTemplateItem webHookTemplateItem;
		
		public EditTemplateRenderingEventTemplateBean(WebHookTemplateItem webHookTemplateItem) {
			this.webHookTemplateItem = webHookTemplateItem;
			for (WebHookTemplateState state : webHookTemplateItem.getStates()) {
				if (state.isEnabled() && BuildStateEnum.findBuildState(state.getType()) != null && state.getType().equals(BuildStateEnum.BUILD_FINISHED.getShortName())) {
					buildStates.add(BuildStateEnum.BUILD_FAILED);
					buildStates.add(BuildStateEnum.BUILD_SUCCESSFUL);
					buildStates.add(BuildStateEnum.BUILD_BROKEN);
					buildStates.add(BuildStateEnum.BUILD_FIXED);
				} else if (state.isEnabled() && BuildStateEnum.findBuildState(state.getType()) != null) {
					buildStates.add(BuildStateEnum.findBuildState(state.getType()));
				}
			}
		}
	}
}
