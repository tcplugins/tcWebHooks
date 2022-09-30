package webhook.teamcity.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.BuildTypeIdResolver;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.json.WebHookFilterJson.Filter;
import webhook.teamcity.json.WebHookHeaderJson.Header;
import webhook.teamcity.json.WebHookParameterJson.Parameter;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;
import webhook.teamcity.settings.project.WebHookParameterModel;

@Data
public class WebHookConfigurationJson {
	
	private String url;
	private String id;
	private String projectId;
	private Boolean enabled = true;
	private String template;
	private Boolean hideSecureValues = true;
	private String href;
	
	private List<WebHookBuildStateJson> buildState;
	private WebHookBuildTypesJson buildTypes;
	private WebHookAuthenticationJson authentication;
	private WebHookParameterJson parameters;
	private WebHookFilterJson filters;
	private WebHookHeaderJson headers;
	
	public WebHookConfig toWebHookConfig(ProjectIdResolver projectIdResolver, BuildTypeIdResolver buildTypeIdResolver) {
		return WebHookConfig.builder()
				.url(getUrl())
				.uniqueKey(StringUtils.isBlank(id) || "_new".equals(id) ? WebHookConfig.generateRandomKey() : id)
				.enabled(getEnabled())
				.projectInternalId(projectIdResolver.getInternalProjectId(getProjectId()))
				.projectExternalId(getProjectId())
				.hideSecureValues(getHideSecureValues())
				.payloadTemplate(getTemplate())
				.allBuildTypesEnabled(getBuildTypes().isAllEnabled())
				.subProjectsEnabled(getBuildTypes().isSubProjectsEnabled())
				.enabledBuildTypesSet(buildTypeIdResolver.getInternalBuildTypeIds(buildTypes.getId()))
				.states(buildBuildState(getBuildState()))
				.authEnabled(this.authentication != null)
				.authType(getAuthType())
				.authParameters(getAuthParameters())
				.authPreemptive(this.authentication != null && Boolean.TRUE.equals(this.authentication.getPreemptive()))
				.extraParameters(getExtraParameters())
				.filters(this.filters.getFilter().stream()
						.map(f -> WebHookFilterConfig.create(f.getValue(), f.getRegex(), f.getEnabled()))
						.collect(Collectors.toList()))
				.headers(this.headers.getHeader().stream()
						.map(h -> WebHookHeaderConfig.create(h.getName(), h.getValue()))
						.collect(Collectors.toList()))
				.build();
		
	}

	public static WebHookConfigurationJson fromWebHookConfig(WebHookConfig webHookConfig, ProjectIdResolver projectIdResolver, BuildTypeIdResolver buildTypeIdResolver) {
		WebHookConfigurationJson json  = new WebHookConfigurationJson();
		json.setUrl(webHookConfig.getUrl());
		json.setId(webHookConfig.getUniqueKey());
		json.setProjectId(projectIdResolver.getExternalProjectId(webHookConfig.getProjectInternalId()));
		json.setEnabled(webHookConfig.getEnabled());
		json.setHideSecureValues(webHookConfig.isHideSecureValues());
		json.setTemplate(webHookConfig.getPayloadTemplate());
		json.setBuildState(fromBuildState(webHookConfig.getBuildStates()));
		json.setBuildTypes(
				new WebHookBuildTypesJson(
					webHookConfig.isEnabledForAllBuildsInProject(),
					webHookConfig.isEnabledForSubProjects(),
					buildTypeIdResolver.getExternalBuildTypeIds(webHookConfig.getEnabledBuildTypesSet())
				)
			);
		json.setAuthentication(WebHookAuthenticationJson.fromWebHookAuthConfig(webHookConfig.getAuthenticationConfig()));
		json.setParameters(fromExtraParamaters(webHookConfig.getParams()));
		json.setFilters(fromFilters(webHookConfig.getTriggerFilters()));
		json.setHeaders(fromHeaders(webHookConfig.getHeaders()));
		return json;
	}
	
	private static WebHookHeaderJson fromHeaders(List<WebHookHeaderConfig> configHeaders) {
		List<Header> headers = new ArrayList<>();
		int count = 0;
		for (WebHookHeaderConfig webHookHeaderConfig : configHeaders) {
			headers.add(new Header(++count, webHookHeaderConfig.getName(), webHookHeaderConfig.getValue()));
		}
		return WebHookHeaderJson.create(headers);
	}

	private static WebHookFilterJson fromFilters(List<WebHookFilterConfig> triggerFilters) {
		List<Filter> filters = new ArrayList<>();
		int count = 0;
		for (WebHookFilterConfig webHookFilterConfig : triggerFilters) {
			filters.add(new Filter(++count, webHookFilterConfig.getValue(), webHookFilterConfig.getRegex(), webHookFilterConfig.isEnabled()));
		}
		return WebHookFilterJson.create(filters);
	}

	private static WebHookParameterJson fromExtraParamaters(ExtraParameters params) {
		return WebHookParameterJson.create(
				params.getWebHookParameters().getAll()
					.stream().map(p -> Parameter.create(p)).collect(Collectors.toList()));
	}

	private static List<WebHookBuildStateJson> fromBuildState(BuildState buildStates) {
		return buildStates.getStateSet()
					.stream()
					.filter(s -> !s.getShortName().equalsIgnoreCase("buildFinished")) // don't include finished since it's a pseudo state.
					.map(s -> new WebHookBuildStateJson(s, buildStates.enabled(s)))
					.collect(Collectors.toList());
	}

	private BuildState buildBuildState(List<WebHookBuildStateJson> buildStates) {
		BuildState buildStateNew = new BuildState();
		for (WebHookBuildStateJson state : buildStates) {
			if (!state.getType().equals(BuildStateEnum.BUILD_FINISHED)) {
				buildStateNew.setEnabled(state.getType(),state.isEnabled());
				if (BuildStateEnum.isAnEnabledFinishedState(state.getType().getShortName(), state.isEnabled())) {
					buildStateNew.setEnabled(BuildStateEnum.BUILD_FINISHED, true);
				}
			}
		}
		return buildStateNew;
	}

	private ExtraParameters getExtraParameters() {
		return new ExtraParameters(
				this.parameters.getParameter()
				.stream()
				.map(p -> WebHookParameterModel.create(ExtraParameters.WEBHOOK, p))
				.collect(Collectors.toList()));
	}

	private String getAuthType() {
		if (this.authentication != null && this.authentication.getType() != null) {
			return this.authentication.getType();
		}
		return null;
	}

	private Map<String, String> getAuthParameters() {
		if (this.authentication != null && this.authentication.getParameters() != null) {
			return this.authentication.getParameters();
		}
		return null;
	}
}
