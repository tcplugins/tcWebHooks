package webhook.teamcity.executor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import lombok.Getter;
import lombok.Synchronized;
import webhook.teamcity.DeferrableService;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.Loggers;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsEvent;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsRequest;
import webhook.teamcity.settings.WebHookMainSettings;

public class WebHookBuildStatisticsEventCollatorImpl implements WebHookBuildStatisticsEventCollator, DeferrableService {
    
    private Map<StatisticKey, WebHookBuildStatisticsRequest> statisticsRequests = new HashMap<>(); 
    private List<WebHookBuildStatisticsEventListener> buildStatisticsEventListeners = new ArrayList<>();
    private ScheduledExecutorService myExecutorService;
    private DeferrableServiceManager myDeferrableServiceManager;
    private WebHookMainSettings myWebHookMainSettings;
    
    @Getter
    private boolean serviceEnabled = false;
    
    public WebHookBuildStatisticsEventCollatorImpl(ExecutorServices executorServices, DeferrableServiceManager deferrableServiceManager, WebHookMainSettings configSettings) {
        this.myExecutorService = executorServices.getNormalExecutorService();
        this.myDeferrableServiceManager = deferrableServiceManager;
        this.myWebHookMainSettings = configSettings;
    }

    @Override
    public void registerAsBuildStatisticsEventListener(WebHookBuildStatisticsEventListener buildStatisticsEventListener) {
        Loggers.SERVER.info("WebHookBuildStatisticsEventCollatorImpl :: Listener registered for WebHookBuildStatisticsEvent events: " + buildStatisticsEventListener.getClass().getSimpleName() );
        this.buildStatisticsEventListeners.add(buildStatisticsEventListener);
    }

    @Override
    public void registerInterestInBuild(WebHookBuildStatisticsRequest webHookBuildStatisticsRequest) {
        StatisticKey key = new StatisticKey(webHookBuildStatisticsRequest.getBuildId(), webHookBuildStatisticsRequest.getWebhookConfigId());
        if (statisticsRequests.containsKey(key)) {
            Loggers.SERVER.warn("WebHookBuildStatisticsEventCollatorImpl :: Ignoring request to listen for statistics to same build and webhookId: " + key);
        } else {
            Loggers.SERVER.info(String.format("WebHookBuildStatisticsEventCollatorImpl :: Registering interest in webhook and build: buildId: '%d', webHookConfigId: '%s', requiredStatistics: '%s'", webHookBuildStatisticsRequest.getBuildId(), webHookBuildStatisticsRequest.getWebhookConfigId(), 
                    webHookBuildStatisticsRequest.getRequiredStatistics() != null ? webHookBuildStatisticsRequest.getRequiredStatistics().toString() : ""));
            this.statisticsRequests.put(key, webHookBuildStatisticsRequest);
            Loggers.SERVER.info(String.format("WebHookBuildStatisticsEventCollatorImpl :: statisticsRequests now contains '%d' items", this.statisticsRequests.size()));
        }
    }

    @Override
    public void handleEvent(SBuild build, String valueTypeKey, BigDecimal value) {
        for (StatisticKey key : statisticsRequests.keySet()) {
            if (build.getBuildId() == key.getBuildId()) {
                updateStatistics(key, valueTypeKey, value);
            }
        }
    }
    
    @Override
    public void requestDeferredRegistration() {
        Loggers.SERVER.info("WebHookBuildStatisticsEventCollatorImpl :: Registering as a deferrable service");
        myDeferrableServiceManager.registerService(this);
    }

    @Override
    public void register() {
        if (myWebHookMainSettings.isBuildStatisticsCollatorEnabled()) {
            this.serviceEnabled = true;
            Loggers.SERVER.info(String.format("WebHookBuildStatisticsEventCollatorImpl :: Requesting %d second scheduling of WebHookBuildStatisticsCollatorScheduledTask", this.myWebHookMainSettings.getCheckInterval()));
            this.myExecutorService.scheduleAtFixedRate(new WebHookBuildStatisticsCollatorScheduledTask(this), 10, this.myWebHookMainSettings.getCheckInterval(), TimeUnit.SECONDS);
        } else {
            Loggers.SERVER.info("WebHookBuildStatisticsEventCollatorImpl :: Service is not enabled. WebHookBuildStatisticsCollatorScheduledTask will NOT be scheduled. See https://github.com/tcplugins/tcWebHooks/wiki/Waiting-for-Build-Statistics-to-be-published");
        }
    }
    
    @Override
    public void unregister() {}
    
    @Synchronized
    private void updateStatistics(StatisticKey key, String valueTypeKey, BigDecimal value) {
        WebHookBuildStatisticsRequest req = this.statisticsRequests.get(key);
        if (req != null) {
            req.addStatistic(valueTypeKey, value);
        }
    }
    @Synchronized
    private void updateStatistics(StatisticKey key, SBuild sBuild) {
        WebHookBuildStatisticsRequest req = this.statisticsRequests.get(key);
        if (req != null) {
            req.setSBuild(sBuild);
        }
    }

    @Override
    public void notifyListenersOfNewEvent(WebHookBuildStatisticsEvent event) {
        Loggers.SERVER.info("WebHookBuildStatisticsEventCollatorImpl :: Notifying of new WebHookBuildStatisticsEvent: " + event.toString());
        for (WebHookBuildStatisticsEventListener webHookBuildStatisticsEventListener : buildStatisticsEventListeners) {
            try {
                webHookBuildStatisticsEventListener.buildStatisticsPublished(event);
                Loggers.SERVER.debug(String.format("WebHookBuildStatisticsEventCollatorImpl :: Successfully sent WebHookBuildStatisticsEvent to listener '%s'. %s", webHookBuildStatisticsEventListener.getClass().getSimpleName(), event.toString()));
            }
            catch (Exception e) {
                Loggers.SERVER.warn(String.format("WebHookBuildStatisticsEventCollatorImpl :: Exception occurred trying to notify of WebHookBuildStatisticsEvent to listener '%s'", webHookBuildStatisticsEventListener.getClass().getSimpleName()), e);
            }
        }
    }
    
    @Override
    public boolean hasEvents() {
        return !this.statisticsRequests.isEmpty();
    }

    @Override
    public Set<Entry<StatisticKey, WebHookBuildStatisticsRequest>> getEvents() {
        return this.statisticsRequests.entrySet();
    }
    
    @Override
    public boolean isInterestedInBuild(String webHookConfigId, long buildId) {
        return this.statisticsRequests.containsKey(new StatisticKey(buildId, webHookConfigId));
    }


    @Override
    public void remove(WebHookBuildStatisticsEvent event) {
        Loggers.SERVER.debug("WebHookBuildStatisticsCollatorScheduledTask :: Removing statistics request: " + event );
        this.statisticsRequests.remove(new StatisticKey(event.getRequest().getBuildId(), event.getRequest().getWebhookConfigId()));
    }
    

    @Override
    public void setSBuild(SBuild sBuild) {
        for (StatisticKey key : statisticsRequests.keySet()) {
            if (sBuild.getBuildId() == key.getBuildId()) {
                updateStatistics(key, sBuild);
            }
        }
    }
    
    public class WebHookBuildStatisticsCollatorScheduledTask implements Runnable {
        
        private WebHookBuildStatisticsEventCollator webHookBuildStatisticsEventCollator;


        public WebHookBuildStatisticsCollatorScheduledTask(WebHookBuildStatisticsEventCollator collator) {
            this.webHookBuildStatisticsEventCollator = collator;
        }
        

        @Override
        public void run() {
            Loggers.SERVER.debug("WebHookBuildStatisticsCollatorScheduledTask starting");
            if (webHookBuildStatisticsEventCollator.hasEvents()) {
                Instant now = Instant.now();
                List<WebHookBuildStatisticsEvent> actionableEvents = new ArrayList<>();
                for (Entry<StatisticKey, WebHookBuildStatisticsRequest> r : webHookBuildStatisticsEventCollator.getEvents()) {
                    if (r.getValue().allRequiredStatisticsWereReceived()) {
                        actionableEvents.add(new WebHookBuildStatisticsEvent(r.getValue(), WebHookBuildStatisticsEventListener.ALL_REQUIRED_STATISTICS_WERE_RECEIVED_REASON));
                    } else if (r.getValue().totalElapsedTimeExpired(now)) {
                        actionableEvents.add(new WebHookBuildStatisticsEvent(r.getValue(), WebHookBuildStatisticsEventListener.FAILURE_TIMEOUT_EXPIRED_REASON));
                    }
                }
                actionableEvents.forEach(ev -> {
                    this.webHookBuildStatisticsEventCollator.notifyListenersOfNewEvent(ev);
                    this.webHookBuildStatisticsEventCollator.remove(ev);
                });
                Loggers.SERVER.debug(String.format("WebHookBuildStatisticsCollatorScheduledTask :: statisticsRequests contains '%d' items", webHookBuildStatisticsEventCollator.getEvents().size()));
            } else {
                Loggers.SERVER.debug("WebHookBuildStatisticsCollatorScheduledTask :: statisticsRequests is empty");
            }
            Loggers.SERVER.debug("WebHookBuildStatisticsCollatorScheduledTask finished");
        }
    }

}
