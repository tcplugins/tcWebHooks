package webhook.teamcity.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @AllArgsConstructor @Builder
public class WebHookAuthenticationParameter {
	
	private String key;
	private boolean required;
	private boolean hidden;
	private String name;
	private String toolTip;

}
