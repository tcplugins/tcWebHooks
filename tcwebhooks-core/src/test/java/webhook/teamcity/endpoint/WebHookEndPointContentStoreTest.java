package webhook.teamcity.endpoint;

import jetbrains.buildServer.serverSide.SBuildServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import webhook.teamcity.payload.WebHookPayloadManager;

import java.util.Date;

public class WebHookEndPointContentStoreTest {
    @Mock
    SBuildServer server;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAll() throws InterruptedException {

        WebHookPayloadManager manager = new WebHookPayloadManager(server);

        WebHookEndPointContentStore store = new WebHookEndPointContentStore(manager);

        WebHookEndPointPayload payload1 = WebHookEndPointPayload.builder()
                .date(new Date())
                .contentType("application/json")
                .payload("blah blah")
                .build().generateHash();

        String hash1 = payload1.getHash();

        Thread.sleep(1000);

        WebHookEndPointPayload payload2 = WebHookEndPointPayload.builder()
                .date(new Date())
                .contentType("application/json")
                .payload("blah blah")
                .build().generateHash();

        String hash2 = payload2.getHash();


        store.put(payload1);
        store.put(payload2);

        Assert.assertEquals(hash1, store.getAll().get(1).getHash());
        System.out.println(store.getAll().get(1).getHash());
        Assert.assertEquals(hash2, store.getAll().get(0).getHash());
        System.out.println(store.getAll().get(0).getHash());
    }

}
