package webhook.teamcity.extension.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class StateBean{
	private String buildStateName;
	private boolean enabled;
}