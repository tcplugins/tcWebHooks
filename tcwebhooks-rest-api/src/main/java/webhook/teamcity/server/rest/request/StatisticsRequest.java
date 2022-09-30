package webhook.teamcity.server.rest.request;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;

import jetbrains.buildServer.ServiceLocator;
import jetbrains.buildServer.server.rest.data.PermissionChecker;
import webhook.teamcity.server.rest.data.WebHookStatisticsDataProvider;
import webhook.teamcity.server.rest.util.BeanContext;
import webhook.teamcity.statistics.CryptValueHasher;
import webhook.teamcity.statistics.StatisticsEntity;
import webhook.teamcity.statistics.StatisticsReport;

@Path(StatisticsRequest.API_STATISTICS_URL)
public class StatisticsRequest {

	@Context
	@NotNull
	private WebHookStatisticsDataProvider myDataProvider;

	@Context
	@NotNull
	private ServiceLocator myServiceLocator;
	
	@Context
	@NotNull
	private BeanContext myBeanContext;
	
	@Context
	@NotNull
	private PermissionChecker myPermissionChecker;

	public static final String API_STATISTICS_URL = Constants.API_URL + "/statistics";
	
	@GET
	@Produces({ "application/json"})
	public StatisticsReport serveStatistics(@QueryParam("fields") String fields) {
		List<StatisticsEntity> statisticsEntities = this.myDataProvider.getStatisticsManager().getUnreportedHistoricalStatisticsEntities(LocalDate.now().minusDays(7), LocalDate.now());
		return this.myDataProvider.getStatisticsReportAssembler().assembleStatisticsReports(new CryptValueHasher(), statisticsEntities);
	}
	
}
