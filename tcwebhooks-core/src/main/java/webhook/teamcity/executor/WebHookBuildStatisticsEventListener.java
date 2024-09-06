package webhook.teamcity.executor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.serverSide.SBuild;
import lombok.Data;

/**
 * A listener for callbacks for WebHookBuildStatisticsEvents.
 * When a WebHookBuildStatisticsEvent occurs, this service will handle them.
 */
public interface WebHookBuildStatisticsEventListener {

    void buildStatisticsPublished(WebHookBuildStatisticsEvent event);
    
    
    
    @Data
    public class WebHookBuildStatisticsRequest {
        private long buildId;
        private String webhookConfigId;
        private List<String> requiredStatistics;
        private Map<String,BigDecimal> statistics;
        private int interMessageTimeoutSeconds;
        private int timeToLiveTimeoutSeconds;
        private String reason;
    }
    
    @Data
    public class WebHookBuildStatisticsEvent {
        private WebHookBuildStatisticsRequest request;
        private SBuild build;
        private Map<String,BigDecimal> statistics;
        private String reason;
    }

}
