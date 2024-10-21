package webhook.teamcity.executor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.serverSide.SBuild;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import webhook.teamcity.payload.content.ExtraParameters;

/**
 * A listener for callbacks for WebHookBuildStatisticsEvents.
 * When a WebHookBuildStatisticsEvent occurs, this service will handle them.
 */
public interface WebHookBuildStatisticsEventListener {

    public static final String FAILURE_TIMEOUT_SECONDS = "buildStatisticsFailureTimeout";
    public static final String REQUIRED_BUILD_STATISTICS = "requiredBuildStatistics";
    
    public static final String ALL_REQUIRED_STATISTICS_WERE_RECEIVED_REASON =  "allRequiredStatisticsWereReceived";
    public static final String FAILURE_TIMEOUT_EXPIRED_REASON =  "failureTimeoutExpired";
    
    /**
     * Callback that will be called when it is deemed that there is no reason to wait
     * longer to receive more Build Statistics. It is expected that buildStatistics should
     * now be assembled, and the webhook can send as expected.
     * @param event - Details about the reason build statistics collation has finished.
     */
    void buildStatisticsPublished(WebHookBuildStatisticsEvent event);
    
    
    
    @Data @Builder
    public class WebHookBuildStatisticsRequest {
        @Builder.Default private Instant created = Instant.now();
        private Instant updated;
        private ExtraParameters extraParameters;
        private long buildId;
        private String webhookConfigId;
        private List<String> requiredStatistics;
        @Builder.Default private Map<String,BigDecimal> statistics = new HashMap<>();
        private int failureTimeoutSeconds;
        private SBuild sBuild;
        
        // overriding the auto-generated builder method to
        // transparently return our own custom builder class
        public static WebHookBuildStatisticsRequestBuilder builder() {
            return new CustomWebHookBuildStatisticsRequestBuilder();
        }

        // extend the generated builder class to add custom logic
        private static class CustomWebHookBuildStatisticsRequestBuilder extends WebHookBuildStatisticsRequestBuilder {




            @Override
            public WebHookBuildStatisticsRequest build() {
                if (super.extraParameters != null && super.extraParameters.containsKey(FAILURE_TIMEOUT_SECONDS)) {
                    super.failureTimeoutSeconds(Integer.parseInt(super.extraParameters.get(FAILURE_TIMEOUT_SECONDS)));
                }
                if (super.extraParameters != null && super.extraParameters.containsKey(REQUIRED_BUILD_STATISTICS)) {
                    super.requiredStatistics(Arrays.asList(super.extraParameters.get(REQUIRED_BUILD_STATISTICS).split(",")));
                }
                // invoke the actual generated build method for instantiating the class
                return super.build();
            }
        }
        public void addStatistic(String valueTypeKey, BigDecimal value) {
            this.statistics.put(valueTypeKey, value);
            this.updated = Instant.now();
        }
        public boolean totalElapsedTimeExpired(Instant now) {
            return now.isAfter(this.created.plus(this.failureTimeoutSeconds, ChronoUnit.SECONDS));
        }
        public boolean allRequiredStatisticsWereReceived() {
            return sBuild != null && requiredStatistics != null && this.statistics.keySet().containsAll(requiredStatistics);
        }
    }
    
    @Value
    public class WebHookBuildStatisticsEvent {
        private WebHookBuildStatisticsRequest request;
        private String reason;
    }

}
