package webhook.teamcity.executor;

import java.math.BigDecimal;

import jetbrains.buildServer.serverSide.SBuild;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsRequest;

public class WebHookBuildStatisticsEventCollatorImpl implements WebHookBuildStatisticsEventCollator {
    
    //private Map<long, >
    

    @Override
    public boolean registerAsBuildStatisticsEventListener(WebHookBuildStatisticsEventListener buildStatisticsEventListener) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void registerInterestInBuild(WebHookBuildStatisticsRequest webHookBuildStatisticsRequest) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleEvent(SBuild build, String valueTypeKey, BigDecimal value) {
        // TODO Auto-generated method stub
        
    }

}
