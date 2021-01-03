package webhook.teamcity.statistics;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeamCityInstanceInfo {

	String teamcityVersion;
	String teamcityBuild;
	String teamcityId;
	boolean webHookProxyConfigured;
}
