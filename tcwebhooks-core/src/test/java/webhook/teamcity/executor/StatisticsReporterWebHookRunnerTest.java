package webhook.teamcity.executor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import jetbrains.buildServer.serverSide.SProject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import webhook.TestingWebHookFactory;
import webhook.WebHook;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.WebHookContentBuilder;
import webhook.teamcity.auth.AuthWithRealServerTestBase;
import webhook.teamcity.history.WebHookHistoryItemFactory;
import webhook.teamcity.history.WebHookHistoryRepository;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.statistics.StatisticsReport;
import webhook.teamcity.statistics.WebHooksPluginInfo;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsReporterWebHookRunnerTest extends AuthWithRealServerTestBase {
    
    @Mock
    WebHookContentBuilder webHookContentBuilder;
    
    @Mock
    WebHookHistoryRepository webHookHistoryRepository;
    
    @Mock
    WebHookHistoryItemFactory webHookHistoryItemFactory;
    
    @Mock
    WebHookConfig whc; 
    BuildStateEnum state = BuildStateEnum.BUILD_SUCCESSFUL; 
    
    boolean overrideIsEnabled = true;
    boolean isTest = false;
    
    @Mock
    SProject rootProject;

    @Test
    public void testGetWebHookContent() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(201));
        Mockito.when(whc.getUrl()).thenReturn("http://" + getHost() + ":" + getPort());
        TestingWebHookFactory factory = new TestingWebHookFactory();
        WebHook webHook = factory.getWebHook(whc.getUrl());
        webHook.setEnabled(true);
        StatisticsReport report = new StatisticsReport();
        WebHooksPluginInfo webHooksPluginInfo = new WebHooksPluginInfo();
        webHooksPluginInfo.setTcWehooksVersion("1.0");
        report.setPluginInfo(webHooksPluginInfo);
        StatisticsReporterWebHookRunner runner = new StatisticsReporterWebHookRunner(webHookContentBuilder, webHookHistoryRepository, webHookHistoryItemFactory, whc, state, overrideIsEnabled, webHook, isTest, rootProject, report);
        runner.run();
        RecordedRequest req1 = server.takeRequest();
        assertEquals("1f85267c9462acee95c4493e6408bf87c42b07e17db42543a857dedf2468f305", req1.getHeader("x-tcwebhooks-hmac"));
    }

}
