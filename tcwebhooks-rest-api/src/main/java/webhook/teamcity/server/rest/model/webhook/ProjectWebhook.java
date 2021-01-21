package webhook.teamcity.server.rest.model.webhook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.server.rest.WebHookWebLinks;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.WebHookConfig;

/*
	<webhook url="http://localhost/test" enabled="true" format="nvpairs">
	<states>
	  <state type="buildStarted" enabled="true" />
	  <state type="beforeBuildFinish" enabled="true" />
	  <state type="buildFinished" enabled="true" />
	  <state type="buildBroken" enabled="false" />
	  <state type="buildInterrupted" enabled="true" />
	  <state type="buildSuccessful" enabled="true" />
	  <state type="buildFixed" enabled="false" />
	  <state type="buildFailed" enabled="true" />
	  <state type="responsibilityChanged" enabled="true" />
	  <state type="buildTagged" enabled="false" />
	</states>
	 <parameters>
	  <param name="color" value="red" />
	  <param name="notify" value="1" />
	</parameters>
	<custom-templates>
		<custom-template type="buildStatusHtml" template="${branchDisplayName} ${projectId}" enabled="true"/>
	</custom-templates>
	</webhook>
*/
@XmlRootElement(name = "webhook")
@NoArgsConstructor
@Getter @Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType (propOrder = { "url", "id", "projectId", "enabled", "template", "webUrl", "href", "states", "buildTypes", "parameters", "customTemplates", "authentication" })
public class ProjectWebhook {
	
	@XmlAttribute
	private String url;
	
	@XmlAttribute
	private String id;
	
	@XmlAttribute
	public Boolean enabled;
	
	@XmlAttribute
	public String projectId;
	
	@XmlAttribute
	private String template;
	
	@XmlElement
	public List<ProjectWebhookState> states;

	@XmlElement(name="buildTypes")
	private ProjectWebHookBuildType buildTypes;

	@XmlElement(name = "parameters")
	private List<ProjectWebhookParameter> parameters;
	
	@XmlElement(name = "customTemplates")
	private List<CustomTemplate> customTemplates;
	
	@XmlElement(name = "authentication")
	private ProjectWebHookAuthConfig authentication;
	
	@XmlAttribute
	public String href;
	
	@XmlAttribute
	public  String webUrl;

	public ProjectWebhook(WebHookConfig config, final String projectExternalId, final @NotNull Fields fields, @NotNull final BeanContext beanContext, Collection<String> enabledBuildTypes) {
		
		this.url = ValueWithDefault.decideDefault(fields.isIncluded("url", true, true), config.getUrl()); 
		this.id = ValueWithDefault.decideDefault(fields.isIncluded("id", true, true), config.getUniqueKey());
		this.enabled = ValueWithDefault.decideDefault(fields.isIncluded("enabled", true, true), config.getEnabled());
		this.projectId = ValueWithDefault.decideDefault(fields.isIncluded("projectId", false, true), projectExternalId);
		this.template = ValueWithDefault.decideDefault(fields.isIncluded("template", true, true), config.getPayloadTemplate());
		webUrl = ValueWithDefault.decideDefault(fields.isIncluded("webUrl", false, false), beanContext.getSingletonService(WebHookWebLinks.class).getWebHookUrl(projectExternalId));
		href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getHref(projectExternalId, config));
		buildTypes = ValueWithDefault.decideDefault(fields.isIncluded("buildTypes", true, true), new ProjectWebHookBuildType(config.isEnabledForAllBuildsInProject(), config.isEnabledForSubProjects(), enabledBuildTypes));

		if (fields.isIncluded("states", false, true)) {
			states = new ArrayList<>();
			for (BuildStateEnum state : config.getBuildStates().getStateSet()) {
				if (config.getBuildStates().enabled(state)) {
					ProjectWebhookState webhookState = new ProjectWebhookState();
					webhookState.enabled = true;
					webhookState.type=state.getShortName();
					states.add(webhookState);
				}
			}
		}
		
		if (config.getAuthenticationConfig() != null && ( fields.isIncluded("authentication", false, true) || fields.isAllNested() ) ) {
			authentication = new ProjectWebHookAuthConfig(config.getAuthenticationConfig());
		}
	}

}
