package webhook.teamcity.settings.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import webhook.teamcity.settings.config.WebHookTemplateConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * 
	<?xml version="1.0" encoding="UTF-8"?>
	<webhook-templates>
		<webhook-template enabled="true" name="testXMLtemplate" rank="10">
			<default-template>{ "defaultBuildStatus" : "${buildStatus}" }</default-template>
			<default-branch-template>{ "defaultBuildStatus" : "${buildStatus}" }</default-branch-template>
			<template-description>"Test XML Template"</template-description>
			<template-tool-tip value="This is some tooltip text for the Test XML Template"/>
			<preferred-date-format value="yyyy-MM-dd'T'HH:mm:ss.SSSXXX"/>
			<formats>
				<format name="json" enabled="true" />
				<format name="nvpairs" enabled="true" />
				<format name="tailoredjson" enabled="true" />
			</formats>
			<templates max-id="10">
				<template id="10">
					<template-text>{ "buildStatus" : "${buildStatus}" }</template-text>
					<branch-template-text>{ "buildStatus" : "${buildStatus}" }</branch-template-text>
					<states>
						<state type="buildStarted" enabled="true" />
						<state type="beforeBuildFinish" enabled="true" />
						<state type="buildFinished" enabled="true" />
						<state type="buildBroken" enabled="false" />
						<state type="buildInterrupted" enabled="true" />
						<state type="buildSuccessful" enabled="true" />
						<state type="buildFixed" enabled="false" />
						<state type="buildFailed" enabled="true" />
						<state type="responsibilityChanged" enabled="false" />
					</states>
				</template>
			</templates>
		</webhook-template>
	</webhook-templates>

 */

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD)

@Data  // Let Lombok generate the getters and setters.
@NoArgsConstructor // Empty constructor for JAXB

@XmlRootElement(name = "webhook-template")
public class WebHookTemplateEntity {
	@NotNull @XmlAttribute(name="name")
	String name;
	
	@XmlAttribute
	boolean enabled = true;
	
	@XmlAttribute
	int rank = 10;
	
	@XmlElement(name="default-template")
	WebHookTemplateText defaultTemplate;
	
	@XmlElement(name="default-branch-template")
	WebHookTemplateBranchText defaultBranchTemplate;
	
	@NotNull 
	@XmlElement (name="template-description")
	String templateDescription;
	
	@XmlElement (name="template-tool-tip")
	String templateToolTip;
	
	@XmlElement (name="preferred-date-format")
	String preferredDateTimeFormat = "";
	
	@XmlElement(name="format") @XmlElementWrapper(name="formats")
	private List<WebHookTemplateFormat> formats = new ArrayList<WebHookTemplateFormat>();
	
	@XmlAttribute
	String format;
	
	@XmlElement(name="templates")
	WebHookTemplateItems templates;
	
	public WebHookTemplateEntity(String name, boolean enabled) {
		this.name = name;
		this.enabled = enabled;
	}
	
	public static WebHookTemplateEntity build (WebHookTemplateConfig config) {
		WebHookTemplateEntity entity = new WebHookTemplateEntity(config.getName(), config.isEnabled());
		entity.rank = config.getRank();
		
		if (config.getDefaultTemplate() != null) {
			entity.defaultTemplate = new WebHookTemplateText(
														config.getDefaultTemplate().isUseTemplateTextForBranch(),
														config.getDefaultTemplate().getTemplateContent()
														);
		}
		if (config.getDefaultBranchTemplate() != null) {
			entity.defaultBranchTemplate = new WebHookTemplateBranchText(config.getDefaultBranchTemplate().getTemplateContent());
		}
		
		if (config.getTemplateDescription() != null) {
			entity.setTemplateDescription(config.getTemplateDescription());
		}
		
		if (config.getTemplateToolTip() != null) {
			entity.setTemplateToolTip(config.getTemplateToolTip());
		}
		
		if (config.getPreferredDateTimeFormat() != null) {
			entity.setPreferredDateTimeFormat(config.getPreferredDateTimeFormat());
		}
		
		if (config.getFormat() != null) {
			entity.setFormat(config.getFormat());
		}
		
		entity.templates = new WebHookTemplateItems();
		entity.templates.maxId = config.getTemplates().getMaxId();
		entity.templates.getTemplates().addAll(WebHookTemplateItem.buildAll(config.getTemplates().getTemplates()));
		return entity;
	}
	
	public void fixTemplateIds() {
		if (templates != null){
			templates.fixTemplateIds();
		}
	}
	
	/**
	 * This was added to migrate from 
	 * <pre>
	 * &lt;template&gt;
	 * &nbsp;&lt;formats&gt;
	 * &nbsp;&nbsp;&lt;format name="jsonTemplate" enabled="true"&gt;
	 * &nbsp;&lt;/formats&gt;
	 * &lt;/template&gt;
	 * </pre> to <pre> 
	 * &nbsp;&lt;template format="jsonTemplate"&gt;
	 * &nbsp;&nbsp;...
	 * &nbsp;&lt;/template&gt;
	 * </pre>
	 * @return
	 */
	public String getFormat() {
		if (this.format == null && this.getFormats() != null) {
			for (WebHookTemplateFormat format : getFormats()) {
				if (format.enabled) {
					this.format = format.getName();
					break;
				}
			}
			this.formats = null;
		}
		return format;
	}
	
	@XmlType(name="templates") @Data @XmlAccessorType(XmlAccessType.FIELD) @NoArgsConstructor
	public static class WebHookTemplateItems {

		@XmlAttribute(name="max-id")
	    Integer maxId = 0;
		
		@XmlElements(@XmlElement(name="template", type=WebHookTemplateItem.class))
	    List<WebHookTemplateItem> templates = new ArrayList<WebHookTemplateItem>();

		public void fixTemplateIds() {
			List<Integer> usedIds = new ArrayList<>();
			for (WebHookTemplateItem item : templates){
				if (item.getId() == null || usedIds.contains(item.getId())) {
					item.setId(++maxId);
				} else {
					usedIds.add(item.getId());
				}
			}
		}
	    
	}
	
	@NoArgsConstructor // Empty constructor for JAXB
	@AllArgsConstructor
	@XmlType(name = "format") @Data  @XmlAccessorType(XmlAccessType.FIELD) 
	public static class WebHookTemplateFormat {
		@XmlAttribute
		String name;
		
		@XmlAttribute
		boolean enabled = true;
		
	}
	
	@NoArgsConstructor // Empty constructor for JAXB
	@AllArgsConstructor
	@XmlType @Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateText {
		@XmlAttribute (name="use-for-branch-template")
		boolean useTemplateTextForBranch = false;
		
		@NotNull @XmlValue
		String templateContent;
		
		public WebHookTemplateText(String templateContent){
			this.templateContent = templateContent;
		
		}
	}
	
	@NoArgsConstructor // Empty constructor for JAXB
	@XmlType @Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateBranchText {
	
		@NotNull @XmlValue
		String templateContent;
		
		public WebHookTemplateBranchText(String templateContent){
			this.templateContent = templateContent;
		}
	}
	
	@XmlRootElement
	@NoArgsConstructor // Empty constructor for JAXB
	@XmlType(name = "template") @Data  @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateItem {

		@NotNull @XmlElement(name="template-text")
		WebHookTemplateText templateText;
		
		@XmlElement(name="branch-template-text")
		WebHookTemplateBranchText branchTemplateText;
		
		@XmlAttribute
		boolean enabled = true;
		
		@XmlAttribute @Nullable
		Integer id;
		
		@XmlElement(name="state") @XmlElementWrapper(name="states")
		private List<WebHookTemplateState> states = new ArrayList<WebHookTemplateState>();

		public static Collection<? extends WebHookTemplateItem> buildAll(
				Collection<WebHookTemplateConfig.WebHookTemplateItem> templateItems) {
			List<WebHookTemplateItem> items = new ArrayList<>();
			for (WebHookTemplateConfig.WebHookTemplateItem item : templateItems) {
				WebHookTemplateItem i = new WebHookTemplateItem();
				i.templateText = new WebHookTemplateText(
											item.getTemplateText().isUseTemplateTextForBranch(),
											item.getTemplateText().getTemplateContent()
										);
				i.branchTemplateText = new WebHookTemplateBranchText(
											item.getBranchTemplateText().getTemplateContent()
										);
				i.enabled = item.isEnabled();
				i.id = item.getId();
				for (WebHookTemplateConfig.WebHookTemplateState state : item.getStates()) {
					i.states.add(new WebHookTemplateState(state.getType(), state.isEnabled()));
				}
				items.add(i);
			}
			return items;
		}
	}
	
	@NoArgsConstructor // Empty constructor for JAXB
	@XmlType(name = "state") @Data  @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateState {
		@XmlAttribute(name="type")
		String type;
		
		@XmlAttribute
		boolean enabled;

		public WebHookTemplateState(String shortName, boolean b) {
			this.type = shortName;
			this.enabled = b;
		}
		
	}
	
}