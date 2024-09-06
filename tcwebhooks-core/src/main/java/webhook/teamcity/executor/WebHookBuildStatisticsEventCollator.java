package webhook.teamcity.executor;

import java.math.BigDecimal;

import jetbrains.buildServer.serverSide.SBuild;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsRequest;

/**
 * This is an in-memory storage area in which we can keep a track of Build Statistic Events.
 * When a build statistics event is sent to this service, it is expected to call  ( 
 * 
 *   If a webhook is configured with "buildStatisticsPublished"
 */
public interface WebHookBuildStatisticsEventCollator {
    
    /**
     * Register as a {@link WebHookBuildStatisticsEventListener}. 
     * When a "buildStatisticsPublished" occurs, this lister will have {@link WebHookBuildStatisticsEventListener#buildStatisticsPublished(WebHookBuildStatisticsEvent)} called.
     * @param buildStatisticsEventListener
     * @return
     */
    boolean registerAsBuildStatisticsEventListener(WebHookBuildStatisticsEventListener buildStatisticsEventListener); 
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

}
