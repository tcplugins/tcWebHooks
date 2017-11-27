package webhook.testframework;

import java.util.ArrayList;
import java.util.List;

import webhook.WebHook;
import webhook.WebHookProxyConfig;
import webhook.teamcity.WebHookFactory;
import webhook.teamcity.settings.WebHookConfig;

public class MockingWebHookFactory implements WebHookFactory {
	
	List<MockWebHook> webHookMocks =  new ArrayList<>();

	@Override
	public WebHook getWebHook() {
		MockWebHook m = new MockWebHook();
		webHookMocks.add(m);
		return m;
	}

	@Override
	public WebHook getWebHook(WebHookConfig webhookConfig, WebHookProxyConfig pc) {
		MockWebHook m =  new MockWebHook(webhookConfig, pc);
		webHookMocks.add(m);
		return m;
	}
	
	public Mocked getMostRecentMock() {
		return webHookMocks.get(webHookMocks.size()-1);
	}

}
