package webhook.teamcity.server.rest.model.template;


import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jetbrains.annotations.Nullable;

/*

	<webhook-template enabled="true" name="testXMLtemplate" rank="10">
		<default-template>{ "defaultBuildStatus" : "${buildStatus}" }</default-template>
		<default-branch-template>{ "defaultBuildStatus" : "${buildStatus}" }</default-branch-template>
		<template-description>"Test XML Template"</template-description>
		<template-tool-tip value="This is some tooltip text for the Test XML Template"/>
		<formats>
			<format name="json" enabled="true" />
			<format name="nvpairs" enabled="true" />
			<format name="tailoredjson" enabled="true" />
		</formats>
		<templates>
			<template>
				<template-text>{ "buildStatus" : "${buildStatus}" }</template-text>
				<branch-template-text>{ "buildStatus" : "${buildStatus} - ${branchName}" }</branch-template-text>
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


 */


@SuppressWarnings("PublicField")
@XmlRootElement(name = "newTemplateDescription")
public class NewTemplateDescription {

	public NewTemplateDescription() {
	}

	public NewTemplateDescription(final String name, 
			@Nullable final List<Format> formats) {
		this.name = name;
		this.formats = formats;
	}

	@XmlAttribute
	public String name;


	@XmlElement(name = "formats")
	private List<Format> formats;

}