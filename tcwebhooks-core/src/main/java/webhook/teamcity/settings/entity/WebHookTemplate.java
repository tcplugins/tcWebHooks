package webhook.teamcity.settings.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

import org.jetbrains.annotations.NotNull;

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
public class WebHookTemplate {
	@NotNull @XmlAttribute(name="name")
	String name;
	
	@XmlAttribute
	boolean enabled = true;
	
	@XmlAttribute
	int rank = 10;
	
	@XmlElement(name="default-template")
	String defaultTemplate;
	
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
	private List<WebHookTemplateFormat> formats = new ArrayList<>();
	
	@XmlElement(name="template") @XmlElementWrapper(name="templates")
	private List<WebHookTemplateItem> templates = new ArrayList<>();
	
	WebHookTemplate() {
		// empty constructor for JAXB
	}

	public WebHookTemplate(String name, boolean enabled) {
		this.name = name;
		this.enabled = enabled;
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
	
	@XmlType(name = "template") @Data  @XmlAccessorType(XmlAccessType.FIELD)
	public static class WebHookTemplateItem {
		@NotNull @XmlElement(name="template-text")
		String templateText;
		
		@NotNull @XmlElement(name="branch-template-text")
		String branchTemplateText;
		
		@XmlAttribute
		boolean enabled = true;
		
		@XmlElement(name="state") @XmlElementWrapper(name="states")
		private List<WebHookTemplateState> states = new ArrayList<>();
		
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