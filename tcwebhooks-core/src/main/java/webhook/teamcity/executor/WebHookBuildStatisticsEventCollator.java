package webhook.teamcity.executor;

import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.Set;

import jetbrains.buildServer.serverSide.SBuild;
import lombok.Value;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsEvent;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsRequest;

/**
 * This is an in-memory storage area in which we can keep a track of Build Statistic Events.
 * When a build statistics event is sent to this service, it will be checked to see if a webhook is interested
 * in statistics for that build. Once collation is completed, a BuildFinished event will be emitted.
 * <p>
 * If a webhook is configured with a WebHook Parameter named "waitForBuildStatistics", then 
 * this collator tracks statistics events, otherwise these buildStatistics events are ignored.
 */
public interface WebHookBuildStatisticsEventCollator {
    
    /**
     * Register as a {@link WebHookBuildStatisticsEventListener}. 
     * When a "buildStatisticsPublished" occurs, this lister will have {@link WebHookBuildStatisticsEventListener#buildStatisticsPublished(WebHookBuildStatisticsEvent)} called.
     * @param buildStatisticsEventListener
     * @return
     */
    void registerAsBuildStatisticsEventListener(WebHookBuildStatisticsEventListener buildStatisticsEventListener); 
    /**
     * Tell the {@link WebHookBuildStatisticEventCollator} that future events for this build are of interest to a webhook.
     * @param webHookBuildStatisticsRequest
     */
    void registerInterestInBuild(WebHookBuildStatisticsRequest webHookBuildStatisticsRequest);
    
    /**
     * Notify the {@link WebHookBuildStatisticEventCollator} that a statistics event for this build has occurred. 
     * @param build
     * @param valueTypeKey
     * @param value
     */
    void handleEvent(SBuild build, String valueTypeKey, BigDecimal value);
    boolean hasEvents();
    Set<Entry<StatisticKey, WebHookBuildStatisticsRequest>> getEvents();
    
    @Value
    public static class StatisticKey {
        private long buildId;
        private String webhookConfigId;
    }

    void notifyListenersOfNewEvent(WebHookBuildStatisticsEvent event);
    void remove(WebHookBuildStatisticsEvent event);
    boolean isInterestedInBuild(String webHookConfigId, long buildId);
    void setSBuild(SBuild sBuild);
    boolean isServiceEnabled();
    void removeAllForBuild(long buildId);

}
