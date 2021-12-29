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
}
