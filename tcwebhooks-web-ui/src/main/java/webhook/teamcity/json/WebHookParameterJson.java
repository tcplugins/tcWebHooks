package webhook.teamcity.json;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import webhook.teamcity.settings.project.WebHookParameter;

@Data @AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WebHookParameterJson extends WebHookConfigurationListWrapper {
	private List<Parameter> parameter;

	@Data
	public static class Parameter implements WebHookParameter {
		private String id;
		private String name;
		private String value;
		private Boolean secure;
		private Boolean includedInLegacyPayload;
		private Boolean forceResolveTeamCityVariable;
		private String templateEngine;
		
		@Override
		public Boolean getIncludedInLegacyPayloads() {
			return this.includedInLegacyPayload;
		}
		@Override
		public void setIncludedInLegacyPayloads(Boolean isIncluded) {
			this.includedInLegacyPayload = isIncluded;
		}
		
		public static Parameter create(WebHookParameter webHookParameter) {
			Parameter p = new Parameter();
			p.setId(webHookParameter.getId());
			p.setName(webHookParameter.getName());
			p.setValue(webHookParameter.getValue());
			p.setSecure(webHookParameter.getSecure());
			p.setIncludedInLegacyPayload(webHookParameter.getIncludedInLegacyPayloads());
			p.setForceResolveTeamCityVariable(webHookParameter.getForceResolveTeamCityVariable());
			p.setTemplateEngine(webHookParameter.getTemplateEngine());
			return p;
		}
	}
}
