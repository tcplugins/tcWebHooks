package webhook.teamcity.server.rest.data;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.server.rest.jersey.provider.annotated.JerseyInjectable;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import org.springframework.stereotype.Component;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.statistics.StatisticsManager;
import webhook.teamcity.statistics.StatisticsReportAssembler;

@JerseyInjectable
@Component
public class WebHookStatisticsDataProvider extends DataProvider {

	private final StatisticsReportAssembler myStatisticsReportAssembler;
	private final StatisticsManager myStatisticsManager;
	
	public WebHookStatisticsDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			ProjectManager projectManager,
			ProjectIdResolver projectIdResolver,
			SecurityContext securityContext,
			StatisticsManager statisticsManager,
			StatisticsReportAssembler statisticsReportAssembler) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myStatisticsManager = statisticsManager;
		this.myStatisticsReportAssembler = statisticsReportAssembler;
	}
	
	public StatisticsManager getStatisticsManager() {
		return myStatisticsManager;
	}
	
	public StatisticsReportAssembler getStatisticsReportAssembler() {
		return myStatisticsReportAssembler;
	}
}
