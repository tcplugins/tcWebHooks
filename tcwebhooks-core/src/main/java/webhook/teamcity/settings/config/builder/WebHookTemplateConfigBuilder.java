package webhook.teamcity.settings.config.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import webhook.teamcity.settings.config.WebHookTemplateConfig;
import webhook.teamcity.settings.entity.WebHookTemplateEntity;

public class WebHookTemplateConfigBuilder {
	
	
	public static WebHookTemplateEntity buildEntity(WebHookTemplateConfig config) {
		return WebHookTemplateEntity.build(config);
	}
	
	public static WebHookTemplateConfig buildConfig(WebHookTemplateEntity entity) {
		return build(entity);
	}
	
	public static WebHookTemplateConfig copyConfig(WebHookTemplateConfig config) {
		return new WebHookTemplateConfig();		
	}
	
	private static WebHookTemplateConfig build (WebHookTemplateEntity entity) {
		WebHookTemplateConfig config = new WebHookTemplateConfig(entity.getName(), entity.isEnabled());
		config.setRank(entity.getRank());
		
		if (entity.getDefaultTemplate() != null) {
			config.setDefaultTemplate(new WebHookTemplateConfig.WebHookTemplateText(
					entity.getDefaultTemplate().isUseTemplateTextForBranch(),
					entity.getDefaultTemplate().getTemplateContent()
														));
		}
		if (entity.getDefaultBranchTemplate() != null) {
			config.setDefaultBranchTemplate(new WebHookTemplateConfig.WebHookTemplateBranchText(entity.getDefaultBranchTemplate().getTemplateContent()));
		}
		
		if (entity.getTemplateDescription() != null) {
			config.setTemplateDescription(entity.getTemplateDescription());
		}
		
		if (entity.getTemplateToolTip() != null) {
			config.setTemplateToolTip(entity.getTemplateToolTip());
		}
		
		if (entity.getPreferredDateTimeFormat() != null) {
			config.setPreferredDateTimeFormat(entity.getPreferredDateTimeFormat());
		}
		
		if (entity.getFormat() != null) {
			config.setFormat(entity.getFormat());
		}
		
		WebHookTemplateConfig.WebHookTemplateItems templates = new WebHookTemplateConfig.WebHookTemplateItems();
		List<WebHookTemplateConfig.WebHookTemplateItem> templateItems = new ArrayList<>();
		if (entity.getTemplates() == null) {
			templates.setMaxId(0);
		} else {
			templates.setMaxId(entity.getTemplates().getMaxId());
			templateItems.addAll(buildAll(entity.getTemplates().getTemplates()));
		}
		templates.setTemplates(templateItems);
		config.setTemplates(templates);
		return config;
	}
	
	private static Collection<WebHookTemplateConfig.WebHookTemplateItem> buildAll(
			Collection<WebHookTemplateEntity.WebHookTemplateItem> templateItems) {
		List<WebHookTemplateConfig.WebHookTemplateItem> items = new ArrayList<>();
		for (WebHookTemplateEntity.WebHookTemplateItem item : templateItems) {
			WebHookTemplateConfig.WebHookTemplateItem i = new WebHookTemplateConfig.WebHookTemplateItem();
			i.setTemplateText(new WebHookTemplateConfig.WebHookTemplateText(
										item.getTemplateText().isUseTemplateTextForBranch(),
										item.getTemplateText().getTemplateContent()
									));
			i.setBranchTemplateText(new WebHookTemplateConfig.WebHookTemplateBranchText(
										item.getBranchTemplateText().getTemplateContent()
									));
			i.setEnabled(item.isEnabled());
			i.setId(item.getId());
			
			List<WebHookTemplateConfig.WebHookTemplateState> states = new ArrayList<>();
			for (WebHookTemplateEntity.WebHookTemplateState state : item.getStates()) {
				states.add(new WebHookTemplateConfig.WebHookTemplateState(state.getType(), state.isEnabled()));
			}
			i.setStates(states);
			items.add(i);
		}
		return items;
	}

}
