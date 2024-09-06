package webhook.teamcity.executor;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;

import jetbrains.buildServer.serverSide.SBuild;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsEvent;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsRequest;

public class WebHookBuildStatisticsEventCollatorImpl implements WebHookBuildStatisticsEventCollator {
    
    //private Map<long, >
    private Set<WebHookBuildStatisticsEventListener> buildStatisticsEventListeners = new TreeSet<>();
    

    @Override
    public void registerAsBuildStatisticsEventListener(WebHookBuildStatisticsEventListener buildStatisticsEventListener) {
        this.buildStatisticsEventListeners.add(buildStatisticsEventListener);
    }

    @Override
    public void registerInterestInBuild(WebHookBuildStatisticsRequest webHookBuildStatisticsRequest) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleEvent(SBuild build, String valueTypeKey, BigDecimal value) {
        // TODO Auto-generated method stub
        
    }
    
    private void notifyListenersOfNewEvent(WebHookBuildStatisticsEvent event) {
        this.buildStatisticsEventListeners.forEach(l -> l.buildStatisticsPublished(event));
    }

}
