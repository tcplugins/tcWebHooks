package webhook.teamcity.settings.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class WebHookParameterModel implements WebHookParameter {
	
	private String id;
	private String context;
	private String name;
	private String value;
	private Boolean secure;
	private Boolean includedInLegacyPayloads;
	private Boolean forceResolveTeamCityVariable;
	private String templateEngine;
	
	public static WebHookParameterModel create(String context, WebHookParameter parameter) {
		return new WebHookParameterModel(
				parameter.getId(), 
				context,
				parameter.getName(),
				parameter.getValue(),
				parameter.getSecure(),
				parameter.getIncludedInLegacyPayloads(),
				parameter.getForceResolveTeamCityVariable(),
				parameter.getTemplateEngine()
			);
	}
}
