package webhook.teamcity.server.rest.request;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

import webhook.teamcity.statistics.StatisticsReport;

public class StatisticsRequestReadOnlyTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testStatisticsEndpointRespondsJson() {
        Response response = target(StatisticsRequest.API_STATISTICS_URL)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);
        String body = response.readEntity(String.class);
        assertNotNull(body);
        assertTrue(!body.trim().isEmpty());
    }

    @Test
    public void testStatisticsEndpointTypedResponse() {
        Response response = target(StatisticsRequest.API_STATISTICS_URL)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        assertHttpStatus(Status.OK, response);

        StatisticsReport report = response.readEntity(StatisticsReport.class);
        assertNotNull(report);
    }
}

