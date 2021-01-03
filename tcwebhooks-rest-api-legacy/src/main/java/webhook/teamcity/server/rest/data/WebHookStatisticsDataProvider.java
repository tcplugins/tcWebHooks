package webhook.teamcity.server.rest.data;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import webhook.teamcity.ProjectIdResolver;
import webhook.teamcity.statistics.StatisticsReportAssembler;

public class WebHookStatisticsDataProvider extends DataProvider {

	private final StatisticsReportAssembler myStatisticsReportAssembler;
	
	public WebHookStatisticsDataProvider(SBuildServer server, 
			RootUrlHolder rootUrlHolder,
			PermissionChecker permissionChecker, 
			ProjectManager projectManager,
			ProjectIdResolver projectIdResolver,
			SecurityContext securityContext,
			StatisticsReportAssembler statisticsReportAssembler) {
		
		super(server, rootUrlHolder, permissionChecker, projectManager,
				projectIdResolver, securityContext);
		
		this.myStatisticsReportAssembler = statisticsReportAssembler;
	}
	
	public StatisticsReportAssembler getStatisticsReportAssembler() {
		return myStatisticsReportAssembler;
	}
}
