package webhook.teamcity.server.rest.request;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;

import webhook.teamcity.server.rest.data.WebHookStatisticsDataProvider;
import webhook.teamcity.statistics.CryptValueHasher;
import webhook.teamcity.statistics.StatisticsEntity;
import webhook.teamcity.statistics.StatisticsReport;

@Path(StatisticsRequest.API_STATISTICS_URL)
public class StatisticsRequest {

	@Context
	@NotNull
	private WebHookStatisticsDataProvider myDataProvider;

	public static final String API_STATISTICS_URL = Constants.API_URL + "/statistics";
	
	@GET
	@Produces({ "application/json"})
	public StatisticsReport serveStatistics(@QueryParam("fields") String fields) {
		List<StatisticsEntity> statisticsEntities = this.myDataProvider.getStatisticsManager().getHistoricalStatistics(LocalDate.now().minusDays(7), LocalDate.now());
		return this.myDataProvider.getStatisticsReportAssembler().assembleStatisticsReports(new CryptValueHasher(), statisticsEntities);
	}
	
}
