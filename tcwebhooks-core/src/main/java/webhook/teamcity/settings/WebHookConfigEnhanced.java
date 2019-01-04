package webhook.teamcity.settings;

import java.util.Set;

import lombok.Getter;
import webhook.teamcity.BuildStateEnum;

@Getter
public class WebHookConfigEnhanced {
	private WebHookConfig webHookConfig;
	private Set<String> tags;
	private String templateId;
	private String payloadFormat;
	private String projectExternalId;
	private Set<String> buildTypeExternalIds;
	private Set<BuildStateEnum> buildStates;
	
}
