package webhook.testframework;

import java.io.IOException;

import org.apache.http.impl.client.HttpClients;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.WebHookProxyConfig;
import webhook.teamcity.settings.WebHookConfig;

public class MockWebHook extends WebHookImpl implements WebHook, Mocked {
	
	int invocationCount = 0;

	public MockWebHook() {
		super("", null, HttpClients.createDefault());
	}
	
	public MockWebHook(WebHookConfig webHookConfig, WebHookProxyConfig pc) {
		super(webHookConfig.getUrl(), pc, HttpClients.createDefault());
		this.setUrl(webHookConfig.getUrl());
		this.setEnabled(webHookConfig.getEnabled());
		this.setBuildStates(webHookConfig.getBuildStates());
		this.setHideSecureValues(webHookConfig.isHideSecureValues());
	}

	@Override
	public void post() throws IOException {
		invocationCount++;
		this.getExecutionStats().setUrl(this.getUrl());
    	this.getExecutionStats().setRequestStarting();
    	this.getExecutionStats().setRequestCompleted(500);
        this.getExecutionStats().setTeardownCompleted();
	}

	@Override
	public Integer getStatus() {
		return 500;
	}


	@Override
	public Boolean isErrored() {
		return true;
	}

	@Override
	public String getErrorReason() {
		return "I'm only a mock";
	}

	@Override
	public int getInvocationCount() {
		return invocationCount;
	}

}
