package webhook.teamcity.settings.entity;

import java.util.ArrayList;
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

import lombok.Data;

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
			<templates>
				<template>
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

@SuppressWarnings("UnusedDeclaration")
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
	String defaultBranchTemplate;
	
	@NotNull 
	@XmlElement (name="template-description")
	String templateDescription;
	
	@XmlElement (name="template-tool-tip")
	String templateToolTip;
	
	@XmlElement (name="preferred-date-format")
	String preferredDateTimeFormat = "";
	
	@XmlElement(name="format") @XmlElementWrapper(name="formats")
	private List<WebHookTemplateFormat> formats = new ArrayList<WebHookTemplateFormat>();
	
//	@XmlElement(name="template") @XmlElementWrapper(name="templates")
//	private List<WebHookTemplateItem> templates = new ArrayList<WebHookTemplateItem>();
	@XmlElement(name="templates")
	WebHookTemplateItems templates;
	
	WebHookTemplateEntity() {
		// empty constructor for JAXB
	}

	public WebHookTemplateEntity(String name, boolean enabled) {
		this.name = name;
		this.enabled = enabled;
	}
	
	@XmlType(name="templates") @Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateItems {

		@XmlAttribute(name="max-id")
	    Integer maxId = 0;
		
		@XmlElements(@XmlElement(name="template", type=WebHookTemplateItem.class))
	    List<WebHookTemplateItem> templates = new ArrayList<WebHookTemplateItem>();


/*	    public WebHookTemplateItems(Integer maxId, List<WebHookTemplateItem> listOfTemplates) {
	        this();
	        this.maxId = maxId;
	        this.templates = listOfTemplates;  
	    }*/

	    /**
	     * 
	     */
	    public WebHookTemplateItems() {
	        // TODO Auto-generated constructor stub
	    }
	}
	
	@XmlType(name = "format") @Data  @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateFormat {
		@XmlAttribute
		String name;
		
		@XmlAttribute
		boolean enabled = true;
		
		WebHookTemplateFormat() {
			// empty constructor for JAXB
		}
	}
	
	@XmlType @Data @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateText {
		@XmlAttribute (name="use-for-branch-template")
		boolean useTemplateTextForBranch = false;
		
		@NotNull @XmlValue
		String templateContent;
		
		WebHookTemplateText() {
			// empty constructor for JAXB
		}
	}
	
	
	@XmlType(name = "template") @Data  @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateItem {
		@NotNull @XmlElement(name="template-text")
		WebHookTemplateText templateText;
		
		@XmlElement(name="branch-template-text")
		String branchTemplateText;
		
		@XmlAttribute
		boolean enabled = true;
		
		@XmlAttribute @Nullable
		Integer id;
		
		@XmlElement(name="state") @XmlElementWrapper(name="states")
		private List<WebHookTemplateState> states = new ArrayList<WebHookTemplateState>();
		
		WebHookTemplateItem() {
			// empty constructor for JAXB
		}
	}
	
	@XmlType(name = "state") @Data  @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateState {
		@XmlAttribute(name="type")
		String type;
		
		@XmlAttribute
		boolean enabled;
		
		WebHookTemplateState() {
			// empty constructor for JAXB
		}
	}
	
	
}