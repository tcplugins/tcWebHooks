package webhook.teamcity.statistics;

import java.util.List;

import lombok.Data;

@Data
public class StatisticsReport {
	
	WebHooksPluginInfo pluginInfo;
	TeamCityInstanceInfo instanceInfo;
	WebHookConfigurationStatistics configStatistics;
	List<StatisticsSnapshot> reports;
 
}
