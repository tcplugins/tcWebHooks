package webhook.teamcity.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.auth.WebHookAuthConfig;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;

@Getter @Setter
public class WebHookConfigurationStatistics {
	
	Integer configurationCount;
	Map<String,Integer> buildStates = new HashMap<>();
	Map<String,Integer> authTypes = new HashMap<>();
	Map<String,Integer> formats = new HashMap<>();
	Map<String,Integer> features = new HashMap<>(); // Filters, Parameters, customTemplates,
	Map<String,Integer> templateStates = new HashMap<>();
	Map<String,Integer> templateIds = new HashMap<>();
	public void addTemplateState(TemplateState templateState) {
		int count = templateStates.getOrDefault(templateState.toString(), 0);
		templateStates.put(templateState.toString(), ++count);
	}
	public void addTemplateFormat(String payloadFormat) {
		int count = formats.getOrDefault(payloadFormat, 0);
		formats.put(payloadFormat, ++count);
	}
	public void addTemplateId(String templateId) {
		int count = templateIds.getOrDefault(templateId, 0);
		templateIds.put(templateId, ++count);
	} 
	public void addBuildStates(Set<BuildStateEnum> buildStatesList) {
		if (Objects.nonNull(buildStatesList)) {
			for (BuildStateEnum buildState : buildStatesList) {
				int count = buildStates.getOrDefault(buildState.getShortName(), 0);
				buildStates.put(buildState.getShortName(), ++count);
			}
		}
	}
	public void addAuthentication(WebHookAuthConfig authenticationConfig) {
		String authType = "none";
		if (Objects.nonNull(authenticationConfig) && Objects.nonNull(authenticationConfig.getType())) {
			authType = authenticationConfig.getType();
		}
		int count = authTypes.getOrDefault(authType, 0);
		authTypes.put(authType, ++count);
	}
	public void addFeature(String feature, int featureCount) {
		int count = features.getOrDefault(feature, 0);
		features.put(feature, count += featureCount);
	}

}
