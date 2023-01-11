package webhook.teamcity.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerSettings;
import lombok.RequiredArgsConstructor;
import webhook.teamcity.WebHookPluginDataResolver;
import webhook.teamcity.payload.WebHookPayloadTemplate;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateManager.TemplateState;
import webhook.teamcity.settings.WebHookConfigEnhanced;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookSearchFilter;
import webhook.teamcity.settings.WebHookSearchResult;
import webhook.teamcity.settings.WebHookSettingsManager;

@RequiredArgsConstructor
public class StatisticsReportAssemblerImpl implements StatisticsReportAssembler {
	
	private final ServerSettings myServerSettings;
	private final SBuildServer mySBuildServer;
	private final WebHookPluginDataResolver myWebHookPluginDataResolver;
	private final WebHookSettingsManager myWebHookSettingsManager;
	
	private final WebHookMainSettings myWebHookMainSettings;
	private final WebHookTemplateManager myWebHookTemplateManager;

	@Override
	public StatisticsReport assembleStatisticsReports(ValueHasher hasher, List<StatisticsEntity> statisticsEntities) {
		StatisticsReport report = new StatisticsReport();
		report.setPluginInfo(assembleWebHooksPluginInfo(hasher));
		report.setInstanceInfo(assembleTeamCityInstanceInfo(hasher));
		report.setConfigStatistics(assembleWebHookConfigurationStatistics(hasher));
		report.setReports(assembleWebHookStatisticsReports(hasher, statisticsEntities));
		return report;
	}

	@Override
	public WebHooksPluginInfo assembleWebHooksPluginInfo(ValueHasher hasher) {
		WebHooksPluginInfo webHooksPluginInfo = new WebHooksPluginInfo();
		webHooksPluginInfo.setTcWehooksVersion(myWebHookPluginDataResolver.getWebHooksCoreVersion());
		webHooksPluginInfo.setTcWebHooksRestApiVersion(myWebHookPluginDataResolver.getWebHooksRestApiVersion());
		return webHooksPluginInfo;
	}

	@Override
	public TeamCityInstanceInfo assembleTeamCityInstanceInfo(ValueHasher hasher) {
		TeamCityInstanceInfo teamCityInstanceInfo = new TeamCityInstanceInfo();
		teamCityInstanceInfo.setTeamcityId(hasher.hash(myServerSettings.getServerUUID(),"$6$AYWFtQpe"));
		teamCityInstanceInfo.setTeamcityVersion(mySBuildServer.getFullServerVersion());
		teamCityInstanceInfo.setTeamcityBuild(mySBuildServer.getBuildNumber());
		teamCityInstanceInfo.setWebHookProxyConfigured(Objects.nonNull(myWebHookMainSettings.getWebHookMainConfig().getProxyAsElement()));
		return teamCityInstanceInfo;
	}

	@Override
	public WebHookConfigurationStatistics assembleWebHookConfigurationStatistics(ValueHasher hasher) {
		
		List<WebHookSearchResult> webHookConfigs = myWebHookSettingsManager.findWebHooks(WebHookSearchFilter.builder().show("all").build());
		WebHookConfigurationStatistics stats = new WebHookConfigurationStatistics();
		stats.configurationCount = webHookConfigs.size();
		for (WebHookSearchResult config : webHookConfigs) {

			WebHookConfigEnhanced webHookConfigEnhanced = config.getWebHookConfigEnhanced();
			if (Boolean.TRUE.equals(webHookConfigEnhanced.getWebHookConfig().getEnabled())) {
				stats.addFeature("enabled", 1);
				stats.addBuildStates(webHookConfigEnhanced.getBuildStates());
				WebHookPayloadTemplate template = myWebHookTemplateManager.getTemplate(webHookConfigEnhanced.getTemplateId());
				if (Objects.nonNull(template)) {
					stats.addTemplateState(myWebHookTemplateManager.getTemplateState(template.getTemplateId(), TemplateState.BEST));
					stats.addTemplateFormat(webHookConfigEnhanced.getPayloadFormat());
					stats.addTemplateId(hasher.hash(webHookConfigEnhanced.getTemplateId()));
				}
				stats.addAuthentication(webHookConfigEnhanced.getWebHookConfig().getAuthenticationConfig());
				
				if (Objects.nonNull(webHookConfigEnhanced.getWebHookConfig().getTriggerFilters())){
					stats.addFeature("triggers", webHookConfigEnhanced.getWebHookConfig().getTriggerFilters().size());
				}
				if (Objects.nonNull(webHookConfigEnhanced.getWebHookConfig().getEnabledTemplatesExcludingDefaults())){
					stats.addFeature("customTemplates", webHookConfigEnhanced.getWebHookConfig().getEnabledTemplatesExcludingDefaults().size());
				}
				if (Objects.nonNull(webHookConfigEnhanced.getWebHookConfig().getParams())){
					stats.addFeature("parameters", webHookConfigEnhanced.getWebHookConfig().getParams().size());
				}
			} else {
				stats.addFeature("disabled", 1);
			}
		}
		return stats;
	}

	@Override
	public List<StatisticsSnapshot> assembleWebHookStatisticsReports(ValueHasher hasher, List<StatisticsEntity> statisticsEntities) {
		List<StatisticsSnapshot> hashedStatistics = new ArrayList<>();
		StatisticsEntityBuilder statisticsEntityBuilder = new StatisticsEntityBuilder().withHasher(hasher);
		for (StatisticsEntity snapshot : statisticsEntities) {
			hashedStatistics.add(statisticsEntityBuilder.copy(snapshot.statisticsSnapshot));
		}
		return hashedStatistics;
	}

}
