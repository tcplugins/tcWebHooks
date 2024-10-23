package webhook.teamcity.executor;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import webhook.teamcity.DeferrableServiceManager;
import webhook.teamcity.executor.WebHookBuildStatisticsEventListener.WebHookBuildStatisticsRequest;
import webhook.teamcity.payload.content.ExtraParameters;
import webhook.teamcity.settings.WebHookMainSettings;

@RunWith(MockitoJUnitRunner.class)
public class WebHookBuildStatisticsEventCollatorImplTest {
    
    @Mock
    DeferrableServiceManager deferrableServiceManager;
    
    @Mock
    ExecutorServices executorServices;
    
    @Spy
    WebHookMainSettings mainSettings = new WebHookMainSettings(null);
    
    @Mock
    SBuild mockBuild;
    String reason;
    
    @Before
    public void setup() {
        when(executorServices.getNormalExecutorService()).thenReturn(Executors.newSingleThreadScheduledExecutor());
        when(mockBuild.getBuildId()).thenReturn(1L);
        when(mainSettings.isBuildStatisticsCollatorEnabled()).thenReturn(Boolean.TRUE);
        when(mainSettings.getCheckInterval()).thenReturn(1);
    }
    
    @Test
    public void testRegisterInterestInBuildWithNoOtherSettingsWillFinishWhenTimeToLiveExpired() {
        AtomicInteger atomic = new AtomicInteger(0); // Create a counter, so we can wait for it in the test.
        reason = null;
        ExtraParameters extraParameters = new ExtraParameters();
        extraParameters.put("webhook", WebHookBuildStatisticsEventListener.FAILURE_TIMEOUT_SECONDS, "2");
        extraParameters.put("webhook", WebHookBuildStatisticsEventListener.BUILD_COMPLETED_TIMEOUT_SECONDS, "200");
        WebHookBuildStatisticsEventCollatorImpl collator = new WebHookBuildStatisticsEventCollatorImpl(executorServices, deferrableServiceManager, mainSettings);
        collator.registerAsBuildStatisticsEventListener(new WebHookBuildStatisticsEventListener() {
            
            @Override
            public void buildStatisticsPublished(WebHookBuildStatisticsEvent event) {
                reason = event.getReason();
                atomic.incrementAndGet();
            }
        });
        collator.register();
        WebHookBuildStatisticsRequest request = WebHookBuildStatisticsRequest.builder()
                .buildId(mockBuild.getBuildId())
                .webhookConfigId("Webhook_id_1")
                .extraParameters(extraParameters)
                .build();
        collator.registerInterestInBuild(request);
        collator.setSBuild(mockBuild);

        Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAtomic(atomic, equalTo(1));
        assertEquals(WebHookBuildStatisticsEventListener.FAILURE_TIMEOUT_EXPIRED_REASON, reason);
    }
    
    @Test
    public void testRegisterInterestInBuildWithExpectedEventsWillFinishWithAllRequiredStatisticsWereReceived() throws InterruptedException {
        AtomicInteger atomic = new AtomicInteger(0); // Create a counter, so we can wait for it in the test.
        reason = null;
        ExtraParameters extraParameters = new ExtraParameters();
        extraParameters.put("webhook", WebHookBuildStatisticsEventListener.FAILURE_TIMEOUT_SECONDS, "20");
        extraParameters.put("webhook", WebHookBuildStatisticsEventListener.REQUIRED_BUILD_STATISTICS, "fooBar01,fooBar02");
        
        WebHookBuildStatisticsEventCollatorImpl collator = new WebHookBuildStatisticsEventCollatorImpl(executorServices, deferrableServiceManager, mainSettings);
        collator.registerAsBuildStatisticsEventListener(new WebHookBuildStatisticsEventListener() {
            
            @Override
            public void buildStatisticsPublished(WebHookBuildStatisticsEvent event) {
                reason = event.getReason();
                atomic.incrementAndGet();
            }
        });
        
        // Start the thread (via deferred service start)
        collator.register();
        
        // register that this webhook cares about statistics. 
        WebHookBuildStatisticsRequest request = WebHookBuildStatisticsRequest.builder()
                .buildId(mockBuild.getBuildId())
                .webhookConfigId("Webhook_id_1")
                .extraParameters(extraParameters)
                .build();
        collator.registerInterestInBuild(request);
        
        // Now post some statistics, but not fooBar02, which is an expected value .
        collator.handleEvent(mockBuild, "fooBar01", BigDecimal.valueOf(1000.001));
        Thread.sleep(500);
        collator.handleEvent(mockBuild, "fooBar03", BigDecimal.valueOf(1000.003));
        Thread.sleep(500);
        collator.handleEvent(mockBuild, "fooBar04", BigDecimal.valueOf(1000.004));
        Thread.sleep(500);
        collator.handleEvent(mockBuild, "fooBar02", BigDecimal.valueOf(1000.002));
        
        collator.setSBuild(mockBuild);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAtomic(atomic, equalTo(1));
        assertEquals(WebHookBuildStatisticsEventListener.ALL_REQUIRED_STATISTICS_WERE_RECEIVED_REASON, reason);
    }
    
    @Test
    public void testBuilderFailureTimeoutValueIsOverridenByValueFromParameters() {
        ExtraParameters extraParameters = new ExtraParameters();
        extraParameters.put("webhook", WebHookBuildStatisticsEventListener.FAILURE_TIMEOUT_SECONDS, "20");
        extraParameters.put("webhook", WebHookBuildStatisticsEventListener.REQUIRED_BUILD_STATISTICS, "fooBar01,fooBar02");
        
        WebHookBuildStatisticsRequest request = WebHookBuildStatisticsRequest.builder()
                .buildId(mockBuild.getBuildId())
                .webhookConfigId("Webhook_id_1")
                .failureTimeoutSeconds(1000)
                .extraParameters(extraParameters)
                .build();
        assertEquals(20, request.getFailureTimeoutSeconds());
    }

}
