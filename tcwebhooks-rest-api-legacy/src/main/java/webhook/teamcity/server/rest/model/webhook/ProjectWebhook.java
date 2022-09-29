package webhook.teamcity.server.rest.model.webhook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.server.rest.model.Fields;
import jetbrains.buildServer.server.rest.util.ValueWithDefault;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.server.rest.WebHookWebLinks;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.settings.CustomMessageTemplate;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookConfig.WebHookConfigBuilder;
import webhook.teamcity.settings.project.WebHookParameter;

/*
	<webhook url="http://localhost/test" enabled="true" format="nvpairs" hide-secure-values="true">
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
@XmlType (propOrder = { "url", "id", "projectId", "enabled", "template", "hideSecureValues", "webUrl", "href", "buildStates", "buildTypes", "parameters", "filters", "headers", "customTemplates", "authentication" })
public class ProjectWebhook {
	
	@XmlElement
	private String url;
	
	@XmlAttribute
	private String id;
	
	@XmlAttribute
	public Boolean enabled;
	
	@XmlAttribute
	public String projectId;
	
	@XmlAttribute
	private String template;
	
	@XmlAttribute
	private Boolean hideSecureValues;
	
	@XmlElement(name = "buildState")	@XmlElementWrapper(name = "buildStates")
	public List<ProjectWebhookState> buildStates;

	@XmlElement(name="buildTypes")
	private ProjectWebHookBuildType buildTypes;

	@XmlElement(name = "parameters")
	private ProjectWebHookParameters parameters;
	
	@XmlElement(name = "filters")
	private ProjectWebHookFilters filters;
	
	@XmlElement(name = "headers")
	private ProjectWebHookHeaders headers;
	
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
		this.hideSecureValues = ValueWithDefault.decideDefault(fields.isIncluded("hideSecureValues", true, true), config.isHideSecureValues());
		this.webUrl = ValueWithDefault.decideDefault(fields.isIncluded("webUrl", false, false), beanContext.getSingletonService(WebHookWebLinks.class).getWebHookUrl(projectExternalId));
		this.href = ValueWithDefault.decideDefault(fields.isIncluded("href"), beanContext.getApiUrlBuilder().getHref(projectExternalId, config));
		this.buildTypes = ValueWithDefault.decideDefault(fields.isIncluded("buildTypes", true, true), new ProjectWebHookBuildType(config.isEnabledForAllBuildsInProject(), config.isEnabledForSubProjects(), enabledBuildTypes));

		if (Boolean.TRUE.equals(fields.isIncluded("states", false, true))) {
			buildStates = new ArrayList<>();
			for (BuildStateEnum state : config.getBuildStates().getStateSet()) {
				// Only add enabled states. Skip FINISHED as it's not a configurable state.
				if (!BuildStateEnum.BUILD_FINISHED.equals(state) && config.getBuildStates().enabled(state)) {
					ProjectWebhookState webhookState = new ProjectWebhookState();
					webhookState.enabled = true;
					webhookState.type=state.getShortName();
					buildStates.add(webhookState);
				}
			}
		}
		
		if (config.getAuthenticationConfig() != null && ( fields.isIncluded("authentication", false, true) || fields.isAllNested() ) ) {
			authentication = new ProjectWebHookAuthConfig(config.getAuthenticationConfig());
		}
		if (config.getParams() != null && ( fields.isIncluded("parameters", false, true) || fields.isAllNested() ) ) {
			parameters = new ProjectWebHookParameters(config, config.getParams().getWebHookParameters().getAll(),
					projectExternalId, null, fields, beanContext);
		}
		if (config.getTriggerFilters() != null && ( fields.isIncluded("filters", false, true) || fields.isAllNested() ) ) {
			filters = new ProjectWebHookFilters(config, config.getTriggerFilters(), 
					projectExternalId, null, fields, beanContext);
		}
		if (config.getHeaders() != null && ( fields.isIncluded("headers", false, true) || fields.isAllNested() ) ) {
			headers = new ProjectWebHookHeaders(config, config.getHeaders(), 
					projectExternalId, null, fields, beanContext);
		}
	}

	public WebHookConfig toWebHookConfig(ProjectIdResolver projectIdResolver, BuildTypeIdResolver buildTypeIdResolver) {
		List<WebHookParameter> params = null;
		if (parameters != null && parameters.getParameters() != null) {
			params = new ArrayList<>();
			params.addAll(parameters.getParameters());
		}
		WebHookConfigBuilder builder = WebHookConfig.builder();
		
		builder.allBuildTypesEnabled(Boolean.TRUE.equals(buildTypes.getAllEnabled()));
		
		if (this.authentication != null) {
			builder.authEnabled(true)
				.authParameters(this.authentication.getParameters())
				.authPreemptive(this.authentication.getPreemptive())
				.authType(this.authentication.getType());
		}
		builder.enabled(getEnabled());
		builder.hideSecureValues(this.hideSecureValues);
			
		builder.enabledBuildTypesSet(buildTypeIdResolver.getInternalBuildTypeIds(this.buildTypes.getEnabledBuildTypes()));
		
		if (params != null) {
			builder.extraParameters(new ExtraParameters().putAll(ExtraParameters.WEBHOOK, params));
		}
		
		if (filters != null) {
			builder.filters(filters.getFilterConfigs());
		}
		
		if (headers != null) {
			builder.headers(headers.getHeaderConfigs());
		}
		
		builder.payloadTemplate(template);
		builder.projectExternalId(this.projectId);
		builder.projectInternalId(projectIdResolver.getInternalProjectId(this.projectId));
		builder.states(toBuildState(buildStates));
		
		if (buildTypes != null) {
			builder.subProjectsEnabled(Boolean.TRUE.equals(buildTypes.getSubProjectsEnabled()));
		}
		
		if (customTemplates != null) {
			builder.templates(toCustomTemplates(customTemplates));
		}
		
		builder.uniqueKey(StringUtils.isBlank(id) || "_new".equals(id) ? WebHookConfig.getRandomKey() : id); // If empty or "_new", set the key to a generated one
		builder.url(url);
		return builder.build();
	}
	

	private SortedMap<String, CustomMessageTemplate> toCustomTemplates(List<CustomTemplate> customTemplates) {
		if (Objects.isNull(customTemplates)) {
			return null;
		}
		SortedMap<String, CustomMessageTemplate> customTemplatesMap = new TreeMap<>();
		for (CustomTemplate customTemplate : customTemplates) {
			customTemplatesMap.put(customTemplate.getType(), CustomMessageTemplate.create(customTemplate.getType(), customTemplate.getTemplate(), customTemplate.getEnabled()));
		}
		return customTemplatesMap;
	}

	private BuildState toBuildState(List<ProjectWebhookState> states) {
		BuildState buildState = new BuildState();
		for (ProjectWebhookState state : states) {
			if (Objects.nonNull(BuildStateEnum.findBuildState(state.type))) {
				buildState.setEnabled(BuildStateEnum.findBuildState(state.type), state.enabled);
				if (BuildStateEnum.isAnEnabledFinishedState(state.type, state.enabled)) {
					buildState.setEnabled(BuildStateEnum.BUILD_FINISHED, true);
				}
			}
		}
		return buildState;
	}

}
